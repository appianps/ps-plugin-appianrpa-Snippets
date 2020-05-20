package com.appian.rpa.snippets.examples.queuemanager;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.appian.rpa.snippets.examples.queuemanager.params.EEnvironmentVariables;
import com.appian.rpa.snippets.examples.queuemanager.params.EInstructions;
import com.appian.rpa.snippets.queuemanager.generic.manager.GenericQueueManager;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.annotations.Robot;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;
import com.novayre.jidoka.client.api.exceptions.JidokaItemException;

/**
 * Robot that searches the files in the folder given by parameters and maps them
 * to elements in the queue, setting the key and empty functional data. First,
 * the robot checks if there is a queue with today's date. If the queue exists,
 * the robot assigns it to itself. If not, the robot creates it and assigns it
 * to itself. Then, it checks if a file was previously added to the queue, and
 * if it was not added, the robot adds it. Finally, the robot finishes, leaving
 * the queue in a pending state.
 */
@Robot
public class FillQueueRobot implements IRobot {

	/** Server instance */
	IJidokaServer<?> server;

	/** GenericQueueManager instance */
	private GenericQueueManager genericQueueManager;

	/** Queue name */
	private String queueName;

	/** List of new files to add to the queue */
	private List<File> filesToAdd;

	@Override
	public boolean startUp() throws Exception {
		// Init modules and managers
		server = JidokaFactory.getServer();

		return IRobot.super.startUp();
	}

	/**
	 * Inits the robot variables
	 */
	public void init() {

		// Inits the queue name
		this.queueName = getQueueName();
	}

	/**
	 * Gets the queue name from the robot name and the current date and time
	 * 
	 * @return The generated queue name
	 */
	private String getQueueName() {
		return server.getExecution(0).getRobotName() + " - " + getDateFormated(new Date(), "yyyy-MM-dd");

	}

	/**
	 * Returns a {@link Date} formated.
	 * 
	 * @param date       The date to format
	 * @param dateFormat The format to apply
	 * @return The formatted date
	 */
	public static String getDateFormated(Date date, String dateFormat) {

		if (date == null) {
			return "";
		}

		return new SimpleDateFormat(dateFormat).format(date);
	}

	/**
	 * Creates the queue if it doesn't exist. If it exists, the queue is only
	 * assigned.
	 * 
	 * @return A list with the pending files
	 */
	public void checkIfQueueExists() {
		genericQueueManager = GenericQueueManager.assignExistingQueue(FileModel.class, this.queueName);

		if (genericQueueManager == null) {
			genericQueueManager = GenericQueueManager.createAndAssingNewQueue(FileModel.class, this.queueName);
		}
	}

	/**
	 * Checks if there are new files to add to the queue. If there are new files, it
	 * adds the files to the queue.
	 * 
	 * @return If there are new files to add.
	 */
	public String hasNewItemsToAdd() {

		Integer itemsPerRobot = EEnvironmentVariables.ITEMS_PER_ROBOT.getEnvironmentVariable().getAsInteger();

		int addedRobots = 0;

		filesToAdd = new ArrayList<>();

		for (File file : getFilesList()) {
			if (pendingOfProcess(file)) {
				filesToAdd.add(file);
				addedRobots++;
				if (addedRobots >= itemsPerRobot) {
					server.registerEvent("LAUNCH_CONSUMER_ROBOT");
					addedRobots = 0;
				}
			}
		}

		if (addedRobots > 0) {
			server.registerEvent("LAUNCH_CONSUMER_ROBOT");
		}

		if (filesToAdd.isEmpty()) {
			server.info("No new files to add to the queue");
			return "no";
		} else {
			server.info(filesToAdd.size() + " new files to add to the queue");
			return "yes";
		}

	}

	/**
	 * Gets the full folder files list.
	 * 
	 * @return The folder files list
	 */
	private List<File> getFilesList() {

		String filesFolder = EInstructions.FOLDER.getInstruction().getAsString();

		Path folderPath = Paths.get(server.getCurrentDir(), filesFolder);

		if (!Files.exists(folderPath)) {
			throw new JidokaFatalException("The path " + folderPath + " doesn't exist");
		}

		File folder = new File(folderPath.toString());

		return Arrays.asList(folder.listFiles());

	}

	/**
	 * Check if the given {@code file} is pending of process.
	 * 
	 * @param file File to check if is pending of process.
	 * @return True if the file is pending of process. In another case, it returns
	 *         false.
	 */
	private boolean pendingOfProcess(File file) {

		List<FileModel> filesList = genericQueueManager.findItems(FilenameUtils.getBaseName(file.getName()));

		return filesList.isEmpty();
	}

	/**
	 * Adds the new files to the queue.
	 */
	public void addNewItems() {

		for (File file : filesToAdd) {
			genericQueueManager.addItem(new FileModel(file));
		}
	}

	/**
	 * End action
	 */
	public void end() {
		server.info("No more files to add to the queue");
	}

	@Override
	public String[] cleanUp() throws Exception {

		return IRobot.super.cleanUp();
	}

	/**
	 * Manage exception.
	 *
	 * @param action    the action
	 * @param exception the exception
	 * @return the string
	 * @throws Exception the exception
	 */
	@Override
	public String manageException(String action, Exception exception) throws Exception {

		// We get the message of the exception
		String errorMessage = ExceptionUtils.getRootCause(exception).getMessage();

		// We send a screenshot to the log so the user can see the screen in the moment
		// of the error
		// This is a very useful thing to do
		server.sendScreen("Screenshot at the moment of the error");

		// If we have a FatalException we should abort the execution.
		if (ExceptionUtils.indexOfThrowable(exception, JidokaFatalException.class) >= 0) {

			server.error(StringUtils.isBlank(errorMessage) ? "Fatal error" : errorMessage);
			return IRobot.super.manageException(action, exception);
		}

		// If the error is processing one items we must mark it as a warning and go on
		// with the next item
		if (ExceptionUtils.indexOfThrowable(exception, JidokaItemException.class) >= 0) {

			server.setCurrentItemResultToWarn(errorMessage);
			reset();
			return "hasNewItemsToAdd";
		}

		server.warn("Unknown exception!");

		// If we have any other exception we must abort the execution, we don't know
		// what has happened

		return IRobot.super.manageException(action, exception);
	}

	/**
	 * This method reset the robot state to a stable state after a
	 * JidokaItemException is thrown
	 */
	private void reset() {
		// In this case the method is empty because
		// the robot loop reset always to a stable state.
	}
}

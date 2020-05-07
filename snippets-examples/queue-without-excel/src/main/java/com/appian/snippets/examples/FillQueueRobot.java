package com.appian.snippets.examples;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;

import com.appian.snippets.libraries.QueueFromGenericSource;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.annotations.Robot;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;
import com.novayre.jidoka.client.api.exceptions.JidokaQueueException;
import com.novayre.jidoka.client.api.queue.IQueue;

@Robot
public class FillQueueRobot implements IRobot {

	/**
	 * Pending files folder directory
	 */
	private static final String PARAM_FOLDER = "folder";

	/** Server instance */
	IJidokaServer<?> server;

	/** QueueItemsManager instance */
	private QueueFromGenericSource queueFromGenericSourceManager;

	/** Queue name */
	private String queueName;

	/** List of new files to add to the queue */
	private List<File> itemsToAdd;

	@Override
	public boolean startUp() throws Exception {
		// Init modules and managers
		server = JidokaFactory.getServer();
		queueFromGenericSourceManager = new QueueFromGenericSource(FileModel.class);

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
		return server.getExecution(0).getRobotName() + " - " + getDateFormated(new Date(), "yyyy-MM-dd - HH.mm");

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
		try {
			IQueue existingQueue = queueFromGenericSourceManager.findQueue(this.queueName);

			if (existingQueue == null) {
				queueFromGenericSourceManager.createQueue(this.queueName);
			}

		} catch (JidokaQueueException e) {
			throw new JidokaFatalException("Error creating/getting the queue", e);
		}
	}

	public String hasNewItemsToAdd() {

		List<File> filesList = getFilesList();

	}

	private List<FileModel> getFilesList() {

		String filesFolder = server.getParameters().get(PARAM_FOLDER);

		if (StringUtils.isBlank(filesFolder)) {
			throw new JidokaFatalException("You must indicate the input files folder directory");
		}
		Path folderPath = Paths.get(server.getCurrentDir(), filesFolder);

		if (!Files.exists(folderPath)) {
			throw new JidokaFatalException("The path " + folderPath + " doesn't exists");
		}

		File folder = new File(folderPath.toString());
		List<FileModel> listOfFiles = Arrays.asList(folder.listFiles()).stream().filter(f -> pendingOfProcess(f))
				.collect(Collectors.toList());

	}

	private Object pendingOfProcess(File f) {
		
		List<FileModel> files = queueFromGenericSourceManager.findItems(f.getName()).stream().map(i -> new FileModel());
		return null;
	}

	public void addNewItems(T object) {

	}
}

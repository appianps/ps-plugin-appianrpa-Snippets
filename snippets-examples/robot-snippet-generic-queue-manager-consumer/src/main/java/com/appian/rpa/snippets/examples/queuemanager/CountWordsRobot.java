package com.appian.rpa.snippets.examples.queuemanager;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.appian.rpa.snippets.queuemanager.generic.manager.GenericQueueManager;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.annotations.Robot;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;
import com.novayre.jidoka.client.api.exceptions.JidokaItemException;
import com.novayre.jidoka.client.api.multios.IClient;

/**
 * Robot that retrieves an element from the queue and counts the words in the
 * functional data of the element's content. First, the robot assigns today's
 * date to the queue, if it exists. If not, the robot makes an exception. Then,
 * it looks for the pending items in the queue and processes them, assigning one
 * of them to itself and obtaining the field with the file contents. The robot
 * then counts the words in the file contents and updates the functional data
 * field of the queue item that holds the number of words. Finally, the robot
 * finishes if there are no more elements to process.
 */
@Robot
public class CountWordsRobot implements IRobot {

	/** Server instance */
	IJidokaServer<?> server;

	/** GenericQueueManager instance */
	private GenericQueueManager genericQueueManager;

	/** Current FileModel object */
	private FileModel currentFile;

	/** The current item index. */
	private int currentItemIndex;

	@Override
	public boolean startUp() throws Exception {

		// Inits modules and managers
		server = JidokaFactory.getServer();

		return IRobot.super.startUp();
	}

	/**
	 * Inits the robot variables
	 */
	public void init() {

		try {
			// Inits global variables
			currentFile = new FileModel();

			// Gets the queue to process
			genericQueueManager = GenericQueueManager.assignExistingQueue(currentFile.getClass(), getQueueName());
			if (genericQueueManager == null) {
				throw new JidokaFatalException("Queue " + getQueueName() + " not found");
			}

			if (genericQueueManager.getQueue().pendingItems() == 0) {
				server.executionNeedless("No items to process");
			}

		} catch (Exception e) {
			throw new JidokaFatalException(e.getMessage(), e);
		}
	}

	/**
	 * Gets the queue name from the robot name and the current date and time
	 * 
	 * @return The generated queue name
	 */
	private String getQueueName() {
		return "5eb3d451e4b08994675a2580" + " - " + getDateFormated(new Date(), "yyyy-MM-dd");

	}

	/**
	 * Returns a {@link Date} formated.
	 * 
	 * @param date       The date to format
	 * @param dateFormat The format to apply
	 * @return The formatted date
	 */
	private static String getDateFormated(Date date, String dateFormat) {

		if (date == null) {
			return "";
		}

		return new SimpleDateFormat(dateFormat).format(date);
	}

	/**
	 * Checks for more items.
	 *
	 * @return the string
	 * @throws Exception the exception
	 */
	public String hasMoreItems() {

		try {

			// retrieve the next item in the queue
			currentFile = genericQueueManager.getNextItem();

			if (currentFile != null) {

				// set the stats for the current item
				currentItemIndex++;
				server.setNumberOfItems(currentItemIndex);
				server.setCurrentItem(currentItemIndex, currentFile.getFileName());

				return "yes";
			}

			return "no";

		} catch (Exception e) {
			throw new JidokaFatalException("Not possible to evaluate item", e);
		}
	}

	/**
	 * Counts the words of the current {@link FileModel} object
	 */
	public void countWords() {
		try {
			String content = currentFile.getFileContent();

			if (StringUtils.isBlank(content)) {
				currentFile.setNumOfWords(0);
			}
			String[] words = content.split("\\s+");
			currentFile.setNumOfWords(words.length);

			IClient.getInstance(this).pause(5000);
		} catch (Exception e) {
			throw new JidokaItemException("Error counting the words of item " + currentFile.getFileName());
		}

	}

	/**
	 * Updates the current item functional data
	 */
	public void updateQueueItem() {
		try {
			server.setCurrentItemResultToOK("Num of words: " + currentFile.getNumOfWords());
			genericQueueManager.updateItem(currentFile);
		} catch (Exception e) {
			throw new JidokaItemException("Error updating the current queue item");
		}
	}

	/**
	 * End action
	 */
	public void end() {
		server.info("No more items to process");
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
			if (currentFile != null) {
				// We release the item that we reserved, with new functional data
				genericQueueManager.updateItem(currentFile);
			}
			reset();
			return "hasMoreItems";
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
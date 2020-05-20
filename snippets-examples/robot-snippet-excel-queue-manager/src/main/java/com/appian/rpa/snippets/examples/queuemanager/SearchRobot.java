package com.appian.rpa.snippets.examples.queuemanager;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.WebElement;

import com.appian.rpa.snippets.browsermanager.BrowserManager;
import com.appian.rpa.snippets.browsermanager.SelectorsManager;
import com.appian.rpa.snippets.queuemanager.excel.manager.ExcelQueueManager;
import com.appian.rpa.snippets.queuemanager.excel.results.EQueueResultTarget;
import com.novayre.jidoka.browser.api.EBrowsers;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.annotations.Robot;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;
import com.novayre.jidoka.client.api.exceptions.JidokaItemException;
import com.novayre.jidoka.client.api.exceptions.JidokaUnsatisfiedConditionException;
import com.novayre.jidoka.client.api.multios.IClient;

/**
 * This robot is an example of using the ExcelQueueManager library. The robot
 * creates a queue using the input file given by parameters. Then, it process
 * the queue and uses the search term of the item to search on Google. When the
 * search has finished, it saves of the functional data the number of results
 * and the time required to search. Finally, it closes the queue, updates the
 * Excel file and returns it.
 * 
 */
@Robot
public class SearchRobot implements IRobot {

	/** URL for search */
	private static final String SEARCH_URL = "https://www.google.es/#q={0}";

	/** IJidokaServer instance */
	private IJidokaServer<?> server;

	/** IClient instance */
	IClient client;

	/** {@linkplain ExcelQueueManager} instance */
	ExcelQueueManager<SearchModel> excelQueueManager;

	/** {@linkplain BrowserManager} instance */
	BrowserManager browserManager;

	/** {@linkplain SelectorsManager} instance */
	SelectorsManager selectorsManager;

	/** Current SearchModel object */
	SearchModel currentSearch;

	/** Current item index. */
	private int currentItemIndex;

	@Override
	public boolean startUp() throws Exception {

		// Inits libraries and modules
		server = JidokaFactory.getServer();
		client = IClient.getInstance(this);

		excelQueueManager = new ExcelQueueManager<>(this, new SearchMapper());
		browserManager = new BrowserManager(this, EBrowsers.CHROME);
		selectorsManager = browserManager.getSelectorsManager();

		return IRobot.super.startUp();
	}

	/**
	 * Start action where to initialize the global variables
	 */
	public void start() {
		// no global variables to initialize
	}

	/**
	 * Assigns the queue using the given Excel file or usign the preselected queue.
	 */
	public void assignQueue() {
		try {
			excelQueueManager.assignQueue();

			server.setNumberOfItems(excelQueueManager.getCurrentQueue().pendingItems());
		} catch (Exception e) {
			throw new JidokaFatalException("Error assigning the queue", e);
		}
	}

	/**
	 * Opens the browser
	 */
	public void openBrowser() {
		try {
			browserManager.openBrowser();
		} catch (Exception e) {
			throw new JidokaFatalException("Error opening the browser", e);
		}
	}

	/**
	 * Checks if there are more items to process on the queue
	 * 
	 * @return 'yes' if there are more items to process. Otherwise, returns 'no'.
	 */
	public String hasMoreItems() {

		try {

			// retrieve the next item in the queue
			currentSearch = excelQueueManager.getNextItem();

			if (currentSearch != null) {

				// set the stats for the current item
				currentItemIndex++;
				server.setCurrentItem(currentItemIndex, currentSearch.getSearchTerm());

				return "yes";
			}

			return "no";

		} catch (Exception e) {
			throw new JidokaFatalException("Error getting the next item", e);
		}
	}

	/**
	 * Search for the current {@linkplain SearchModel} object search term.
	 */
	public void searchTerm() {

		try {
			String url = MessageFormat.format(SEARCH_URL, URLEncoder.encode(currentSearch.getSearchTerm(), "UTF-8"));

			browserManager.navigateTo(url);

			// It is used an array instead of a simple object to avoid the error:
			// Local variable searchResults defined in an enclosing scope must be final or
			// effectively final
			WebElement[] searchResults = new WebElement[1];

			client.getWaitFor(this).wait(10, "Waiting for search results", false, false, () -> {
				try {
					searchResults[0] = selectorsManager.getElement("resultsPage.numberOfResults.id");
					return searchResults[0] != null;
				} catch (Exception e) {
					return false;
				}
			});

			if (searchResults.length == 0) {
				// Results not found.
				throw new JidokaItemException(
						String.format("Search term %s not found: ", currentSearch.getSearchTerm()));
			}

			// Extracts the search results number
			Pattern p = Pattern.compile(".*?(\\d.*?)\\s.*");
			Matcher m = p.matcher(searchResults[0].getText());

			if (m.find()) {
				currentSearch.setNumberOfResults(m.group(1).replace(",", ""));
			} else {
				throw new JidokaItemException("NUMBER OF RESULTS not found");
			}

			WebElement timeRequired = selectorsManager.getElement("resultsPage.timeRequired.css");

			if (timeRequired == null) {
				// Time required not found.
				throw new JidokaItemException(
						String.format("Time required %s not found for search term: ", currentSearch.getSearchTerm()));
			}

			Pattern p2 = Pattern.compile("\\((\\d.*?)\\s.*");
			Matcher m2 = p2.matcher(timeRequired.getText());

			if (m2.find()) {
				currentSearch.setTimeRequired(m2.group(1));
			} else {
				throw new JidokaItemException("TIME_REQUIRED not found");
			}

		} catch (UnsupportedEncodingException | JidokaUnsatisfiedConditionException e) {
			throw new JidokaItemException("Error searching for the term '" + currentSearch.getSearchTerm() + "'");
		}

	}

	/**
	 * Updates the current queue item functional data and sets it to OK.
	 */
	public void updateQueueItem() {
		try {
			server.setCurrentItemResultToOK();

			excelQueueManager.saveItem(currentSearch);
		} catch (Exception e) {
			throw new JidokaItemException("Error updating the item '" + currentSearch.getSearchTerm() + "'");
		}
	}

	/**
	 * Closes the queue and sets the results target to an Excel file
	 */
	public void closeQueue() {
		try {
			// First we reserve the queue (other robots can't reserve the queue at the same
			excelQueueManager.closeQueue(EQueueResultTarget.EXCEL);

		} catch (Exception e) {
			throw new JidokaFatalException("Error closing the current queue", e);
		}
	}

	/**
	 * Action 'End'.
	 */
	public void end() {
		// Here the robot ends its execution

	}

	/**
	 * Clean up.
	 *
	 * @return the string[]
	 * @throws Exception the exception
	 * @see com.novayre.jidoka.client.api.IRobot#cleanUp()
	 */
	@Override
	public String[] cleanUp() throws Exception {
		browserManager.browserCleanUp();

		// If the Excel output file exists, returns it
		if (excelQueueManager.getQueueOutputFile().exists()) {
			return new String[] { excelQueueManager.getQueueOutputFile().getAbsolutePath() };
		}

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
			if (currentSearch != null) {
				// We release the item that we reserved, with new functional data
				excelQueueManager.saveItem(currentSearch);
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
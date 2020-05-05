package com.appian.robot.demo.nasdaq.robot;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import com.appian.robot.demo.nasdaq.NasdaqWebConstants;
import com.appian.robot.demo.nasdaq.NasdaqWebManager;
import com.appian.robot.demo.nasdaq.automation.StockApplicationConstants;
import com.appian.robot.demo.nasdaq.automation.UIAutomationManager;
import com.appian.robot.demo.nasdaq.excel.EnterpriseMappedSearch;
import com.appian.robot.demo.nasdaq.excel.EnterpriseModel;
import com.appian.robot.demo.nasdaq.falcon.FalconManager;
import com.appian.rpa.snippets.commons.application.ApplicationManager;
import com.appian.rpa.snippets.commons.browser.BrowserManager;
import com.appian.rpa.snippets.commons.queues.EQueueResultTarget;
import com.appian.rpa.snippets.commons.queues.QueueItemsManager;
import com.novayre.jidoka.browser.api.EBrowsers;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.ItemData;
import com.novayre.jidoka.client.api.ItemData.EResult;
import com.novayre.jidoka.client.api.ItemData.ESubResult;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.annotations.Robot;
import com.novayre.jidoka.client.api.exceptions.JidokaException;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;
import com.novayre.jidoka.client.api.exceptions.JidokaItemException;
import com.novayre.jidoka.client.api.exceptions.JidokaQueueException;

/**
 * Browser module robot.
 * 
 * @author Jidoka
 *
 */
@Robot
public class MainRobot implements IRobot {

	/** Server instance */
	private IJidokaServer<Serializable> server;

	/** NasdaqWebManager instance */
	private NasdaqWebManager nasdaqWebManager;

	/** UIAutomationManager instance */
	private UIAutomationManager uiAutomationManager;

	/** Application Manager instance */
	private ApplicationManager applicationManager;

	/** Falcon manager instance */
	private FalconManager falconManager;

	/** Queue Items manager */
	private QueueItemsManager<EnterpriseModel> queueItemsManager;

	/** BrowserManager instance */
	private BrowserManager browserManager;

	/** The current EnterpriseModel item */
	private EnterpriseModel currentEnterprise;

	/** The current item index. */
	private int currentItemIndex;

	/** Param with the module that manage the application. */
	private static final String PARAM_APPLICATION_MANAGER = "applicationManagerName";

	/** UIAutomation module name */
	private static final String UIAUTOMATION_NAME = "UIAutomation";

	/** Falcon module name */
	private static final String FALCON_NAME = "Falcon";

	/** UIAutomation or Falcon */
	private String applicationManagerName;

	@SuppressWarnings("unchecked")
	@Override
	public boolean startUp() throws Exception {
		// Get an instance of IJidokaServer to communicate with the server
		server = (IJidokaServer<Serializable>) JidokaFactory.getServer();

		// BrowserManager init
		browserManager = new BrowserManager(this, EBrowsers.CHROME);

		// NadasqWebManager init
		nasdaqWebManager = new NasdaqWebManager(browserManager);

		// QueueItemsManager init
		queueItemsManager = new QueueItemsManager<>(this, new EnterpriseMappedSearch());

		// UIAutomationManager init
		uiAutomationManager = new UIAutomationManager(this);

		// FalconManager init
		falconManager = new FalconManager(this);

		// ApplicationManager init
		applicationManager = new ApplicationManager(this, StockApplicationConstants.General.APP_NAME,
				StockApplicationConstants.General.APP_FOLDER, StockApplicationConstants.Titles.APP_MAIN_WINDOW_TITLE);

		return true;
	}

	/**
	 * Action 'Init'.
	 * <p>
	 * Initializes Jidoka modules.
	 * 
	 * @throws JidokaException if the input file couldn't be read
	 * @throws IOException
	 */
	public void init() {

		try {
			// Current enterprise init
			currentEnterprise = new EnterpriseModel();

			applicationManagerName = server.getParameters().get(PARAM_APPLICATION_MANAGER);

			if (StringUtils.isBlank(applicationManagerName)) {
				applicationManagerName = UIAUTOMATION_NAME;
			}

			server.debug("Robot initialized");
		} catch (Exception e) {
			throw new JidokaFatalException("Error initializing");
		}
	}

	/**
	 * Select queue.
	 *
	 * @throws IOException          Signals that an I/O exception has occurred.
	 * @throws JidokaQueueException the jidoka queue exception
	 * @throws JidokaException
	 */
	public void assignQueue() throws JidokaQueueException {

		try {

			queueItemsManager.assignQueue();

			server.setNumberOfItems(queueItemsManager.getCurrentQueue().pendingItems());
		} catch (Exception e) {
			throw new JidokaFatalException("Error creating the queue", e);
		}
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
			currentEnterprise = queueItemsManager.getNextItem();

			if (currentEnterprise != null) {

				// set the stats for the current item
				currentItemIndex++;
				server.setCurrentItem(currentItemIndex, currentEnterprise.getSymbols());

				return "yes";
			}

			return "no";

		} catch (Exception e) {
			throw new JidokaFatalException("Not possible to evaluate item", e);
		}
	}

	/**
	 * Update item queue. With the information extracted from the web
	 *
	 * @throws JidokaQueueException         the Jidoka queue exception
	 * @throws UnsupportedEncodingException
	 */
	public void updateItemQueue() throws JidokaQueueException {

		logFunctionalData();

		queueItemsManager.saveItem(currentEnterprise);

	}

	/**
	 * Open Browser.
	 *
	 * @throws Exception the exception
	 */
	public void openBrowser() {

		try {
			browserManager.openBrowser();
		} catch (Exception e) {
			throw new JidokaFatalException("Error opening the browser");
		}
	}

	/**
	 * Find company match with SYMBOL.
	 *
	 * @throws EnterpriseNotFoundException the company not found exception
	 */
	public void findCompany() {
		try {
			nasdaqWebManager.findCompany(currentEnterprise.getSymbols());
		} catch (Exception e) {
			throw new JidokaItemException("Error finding the company");
		}
	}

	/**
	 * Get data.
	 *
	 * @return the data
	 * @throws Exception the exception
	 */
	public void getData() {

		try {
			currentEnterprise.setSymbols(currentEnterprise.getSymbols());
			nasdaqWebManager.getData(currentEnterprise);
		} catch (Exception e) {
			throw new JidokaItemException(e.getMessage());
		}
	}

	/**
	 * Log functional data.
	 */
	private void logFunctionalData() {

		try {

			// Set current item result. We use ItemData to set result and subresult
			ItemData itemResult = new ItemData().result(EResult.OK)
					.detail(String.format("%s | %s | %s", currentEnterprise.getCompanyName(),
							currentEnterprise.getLastPrice(), currentEnterprise.getChangePercent()));

			switch (currentEnterprise.getArrow()) {
			case NasdaqWebConstants.Data.INCREASE_NAME:
				itemResult.setSubResult(ESubResult.LIME);
				break;
			case NasdaqWebConstants.Data.DECREASE_NAME:
				itemResult.setSubResult(ESubResult.RED);
				break;
			case NasdaqWebConstants.Data.ARROW_VALUE_NULL:
				itemResult.setSubResult(ESubResult.LIGHT_GRAY);
				break;
			default:
				break;
			}

			server.setCurrentItemResult(itemResult);

		} catch (Exception e) {
			throw new JidokaItemException("Error logging the functional data");
		}
	}

	/**
	 * Close queue action
	 *
	 * @return the string
	 * @throws IOException          Signals that an I/O exception has occurred.
	 * @throws JidokaQueueException the jidoka queue exception
	 */
	public void closeQueue() {

		try {
			// First we reserve the queue (other robots can't reserve the queue at the same
			queueItemsManager.closeQueue(EQueueResultTarget.EXCEL);

		} catch (JidokaQueueException e) {
			server.info("Error closing the queue and updating the output file");
		}

	}

	/**
	 * Init application
	 */
	public void startApplication() {

		try {
			applicationManager.startApplication();
		} catch (Exception e) {
			throw new JidokaFatalException("Error starting the application");
		}
	}

	/**
	 * New company
	 * 
	 */
	public void newCompany() {

		try {
			if (applicationManagerName.equals(FALCON_NAME)) {
				falconManager.newCompany();
			} else if (applicationManagerName.equals(UIAUTOMATION_NAME)) {
				uiAutomationManager.newCompany();
			} else {
				throw new JidokaFatalException("None application manager selected");
			}
		} catch (Exception e) {
			throw new JidokaItemException("Error creating a new company");
		}
	}

	/**
	 * Fill fields
	 * 
	 * @throws AutomationException
	 */
	public void fillFields() {

		try {
			if (applicationManagerName.equals(FALCON_NAME)) {
				falconManager.fillFields(currentEnterprise);
			} else if (applicationManagerName.equals(UIAUTOMATION_NAME)) {
				uiAutomationManager.fillFields(currentEnterprise);
			} else {
				throw new JidokaFatalException("None application manager selected");
			}
		} catch (Exception e) {
			throw new JidokaItemException("Error filling the application fields");
		}
	}

	/**
	 * Click buttons
	 * 
	 * @throws AutomationException
	 * @throws JidokaUnsatisfiedConditionException
	 */
	public void clickButtons() {
		try {
			if (applicationManagerName.equals(FALCON_NAME)) {
				falconManager.clickSaveButton();
			} else if (applicationManagerName.equals(UIAUTOMATION_NAME)) {
				uiAutomationManager.clickSaveButton();
			} else {
				throw new JidokaFatalException("None application manager selected");
			}
		} catch (Exception e) {
			throw new JidokaItemException("Error clicking the application buttons");
		}
	}

	/**
	 * Action 'End'.
	 */
	public void end() {

		// continue the process, here the robot ends its execution

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

		// Close the Stock Manager application
		applicationManager.closeApp();

		// If the Excel output file exists, returns it
		if (queueItemsManager.getQueueOutputFile().exists()) {
			return new String[] { queueItemsManager.getQueueOutputFile().getAbsolutePath() };
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
			if (currentEnterprise != null) {
				// We release the item that we reserved, with new functional data
				queueItemsManager.saveItem(currentEnterprise);
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

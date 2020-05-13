package com.appian.rpa.robot.browser_manager;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.By;

import com.appian.rpa.snippets.commons.browser.BrowserManager;
import com.appian.rpa.snippets.commons.browser.SelectorsManager;
import com.novayre.jidoka.browser.api.EBrowsers;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.annotations.Robot;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;
import com.novayre.jidoka.client.api.multios.IClient;

/**
 * 
 * This robotic process has been created to illustrate how the Browser Manager
 * Snippet should be integrated in your process. AS IS scheme, Further
 * instructions to configure and execute the process can be found here:
 * https://github.com/appianps/ps-plugin-appianrpa-Snippets
 * 
 * @author javier.advani
 */

@Robot
public class BrowserManagerRobot implements IRobot {
	private IJidokaServer<?> server;
	private BrowserManager browserManager;
	/** The IClient module. */
	private IClient client;
	private SelectorsManager selectors;
	private String searcherURL;

	/**
	 * Override startup method to initialize some variables involved in our process.
	 */
	@Override
	public boolean startUp() throws Exception {

		server = JidokaFactory.getServer();
		client = IClient.getInstance(this);

		return true;
	}

	/**
	 * Initialize the objects involved in the robotic process.
	 * 
	 * @throws Exception
	 */

	public void start() throws Exception {

		browserManager = new BrowserManager(this, EBrowsers.CHROME);
		searcherURL = server.getParameters().get("platformURL");
		selectors = browserManager.getSelectorsManager();
	}

	/*
	 * Open a new Chrome web browser.
	 */
	public void openBrowser() {
		browserManager.openBrowser();
	}

	/**
	 * Navigate to searcherURL. An exception is thrown if the search button selector
	 * was not found.
	 * 
	 * @throws Exception
	 */
	public void navigateToWeb() throws JidokaFatalException {
		browserManager.navigateTo(searcherURL);
		if (!isClassNameElementSuccessfullyLoaded(selectors.getSelector("selector.search-button.classname"))) {
			throw new JidokaFatalException("page could not be loaded");
		}

	}

	/**
	 * If the search result was successfully loaded, print the first result found in
	 * Console.
	 */

	public void printWebResultInConsole() {
		if (!isXPathElementSuccessfullyLoaded(selectors.getSelector("selector.appian-result.xpath"))) {
			throw new JidokaFatalException("web result not found");
		}
		server.info(browserManager.getBrowser().getText(selectors.getBy("selector.appian-result.xpath")));
	}

	/**
	 * Search results for "Appian" String
	 */

	public void searchInformation() {

		browserManager.getBrowser()
				.clickSafe(browserManager.getBrowser().waitElement(selectors.getBy(("selector.searchbar.xpath"))));
		client.pause(1000);
		client.typeText("Appian");
		client.keyboard().enter();
		client.pause(3000);
	}

	private boolean isClassNameElementSuccessfullyLoaded(String classname) {
		return client.waitCondition(5, 1000, "Checking if " + classname + " selector appeared successfully", null,
				(i, c) -> browserManager.getBrowser().getElement(By.className(classname)) != null);
	}

	private boolean isXPathElementSuccessfullyLoaded(String xpath) {
		return client.waitCondition(5, 1000, "Checking if " + xpath + " selector appeared successfully", null,
				(i, c) -> browserManager.getBrowser().getElement(By.xpath(xpath)) != null);
	}

	/**
	 * close Chrome Browser
	 */

	private void closeBrowser() {
		browserManager.browserCleanUp();

	}

	/**
	 * Close all the applications
	 */
	public void close() {
		closeBrowser();
	}

	/**
	 * This is the last non-hidden action from the robot workflow.
	 */

	public void end() {
		server.info("Execution finished");
	}

	/**
	 * Any type of error should be managed in this method.
	 */
	@Override
	public String manageException(String action, Exception exception) throws Exception {

		// Get the exception message
		String errorMessage = ExceptionUtils.getRootCause(exception).getMessage();

		// Send a screenshot to the log so the user can see the screen in the moment
		// of the error. This is one of the best ways to trace errors clearly.
		server.sendScreen("Screenshot of the error");

		// Whenever a JidokaFatalException is thrown, the execution should be aborted.
		if (ExceptionUtils.indexOfThrowable(exception, JidokaFatalException.class) >= 0) {

			server.error(StringUtils.isBlank(errorMessage) ? "Fatal error" : errorMessage);
			return IRobot.super.manageException(action, exception);
		}

		server.warn("Unknown exception!");

		// If we have any other exception we must abort the execution, as we do not know
		// what happened

		return IRobot.super.manageException(action, exception);
	}

	/**
	 * Override cleanUp method. Any additional data or processes to be released
	 * should be placed here.
	 */

	@Override
	public String[] cleanUp() throws Exception {
		return IRobot.super.cleanUp();

	}

}

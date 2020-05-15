package com.appian.rpa.snippets.examples.browsermanager;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;

import com.appian.rpa.snippets.browsermanager.BrowserManager;
import com.appian.rpa.snippets.browsermanager.SelectorsManager;
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
 * Snippet should be integrated in your process. It basically searches the word
 * "Appian" in Google and shows up the first result found in the console. An
 * exception will be thrown if any error occurs during the process execution.
 * Further instructions to configure and execute the process can be found here:
 * https://github.com/appianps/ps-plugin-appianrpa-Snippets
 * 
 * @author javier.advani
 */

@Robot
public class BrowserManagerRobot implements IRobot {

	/** Jidoka server */
	private IJidokaServer<?> server;
	/** Browser Manager to apply common functions */
	private BrowserManager browserManager;
	/** The IClient module. */
	private IClient client;
	/** Browser Manager to apply selector utilities */
	private SelectorsManager selectorsManager;
	/**
	 * This parameter provides the website where the search term will be placed in
	 **/
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
		selectorsManager = browserManager.getSelectorsManager();
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
	 * @throws JidokaFatalException
	 */
	public void navigateToWeb() throws JidokaFatalException {
		
		browserManager.navigateTo(searcherURL);
		
		waitForElementToBeLoaded("selector.search-button.classname") ;
	}

	/**
	 * If the search result was successfully loaded, print the first result found in
	 * Console 
	 * 
	 * @throws JidokaFatalException
	 */
	public void printWebResultInConsole() throws JidokaFatalException {
		
		waitForElementToBeLoaded("selector.appian-result.xpath");
		
		server.info(browserManager.getBrowser().getText(selectorsManager.getBy("selector.appian-result.xpath")));
	}

	/**
	 * Search results for "Appian" String
	 */
	public void searchInformation() {

		browserManager.getBrowser()
				.clickSafe(browserManager.getBrowser().waitElement(selectorsManager.getBy(("selector.searchbar.xpath"))));
		client.pause(1000);
		browserManager.getBrowser().textFieldSet(selectorsManager.getBy("selector.searchbar.xpath"), "Appian", true);
		client.keyboard().enter();
	}

	private void waitForElementToBeLoaded(String selectorName) throws JidokaFatalException {
		
		if(!client.waitCondition(5, 1000, "Checking if selector "+ selectorName +" appeared successfully", null,
				(i, c) -> selectorsManager.existsElement(selectorName))) {
			
			throw new JidokaFatalException(selectorName + " has not been loaded");
		}
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

		if (browserManager != null) {
			browserManager.browserCleanUp();
		}

		return IRobot.super.cleanUp();

	}

}

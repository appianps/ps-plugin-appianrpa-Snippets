package com.appian.rpa.robot.browser_manager;

import org.openqa.selenium.By;

import com.novayre.jidoka.browser.api.EBrowsers;
import com.novayre.jidoka.browser.api.IWebBrowserSupport;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.annotations.Robot;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;
import com.novayre.jidoka.client.api.multios.IClient;

import com.appian.rpa.snippets.commons.browser.BrowserManager;
import com.appian.rpa.snippets.commons.browser.SelectorsManager;

/**
 * 
 * This robotic process has been created to illustrate how the browser manager
 * snippet should be integrated in your process. AS IS scheme, Further
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
	private IWebBrowserSupport browser;
	private SelectorsManager selectors;
	private String searcherURL;

	/**
	 * Initialize your objects involved in the robotic process.
	 * 
	 * @throws Exception
	 */

	public void start() throws Exception {
		browserManager = new BrowserManager(this, EBrowsers.CHROME);
		server = (IJidokaServer<?>) JidokaFactory.getServer();
		client = IClient.getInstance(this);
		browser = IWebBrowserSupport.getInstance(this, client);
		searcherURL = server.getParameters().get("platformURL");
		selectors = browserManager.getSelectorsManager();
	}

	/*
	 * Use the browserManager to open the previously chosen browser.
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
		//dynamic wait 
		//client.pause(3000);
		if (!isClassNameElementSuccessfullyLoaded(selectors.getSelector("selector.search-button.classname"))) {
			throw new JidokaFatalException("page could not be loaded");
		}

	}

	public void printWebResultInConsole() {
		if (!isXPathElementSuccessfullyLoaded(selectors.getSelector("selector.appian-result.xpath"))) {
			throw new JidokaFatalException("web result not found");
		}
		server.info(browser.getText(By.xpath(selectors.getSelector("selector.appian-result.xpath"))));
	}

	public void searchInformation() {
		browser.clickSafe(browser.waitElement(By.xpath(selectors.getSelector("selector.searchbar.xpath"))));
		client.pause(1000);
		client.typeText("Appian");
		client.keyboard().enter();
		client.pause(3000);
	}

	private boolean isClassNameElementSuccessfullyLoaded(String classname) {
		return client.waitCondition(5, 1000, "Checking if " + classname + " selector appeared successfully", null,
				(i, c) -> browser.getElement(By.className(classname)) != null);
	}

	private boolean isXPathElementSuccessfullyLoaded(String xpath) {
		return client.waitCondition(5, 1000, "Checking if " + xpath + " selector appeared successfully", null,
				(i, c) -> browser.getElement(By.xpath(xpath)) != null);
	}

	private void closeBrowser() {
		browserManager.browserCleanUp();

	}

	/**
	 * Close all the applications
	 */
	public void close() {
		closeBrowser();
	}
	
	@Override
	public void cleanUp() {
		
	}

	/**
	 * Any additional data or processes to be released should be placed here.
	 */

	public void end() {
		server.info("Execution finished");
	}

}

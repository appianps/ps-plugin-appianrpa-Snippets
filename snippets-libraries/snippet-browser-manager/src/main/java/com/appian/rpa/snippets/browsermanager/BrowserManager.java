package com.appian.rpa.snippets.browsermanager;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.novayre.jidoka.browser.api.EBrowsers;
import com.novayre.jidoka.browser.api.IWebBrowserSupport;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;
import com.novayre.jidoka.client.api.exceptions.JidokaItemException;
import com.novayre.jidoka.client.api.exceptions.JidokaUnsatisfiedConditionException;
import com.novayre.jidoka.client.api.multios.EClientShowWindowType;

/**
 * This utility let the robot manage a web browser (Chrome, Firefox and
 * IExplorer are supported). Your process should include an instance for each
 * browser window involved in the robot actions.
 */
public class BrowserManager extends SelectorsManager {

	/** Selected browser */
	private EBrowsers selectedBrowser;

	/** ScreenshotManager instance */
	private ScreenshotsManager screenShotsManager;

	/**
	 * BrowserManager constructor
	 * 
	 * @param selectedBrowser Browser to initialize
	 * @param selectorsFiles  List of files containing the web element selectors
	 */
	public BrowserManager(EBrowsers selectedBrowser, List<File> selectorsFiles) {
		super.init(selectorsFiles);
		IRobot robot = IRobot.getDummyInstance();

		browser = IWebBrowserSupport.getInstance(robot, client);
		browser.setTimeoutSeconds(120);

		screenShotsManager = new ScreenshotsManager();

		if (selectedBrowser == null) {
			throw new JidokaFatalException("You must select the browser to open");
		}

		this.selectedBrowser = selectedBrowser;

		// Sets the browser type
		browser.setBrowserType(this.selectedBrowser);

	}

	/**
	 * Return the browser object to let the users interact with the same instance.
	 * This can be useful to retrieve web elements and interact with the
	 * SelectorsManager class methods too.
	 */

	public IWebBrowserSupport getBrowser() {
		return browser;
	}

	/**
	 * Initialize and open a new browser window. Set the browser type as the one
	 * passed by the constructor instance.
	 */
	public void openBrowser() {

		try {

			if (selectedBrowser.equals(EBrowsers.CHROME)) {
				ChromeOptions options = new ChromeOptions();

				options.addArguments("--disable-dev-shm-usage"); // overcome limited resource problems
				options.addArguments("--no-sandbox"); // Bypass OS security model

				browser.setCapabilities(options);
			} else if (selectedBrowser.equals(EBrowsers.INTERNET_EXPLORER)) {
				DesiredCapabilities ieCapabilities = DesiredCapabilities.internetExplorer();
				ieCapabilities.setCapability("nativeEvents", false);
				ieCapabilities.setCapability("unexpectedAlertBehaviour", "accept");
				ieCapabilities.setCapability("ignoreProtectedModeSettings", true);
				ieCapabilities.setCapability("disable-popup-blocking", true);
				ieCapabilities.setCapability("enablePersistentHover", true);
				ieCapabilities.setCapability("ignoreZoomSetting", true);

				InternetExplorerOptions ieOptions = new InternetExplorerOptions(ieCapabilities);

				browser.setCapabilities(ieOptions);
			}

			// Inits browser
			browser.initBrowser();

			// Waits for the browser to open
			waitFor.window(getBrowserWindowTitle());

		} catch (Exception e) {
			throw new JidokaFatalException("Error initializing the browser: " + e.getMessage(), e);
		}
	}

	/**
	 * Navigates to the given {@code url} . Before that, the browser window is
	 * activated.
	 * 
	 * @param url as String contains the target website
	 */
	@SuppressWarnings("unchecked")
	public void navigateTo(String url) {

		try {
			// Focus on app and activate the window on client module
			client.activateWindow(getBrowserWindowTitle());

			client.showWindow(client.getWindow(getBrowserWindowTitle()).getId(), EClientShowWindowType.MAXIMIZE);

			// Navigate to URL
			browser.navigate(url);

		} catch (Exception e) {
			throw new JidokaItemException((IJidokaServer<Serializable>) JidokaFactory.getServer(),
					"Error navigating to " + url, e);
		}
	}

	/**
	 * Navigates to the given {@code url} and waits until the given
	 * {@code selectorKey} element is loaded. Before that, the browser window is
	 * activated.
	 * 
	 * To use this method, the robot must use a selectors.properties file with
	 * selectors key/value pairs.
	 * 
	 * @param url         as String contains the target website
	 * @param selectorKey Selector key on the selectors.properties file
	 * @param message     Console message
	 * @param seconds     Waiting time in seconds
	 * @return True if the element has been loaded
	 * 
	 */
	public boolean navigateTo(String url, String selectorKey, String message, Integer seconds) {

		navigateTo(url);

		return waitForElement(selectorKey, message, seconds);
	}

	/**
	 * Waits for the given {@code selectorKey} element to load 10 seconds by
	 * default.
	 * 
	 * @param selectorKey Selector key on the selectors.properties file
	 * @return True if the element has been loaded
	 */
	public boolean waitForElement(String selectorKey, String message) {

		return waitForElement(selectorKey, message, 10);
	}

	/**
	 * Waits for the given {@code selectorKey} element to load.
	 * 
	 * @param selectorKey Selector key on the selectors.properties file
	 * @param message     Console message
	 * @param seconds     Waiting time in seconds
	 * @return True if the element has been loaded
	 */
	public boolean waitForElement(String selectorKey, String message, Integer seconds) {

		try {
			return waitFor.wait(seconds, message, false, () -> {
				try {

					return super.getElement(selectorKey) != null;
				} catch (JidokaFatalException e) {
					throw new JidokaFatalException(e.getMessage(), e);
				} catch (Exception e) {
					return false;
				}
			});
		} catch (JidokaUnsatisfiedConditionException e) {
			throw new JidokaFatalException("Error waiting for the given web element to load", e);
		}
	}

	/**
	 * Get the selected browser window title
	 * 
	 * @return The browser window title
	 */
	private String getBrowserWindowTitle() {

		switch (this.selectedBrowser) {
		case CHROME:
			return ".*Chrome.*";
		case INTERNET_EXPLORER:
			return ".*Explorer.*";
		case FIREFOX:
			return ".*Firefox.*";
		default:
			break;
		}
		return null;
	}

	/**
	 * Close the browser window.
	 */
	public void browserCleanUp() {

		// Close if initialized
		if (browser == null) {
			return;
		}
		try {

			browser.close();
			forceBrowserProcessKill();

			browser = null;

		} catch (Exception e) {
			// Ignore exception
		}
	}

	/**
	 * If the browser passed to the cleanUp function was previously null, we ensure
	 * that the snippet does not leave any opened window.
	 * 
	 * @throws IOException
	 */

	private void forceBrowserProcessKill() throws IOException {
		JidokaFactory.getServer().info("Killing webdriver process from windows module");
		switch (selectedBrowser) {
		case CHROME:
			client.killAllProcesses("chromedriver.exe", 1000);
			break;
		case INTERNET_EXPLORER:
			client.killAllProcesses("IEDriverServer.exe", 1000);
			client.killAllProcesses("iexplore.exe", 1000);
			break;
		case FIREFOX:
			client.killAllProcesses("geckodriver.exe", 1000);
			break;
		default:
			break;
		}
	}

	/**
	 * Get the {@link ScreenShotsManager} instance
	 * 
	 * @return The {@link ScreenShotsManager} instance
	 */
	public ScreenshotsManager getScreenShotManager() {
		return this.screenShotsManager;
	}

	/**
	 * Click on the given element
	 * 
	 * @param selectorKey Selector key on the selectors.properties file
	 * @return True if click was success
	 */
	public boolean clickOnElement(String selectorKeyClick) {

		WebElement ele = super.getElement(selectorKeyClick);

		try {
			ele.click();
		} catch (Exception e) {
			return false;
		}

		return true;
	}

	/**
	 * Click on the given element and wait until find another element
	 * 
	 * @param selectorKey Selector key on the selectors.properties file
	 * @param message     Console message
	 * @param seconds     Waiting time in seconds
	 * @return True if click was success
	 */
	public boolean clickOnElement(String selectorKeyClick, String selectorKeyWait, String message, int seconds) {

		WebElement ele = super.getElement(selectorKeyClick);

		try {
			ele.click();
		} catch (Exception e) {
			return false;
		}

		if (selectorKeyWait != null) {

			return waitForElement(selectorKeyWait, message, seconds);
		}

		return true;
	}

	/**
	 * Sets the given non-mandatory {@link WebElement} text
	 * 
	 * @param key   Selector key on the selectors file
	 * @param text  Text to set on the given element
	 * @param clear If true clears the found text field before setting the new value
	 */
	public void setElementText(String key, String text, Boolean clear) {

		setElementText(key, text, clear, false);
	}

	/**
	 * Sets the given {@link WebElement} text
	 * 
	 * @param key       Selector key on the selectors file
	 * @param text      Text to set on the given element
	 * @param clear     If true clears the found text field before setting the new
	 *                  value
	 * @param mandatory True if the field is mandatory
	 */
	public void setElementText(String key, String text, Boolean clear, Boolean mandatory) {
		if (!mandatory) {
			if (!StringUtils.isEmpty(text)) {
				browser.textFieldSet(super.getBy(key), text, clear);
			}
		} else {
			if (StringUtils.isEmpty(text)) {
				throw new JidokaItemException("A mandatory field can't be null or empty");
			} else {
				browser.textFieldSet(super.getBy(key), text, clear);
			}
		}

	}

	/**
	 * Moves to the given element
	 * 
	 * @param key Selector key on the selectors file
	 */
	public void moveToElement(String key) {
		browser.moveTo(super.getElement(key));
	}

	/**
	 * Return the web driver
	 * 
	 * @return
	 */
	public WebDriver getDriver() {
		return browser.getDriver();
	}

}

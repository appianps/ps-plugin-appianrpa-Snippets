package com.appian.rpa.snippets.browsermanager;

import java.io.IOException;
import java.io.Serializable;

import org.openqa.selenium.NoSuchSessionException;

import com.novayre.jidoka.browser.api.EBrowsers;
import com.novayre.jidoka.browser.api.IWebBrowserSupport;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.IWaitFor;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;
import com.novayre.jidoka.client.api.exceptions.JidokaItemException;
import com.novayre.jidoka.client.api.multios.EClientShowWindowType;
import com.novayre.jidoka.client.api.multios.IClient;

/**
 * This utility let the robot manage a web browser (Chrome, Firefox and
 * IExplorer are supported). Your process should include an instance for each
 * browser window involved in the robot actions.
 */
public class BrowserManager {

	/** Browser module instance */
	protected IWebBrowserSupport browser;

	/** Client module instance */
	private IClient client;

	/** WaitFor instance */
	private IWaitFor waitFor;

	/** Selected browser */
	private EBrowsers selectedBrowser;

	/** SelectorsManager instance */
	private SelectorsManager selectorsManager;

	/**
	 * BrowserManager constructor
	 * 
	 * @param robot           IRobot instance
	 * @param selectedBrowser Browser to initialize
	 */
	public BrowserManager(EBrowsers selectedBrowser) {

		IRobot robot = IRobot.getDummyInstance();

		client = IClient.getInstance(robot);
		waitFor = client.waitFor(robot);

		browser = IWebBrowserSupport.getInstance(robot, client);
		browser.setTimeoutSeconds(120);
		selectorsManager = new SelectorsManager();

		if (selectedBrowser == null) {
			throw new JidokaFatalException("You must select the browser to open");
		}

		this.selectedBrowser = selectedBrowser;

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

			// Pick the as default browser
			browser.setBrowserType(this.selectedBrowser);

			// init browser
			browser.initBrowser();

			// wait browser complete
			waitFor.window(getBrowserWindowTitle());

		} catch (Exception e) {
			throw new JidokaFatalException("Error initializing the browser: " + e.getMessage(), e);
		}
	}

	/**
	 * Navigates to the given {@code url} . After that, the browser window is
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
	 * Get the selected browser window title
	 * 
	 * @return The browser window tittle
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
			if (browser.getDriver() == null) {
				forceBrowserProcessKill();
			}
			browser = null;

		} catch (NoSuchSessionException | IOException e) {
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
		case INTERNET_EXPLORER:
			client.killAllProcesses("IEDriverServer.exe", 1000);
		case FIREFOX:
			client.killAllProcesses("geckodriver.exe", 1000);
		default:
			break;
		}
	}

	/**
	 * Get the {@link SelectorsManager} instance
	 * 
	 * @return The {@link SelectorsManager} instance
	 */
	public SelectorsManager getSelectorsManager() {
		return this.selectorsManager;
	}
}

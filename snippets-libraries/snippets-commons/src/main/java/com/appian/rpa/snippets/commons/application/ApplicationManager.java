package com.appian.rpa.snippets.commons.application;

import java.nio.file.Paths;

import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.IWaitFor;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;
import com.novayre.jidoka.client.api.multios.EClientShowWindowType;
import com.novayre.jidoka.client.api.multios.IClient;

import mmarquee.automation.UIAutomation;
import mmarquee.automation.controls.Application;

/**
 * Class to manage the actions refered to a desktop application
 *
 */
public class ApplicationManager {

	/** Client module */
	private IClient client;

	/** IWaitFor instance */
	private IWaitFor waitFor;

	/** IJidokaServer instance */
	private IJidokaServer<?> server;

	/** UIAutomation module */
	private UIAutomation automation;

	/** Application launcher name */
	private String appLauncher;

	/** Application relative directory */
	private String appDir;

	/** Application window title regex */
	private String windowTittle;

	/** Application */
	private Application application;

	/**
	 * ApplicationManager constructor
	 * 
	 * @param robot        IRobot instance
	 * @param appLauncher  App launcher name
	 * @param appDir       App launcher directory
	 * @param windowTittle App window title regex
	 * 
	 */
	public ApplicationManager(IRobot robot, String appLauncher, String appDir, String windowTittle) {

		this.server = JidokaFactory.getServer();
		this.client = IClient.getInstance(robot);
		this.waitFor = client.waitFor(robot);
		this.automation = UIAutomation.getInstance();

		this.appLauncher = appLauncher;
		this.appDir = appDir;
		this.windowTittle = windowTittle;

	}

	/**
	 * Starts the application and wait until the window opens.
	 */
	public void startApplication() throws JidokaFatalException {

		try {
			// Get the App executable path
			String execPath = Paths.get(appDir, appLauncher).toString();

			// Init application
			application = automation.launchOrAttach(execPath);
			application.waitForInputIdle(30);

			// wait app opened
			waitFor.window(this.windowTittle);

			// Send a screenshot to the console
			server.sendScreen("Applicacion started");
		} catch (Exception e) {
			throw new JidokaFatalException(
					"An error appeared while attempting to open the application " + e.getMessage(), e);
		}
	}

	/**
	 * Close the application
	 */
	public void closeApp() throws JidokaFatalException {

		try {
			this.activateWindow();
			client.typeText(client.keyboardSequence().typeAltF(4));
		} catch (Exception e) {
			throw new JidokaFatalException("Error while closing the application", e);
		}
	}

	/**
	 * Activate the window and shows it. Waits until the window is active.
	 */
	public void activateWindow() throws JidokaFatalException {
		try {
			// Activate the Application
			client.activateWindow(this.windowTittle);
			client.showWindow(client.getWindow(this.windowTittle).getId(), EClientShowWindowType.SHOW);

			// Wait to the window to activate
			waitFor.windowActive(this.windowTittle);
		} catch (Exception e) {
			throw new JidokaFatalException(
					"An unexpected error appeared while attempting to activate the application window as foreground element",
					e);
		}
	}

	/**
	 * 
	 * @return application
	 */
	public Application getApplication() {
		return application;
	}

}

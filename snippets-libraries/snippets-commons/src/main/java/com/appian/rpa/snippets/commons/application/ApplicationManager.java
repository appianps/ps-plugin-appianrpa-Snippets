package com.appian.rpa.snippets.commons.application;

import java.io.IOException;
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
	
	/** Application window title regex*/
	private String regexAppWindowTitle;

	/** 
	 * ApplicationManager constructor 
	 * 
	 * @param robot IRobot instance
	 * @param appLauncher App launcher name
	 * @param appDir App launcher directory
	 * @param regexAppWindowTitle App window title regex
	 * 
	 */
	public ApplicationManager(IRobot robot, String appLauncher, String appDir, String windowTitle) {

		this.server = JidokaFactory.getServer();
		this.client = IClient.getInstance(robot);
		this.waitFor = client.waitFor(robot);
		this.automation = UIAutomation.getInstance();

		this.appLauncher = appLauncher;
		this.appDir = appDir;
		this.regexAppWindowTitle = windowTitle;

	}

	/**
	 * Starts the application and wait until the window opens.
	 */
	public void startApplication() {

		try {
			// Get the App executable path
			String execPath = Paths.get(this.appDir, this.appLauncher).toString();

			// Init application
			Application app = automation.launchOrAttach(execPath);
			app.waitForInputIdle(30);

			// wait app opened
			waitFor.window(this.regexAppWindowTitle);

			// Send a screenshot to the console
			server.sendScreen("Applicacion started");
		} catch (Exception e) {
			throw new JidokaFatalException("Error initializing the APP: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Close the application
	 */
	public void closeApp() {

		try {
			// Close the application instances
			client.killProcess(this.appLauncher);
		} catch (IOException e) {
			throw new JidokaFatalException("Error closing the application", e);
		}

	}

	/**
	 * Activate the window and shows it.
	 * Waits until the window is active.
	 */
	public void activateWindow() {

		// Activate the Nasdaq Manager Application
		client.activateWindow(this.regexAppWindowTitle);
		client.showWindow(client.getWindow(this.regexAppWindowTitle).getId(),
				EClientShowWindowType.SHOW);

		// Wait to the window to activate
		waitFor.windowActive(this.regexAppWindowTitle);
	}

}

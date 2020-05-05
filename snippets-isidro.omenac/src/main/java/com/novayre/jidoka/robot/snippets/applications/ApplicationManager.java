package com.novayre.jidoka.robot.snippets.applications;

import java.nio.file.Paths;
import java.util.regex.Pattern;

import com.appian.robot.demo.nasdaq.automation.StockApplicationConstants;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IKeyboard;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.IWaitFor;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;
import com.novayre.jidoka.client.api.multios.IClient;

import mmarquee.automation.UIAutomation;
import mmarquee.automation.controls.Application;
import mmarquee.automation.controls.Window;

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

	/** Application name */
	private String appName;

	/** Application relative directory */
	private String appDir;

	public ApplicationManager(IRobot robot, String appName, String appDir) {

		this.server = JidokaFactory.getServer();
		this.client = IClient.getInstance(robot);
		this.waitFor = client.waitFor(robot);
		this.automation = UIAutomation.getInstance();

		this.appName = appName;
		this.appDir = appDir;

	}

	public void startApplication() {

		try {
			// Get the App executable path
			String execPath = Paths.get(server.getCurrentDir(), appDir, appName).toString();

			// Init application
			Application app = automation.launchOrAttach(execPath);
			app.waitForInputIdle(30);

			// wait app opened
			waitFor.window(StockApplicationConstants.Titles.APP_MAIN_WINDOW_TITLE);

			// Get the app window
			appWindow = automation
					.getDesktopWindow(Pattern.compile(StockApplicationConstants.Titles.APP_MAIN_WINDOW_TITLE));

			// Send a screenshot to the console
			server.sendScreen("Applicacion started");
		} catch (Exception e) {
			throw new JidokaFatalException("Error initializing the APP: " + e.getMessage(), e);
		}
	}

}

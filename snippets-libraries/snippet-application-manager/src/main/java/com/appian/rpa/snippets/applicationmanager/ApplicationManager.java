package com.appian.rpa.snippets.applicationmanager;

import java.nio.file.Paths;
import java.util.regex.Pattern;

import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.IWaitFor;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;
import com.novayre.jidoka.client.api.multios.EClientShowWindowType;
import com.novayre.jidoka.client.api.multios.IClient;
import mmarquee.automation.UIAutomation;
import mmarquee.automation.controls.Application;
import mmarquee.automation.controls.Window;

/**
 * Class to manage the actions referred to a desktop application
 *
 */
public class ApplicationManager {

	/** Client module */
	private IClient client;

	/** IWaitFor instance */
	private IWaitFor waitFor;

	/** UIAutomation module */
	private UIAutomation automation;

	/** Application launcher name */
	private String appLauncher;

	/** Application relative directory */
	private String appDir;

	/** Application window title regex */
	private String windowTittleRegex;

	/** Application */
	private Application application;

	/** Application window */
	private Window window;

	/**
	 * ApplicationManager constructor
	 * 
	 * @param robot             IRobot instance (i.i. this)
	 * @param appLauncher       App launcher name (i.e. "calc.exe")
	 * @param appDir            App launcher directory (i.e.
	 *                          "C:\\Windows\\system32\\")
	 * @param windowTittleRegex App window title regex (i.e. ".*Calculator.*")
	 * 
	 */
	public ApplicationManager(String appLauncher, String appDir, String windowTittleRegex) {

		IRobot robot = IRobot.getDummyInstance();

		this.client = IClient.getInstance(robot);
		this.waitFor = client.waitFor(robot);
		this.automation = UIAutomation.getInstance();

		this.appLauncher = appLauncher;
		this.appDir = appDir;
		this.windowTittleRegex = windowTittleRegex;

	}

	/**
	 * Starts the application and wait until the window opens.
	 */
	public void startApplication() {

		try {
			// Get the App executable path
			String execPath = Paths.get(appDir, appLauncher).toString();

			// Init application
			application = automation.launchOrAttach(execPath);
			application.waitForInputIdle(30);

			// Wait for the application window to be opened
			waitFor.window(this.windowTittleRegex);

			window = automation.getDesktopWindow(Pattern.compile(windowTittleRegex));

		} catch (Exception e) {
			throw new JidokaFatalException(
					"An error appeared while attempting to open the application " + e.getMessage(), e);
		}
	}

	/**
	 * Close the application with a backup way in case the first one doesn't work
	 */
	public void closeApp() {

		try {
			if (window != null) {
				window.close();
				window = null;
				boolean closed = client.waitCondition(15, 1000, "Closing window " + windowTittleRegex, null, false,
						false, (i, t) -> client.getWindow(windowTittleRegex) == null);
				if (!closed && application != null) {
					application.close(Pattern.compile(windowTittleRegex));

					closed = client.waitCondition(15, 1000, "Closing window " + windowTittleRegex, null, false, false,
							(i, t) -> client.getWindow(windowTittleRegex) == null);
					if (!closed) {
						throw new JidokaFatalException("Can't close the window " + windowTittleRegex);
					}
				}
			}
		} catch (

		Exception e) {
			throw new JidokaFatalException("Error while closing the application", e);
		}
	}

	/**
	 * Activate the window and shows it. Waits until the window is active.
	 */
	public void activateWindow() {
		try {
			// Activate the Application
			// Focus on app and activate the window on client module
			client.activateWindow(this.windowTittleRegex);

			client.showWindow(client.getWindow(this.windowTittleRegex).getId(), EClientShowWindowType.SHOW);
			
			// Wait to the window to activate
			waitFor.windowActive(this.windowTittleRegex);
		} catch (Exception e) {
			throw new JidokaFatalException(
					"An unexpected error appeared while attempting to activate the application window as foreground element",
					e);
		}
	}

	/**
	 * The Application object is retrieved through this method so the robot has easy
	 * access to all its functionalities.
	 * 
	 * @return application object
	 */
	public Application getApplication() {
		return application;
	}

	/**
	 * The Application object is retrieved through this method so the robot has easy
	 * access to all its functionalities.
	 * 
	 * @return window object
	 */
	public Window getWindow() {
		return window;
	}

}

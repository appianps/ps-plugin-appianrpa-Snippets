package com.appian.rpa.snippets.examples.applicationmanager;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.appian.rpa.snippets.applicationmanager.ApplicationManager;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.annotations.Robot;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;

/**
 * The Application Manager Robot provides a complete usage example from all the
 * correspondent snippet methods. First of all, the notepad editor is opened and
 * maximized. After that, the Windows native calculator application is opened,
 * and then, the previous opened notepad is set as foreground application.
 * Finally, both applicationes are closed. An exception will be thrown if
 * something goes wrong during any of these actions.
 * 
 * 
 */
@Robot
public class ApplicationManagerRobot implements IRobot {

	/** Manager for the calculator app */
	private ApplicationManager calculatorApp;

	/** Manager for the notepad app */
	private ApplicationManager notepadApp;

	/** Jidoka server */
	private IJidokaServer<?> server;

	/**
	 * Override startup method to initialise some variables involved in our process.
	 */
	@Override
	public boolean startUp() throws Exception {

		server = JidokaFactory.getServer();

		return true;
	}

	/**
	 * Initialize the calculator and notepad applications.
	 */

	public void start() {

		calculatorApp = new ApplicationManager(this, "calc.exe", "C:\\Windows\\system32\\", ".*Calculator.*");
		notepadApp = new ApplicationManager(this, "notepad.exe", "C:\\Windows\\system32\\", ".*Notepad.*");
	}

	/**
	 * Open Calculator application
	 */
	public void openCalculator() {

		calculatorApp.startApplication();
	}

	/**
	 * Open the Notepad application and Maximize its window just to show how to
	 * retrieve the Window object to work with the application
	 */
	public void openNotepad() {

		notepadApp.startApplication();

		try {
			notepadApp.getWindow().maximize();
		} catch (Exception e) {
			throw new JidokaFatalException("Error maximizing the notepad window", e);
		}
	}

	/**
	 * Set the Calculator application window as foreground/active window
	 */
	public void setCalculatorAsForegroundApp() {

		calculatorApp.activateWindow();
	}

	/**
	 * Close the Notepad application
	 */
	public void closeNotepad() {

		notepadApp.closeApp();
	}

	/**
	 * Close the Calculator application
	 */
	public void closeCalculator() {

		calculatorApp.closeApp();
	}

	/**
	 * This is the last action from the robot workflow.
	 */
	public void end() {

		server.info("The robot execution ended succesfuly");
	}

	/**
	 * Very simple manage exception
	 */
	@Override
	public String manageException(String action, Exception exception) throws Exception {

		// We get the message of the exception
		String errorMessage = ExceptionUtils.getRootCause(exception).getMessage();

		// Send a screenshot to the log so the user can see the screen in the moment
		// of the error. This is a very useful thing to do
		server.sendScreen("Screenshot of the error");

		// If we have a FatalException we should abort the execution.
		if (ExceptionUtils.indexOfThrowable(exception, JidokaFatalException.class) >= 0) {

			server.error(StringUtils.isBlank(errorMessage) ? "Fatal error" : errorMessage);
			return IRobot.super.manageException(action, exception);
		}

		server.warn("Unknown exception!");

		// If we have any other exception we must abort the execution, we don't know
		// what has happened

		return IRobot.super.manageException(action, exception);
	}

	/**
	 * Overrides the cleanUp method.
	 * 
	 * We ensure that all the applications involved have been successfully closed,
	 * even if an exception was thrown during the process. This is a common practice
	 * to avoid undesired opened tasks in the following executions.
	 */

	@Override
	public String[] cleanUp() throws Exception {

		closeNotepad();
		closeCalculator();

		return IRobot.super.cleanUp();
	}

}

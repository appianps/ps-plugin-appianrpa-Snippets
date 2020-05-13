package com.appian.rpa.snippets.examples.robot.applicationmanager;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.appian.rpa.snippets.commons.application.ApplicationManager;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.annotations.Robot;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;

/**
 * Application Manager Robot provides a complete usage example from all the
 * correspondent snippet methods. It basically opens a blank notepad, maximise the window, 
 * then opens the calculator, and then set the blank notepad as foreground application. 
 * 
 * After these actions, all the applications are closed if nothing went wrong during
 * the process.
 * 
 */
@Robot
public class ApplicationManagerRobot implements IRobot {

	private ApplicationManager calculatorApp;
	private ApplicationManager notepadApp;
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
	 * Initialise the calculator and notepad apps.
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
	 * Open Notepad application and Maximises its window just to show how to retrieve the Window object for working with the application
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
	 * Set the Calculator app window as foreground
	 */
	public void setCalculatorAsForegroundApp() {
		
		calculatorApp.activateWindow();
	}
	
	/**
	 * Close Notepad application
	 */
	public void closeNotepad() {

		notepadApp.closeApp();
	}

	/**
	 * Close Calculator application
	 */
	public void closeCalculator() {
		
		calculatorApp.closeApp();
	}

	/**
	 * This is the last action from robot workflow.
	 */
	public void end() {
		
		server.info("Robots ends succesfuly");
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
	 * We ensure that the applications are been closed even if there was an exception through the process. 
	 * This is considered a good practice
	 */
	@Override
	public String[] cleanUp() throws Exception {
		
		closeNotepad();
		closeCalculator();

		return IRobot.super.cleanUp();
	}

}

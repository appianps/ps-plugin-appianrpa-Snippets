package com.appian.rpa.robot.applications;

import java.io.Serializable;

import com.appian.rpa.snippets.commons.application.ApplicationManager;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.annotations.Robot;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;
import com.novayre.jidoka.client.api.multios.IClient;

/**
 * Application Manager Robot provides a complete usage example from all the
 * correspondent snippet methods. It basically opens a blank notepad, the
 * calculator, and then set the blank notepad as foreground application. After
 * these actions, all the applications are closed if nothing went wrong during
 * the process
 * 
 * @author javier.advani
 *
 */
@Robot
public class ApplicationManagerRobot implements IRobot {

	private ApplicationManager calculatorApp;
	private ApplicationManager notepadApp;
	private IJidokaServer<Serializable> server;
	private IClient client;

	/**
	 * Override startup method to initialize some variables involved in our process.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public boolean startUp() throws Exception {
		server = (IJidokaServer<Serializable>) JidokaFactory.getServer();
		client = IClient.getInstance(this);
		return true;

	}

	/**
	 * Initialize the calculator and notepad apps.
	 */

	public void start() {
		calculatorApp = new ApplicationManager(this, "calc.exe", "C:\\Windows\\system32\\", ".*Calculator.*");
		notepadApp = new ApplicationManager(this, "notepad.exe", "C:\\Windows\\system32", ".*Notepad.*");
	}

	/**
	 * Open Calculator application
	 */
	public void openCalculator() {

		try {
			calculatorApp.startApplication();
		} catch (Exception e) {
			throw new JidokaFatalException("Error starting the Calculator");
		}
	}

	/**
	 * Open Notepad application
	 */
	public void openNotepad() {

		try {
			notepadApp.startApplication();
		} catch (Exception e) {
			throw new JidokaFatalException("Error starting the Notepad");
		}
	}

	public void setCalculatorAsForegroundApp() {
		calculatorApp.activateWindow();
		client.characterPause(2000);
	}

	/**
	 * Close Notepad application
	 */
	public void closeNotepad() {
		try {
			notepadApp.closeApp();
		} catch (Exception e) {
			throw new JidokaFatalException("An error appeared while closing the Notepad");
		}
	}

	/**
	 * Close Calculator application
	 * 
	 * @throws InterruptedException
	 */
	public void closeCalculator() {
		try {
			client.characterPause(2000);
			calculatorApp.closeApp();
		} catch (Exception e) {
			throw new JidokaFatalException("An error appeared while closing the Calculator");
		}
	}

	/**
	 * This is the last action from robot workflow.
	 */
	public void end() {
		server.info("end");
	}

	/**
	 * Override from the cleanUp method
	 */
	@Override
	public String[] cleanUp() throws Exception {

		return IRobot.super.cleanUp();
	}

}

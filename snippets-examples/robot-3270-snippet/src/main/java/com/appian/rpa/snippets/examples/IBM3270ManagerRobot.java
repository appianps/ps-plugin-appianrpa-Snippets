package com.appian.rpa.snippets.examples;

import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.appian.rpa.snippet.ibm3270.IBM3270AppManager;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.annotations.Robot;
import com.novayre.jidoka.client.api.exceptions.JidokaException;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;
import com.novayre.jidoka.client.api.multios.IClient;

/**
 * IBM3270 Manager Robot provides a complete usage example from all the
 * correspondent snippet methods. It opens an IBM3270 terminal using the
 * emulator indicated as the robot's parameter, goes to a menu page and tries to
 * change the user's password.
 * 
 * Finally the robot closes the emulator.
 */
@Robot
public class IBM3270ManagerRobot implements IRobot {

	/** Jidoka server instance */
	private IJidokaServer<Serializable> server;

	/** Client Module instance */
	protected IClient client;

	IBM3270AppManager intanceIBM3270AppManager;
	

	/**
	 * Startup method. This <code>startUp</code> method is called prior to calling
	 * the first workflow method defined
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean startUp() throws Exception {

		server = (IJidokaServer<Serializable>) JidokaFactory.getServer();
		client = IClient.getInstance(this);

		return true;
	}

	/**
	 * Action 'Init'.
	 * <p>
	 * Initializes Jidoka modules. Instances of the emulator type passed as a
	 * parameter are loaded
	 */
	public void init() {

		try {
			server.debug("Robot initialized");
			
		} catch (Exception e) {
			throw new JidokaFatalException("Error initializing");
		}
	}

	/**
	 * Action 'Open 3270' terminal
	 */
	public void open3270() {
		server.info("Opening 3270 terminal");
		intanceIBM3270AppManager = IBM3270AppManager.openIBM3270("C:\\ProgramData\\IBM\\Personal Communications\\PRE-Santander-Appian.ws", ".*VIPADPRE.*" , this, "PCOMM");
		client.pause(1000);
		server.sendScreen("Screenshot after opening the terminal");
	}

	/**
	 * Go to the page indicated as parameter
	 * 
	 * @param page Name of the page in the menu
	 * @throws JidokaException
	 */
	public void login() throws JidokaException {

		intanceIBM3270AppManager.write("==>", 4, 0, "M", 3);		
		intanceIBM3270AppManager.control();
		client.pause(1000);
		server.sendScreen("Screenshot Access Menu");
		intanceIBM3270AppManager.write("==>", 4, 0, "3", 3);		
		intanceIBM3270AppManager.control();
		server.sendScreen("Screenshot Login");
		client.pause(10000);
		server.debug("Existe texto? " + intanceIBM3270AppManager.existText("TSO DE SAQS"));
	}
	
	/**
	 * Action 'Go to NetView' page.
	 * 
	 * @throws JidokaException
	 */
	public void goToNetView() throws JidokaException {

		server.sendScreen("Screenshot before moving to NetView page");

		//currentPage = ((WelcomePage) currentPage).goToPage("NETVIEW");

		server.sendScreen("Moved to NetView page");
	}

	/**
	 * Action 'Change password'.
	 * 
	 * @throws JidokaException
	 */
	public void changePassword() throws JidokaException {

		server.info("Change NetView Password");

	/*	ChangePwdPage changePwd = ((NetViewPage) currentPage).goToChangePasswordPage();

		currentPage = changePwd.changeOperatorPassword();
		*/
	}

	/**
	 * Action 'Close 3270'.
	 * @throws IOException 
	 */
	public void close3270() throws IOException {

		server.info("Closing IBM3270 terminal");

		intanceIBM3270AppManager.close();
	}

	/**
	 * Action 'End'.
	 */
	public void end() {

		// continue the process, here the robot ends its execution
	}

	/**
	 * Clean up.
	 *
	 * @return the string[]
	 * @throws Exception the exception
	 * @see com.novayre.jidoka.client.api.IRobot#cleanUp()
	 */
	@Override
	public String[] cleanUp() throws Exception {
		if (intanceIBM3270AppManager!=null) {
			intanceIBM3270AppManager.close();
		}
		return IRobot.super.cleanUp();
	}

	/**
	 * Manage exception.
	 *
	 * @param action    action where the original exception was thrown
	 * @param exception original exception
	 * @return the action where the robot will continue its execution
	 * @throws Exception the exception
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

}

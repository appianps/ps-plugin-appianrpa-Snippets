package com.appian.rpa.snippets.examples;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.appian.rpa.snippet.IBM3270Commons;
import com.appian.rpa.snippet.clients.PCOMMEmulatorManager;
import com.appian.rpa.snippet.clients.WC3270EmulatorManager;
import com.appian.rpa.snippet.page.IBM3270Page;
import com.appian.rpa.snippets.examples.commons.IBM3270AppManager;
import com.appian.rpa.snippets.examples.pages.ChangePwdPage;
import com.appian.rpa.snippets.examples.pages.NetViewPage;
import com.appian.rpa.snippets.examples.pages.WelcomePage;
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

	/** IBM3270Commons snippet instance */
	private IBM3270Commons commons;

	/** IBM3270AppManager instance */
	private IBM3270AppManager appManager;

	/** Current screen */
	public IBM3270Page currentPage;

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

			String emulator = server.getParameters().get("Emulator");

			if (emulator.equals("wc3270")) {
				commons = new WC3270EmulatorManager(this);
			} else {
				commons = new PCOMMEmulatorManager(this);
			}

			appManager = new IBM3270AppManager(commons);

			server.debug("Robot initialized");
		} catch (Exception e) {
			throw new JidokaFatalException("Error initializing");
		}
	}

	/**
	 * Action 'Open 3270' terminal
	 */
	public void open3270() {

		try {

			server.info("Opening 3270 terminal");

			currentPage = appManager.openIBM3270();

			client.pause(1000);

			server.sendScreen("Screenshot after opening the terminal");

		} catch (JidokaException e) {

			throw new JidokaFatalException("Error opening the terminal");
		}

	}

	/**
	 * Action 'Go to NetView' page.
	 * 
	 * @throws JidokaException
	 */
	public void goToNetView() throws JidokaException {

		server.sendScreen("Screenshot before moving to NetView page");

		currentPage = ((WelcomePage) currentPage).goToPage("NETVIEW");

		server.sendScreen("Moved to NetView page");
	}

	/**
	 * Action 'Change password'.
	 * 
	 * @throws JidokaException
	 */
	public void changePassword() throws JidokaException {

		server.info("Change NetView Password");

		ChangePwdPage changePwd = ((NetViewPage) currentPage).goToChangePasswordPage();

		currentPage = changePwd.changeOperatorPassword();
	}

	/**
	 * Action 'Close 3270'.
	 */
	public void close3270() {

		server.info("Closing IBM3270 terminal");

		appManager.closeIBM3270(commons.getWindowTitleRegex());
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
		appManager.closeIBM3270(commons.getWindowTitleRegex());
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

package com.appian.robot.demo;

import java.io.Serializable;

import com.appian.robot.demo.commons.IBM3270AppManager;
import com.appian.robot.demo.pages.NetViewPage;
import com.appian.robot.demo.pages.WelcomePage;
import com.appian.rpa.snippet.IBM3270Commons;
import com.appian.rpa.snippet.clients.PCOMMEmulatorManager;
import com.appian.rpa.snippet.clients.WC3270EmulatorManager;
import com.appian.rpa.snippet.page.IBM3270Page;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.annotations.Robot;
import com.novayre.jidoka.client.api.exceptions.JidokaException;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;
import com.novayre.jidoka.client.api.multios.IClient;

/**
 * Robot for managing 3270 terminals
 */
@Robot
public class Robot3270 implements IRobot {

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

			appManager = new IBM3270AppManager(this, emulator);

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

			currentPage = appManager.openIBM3270(commons);

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

		((NetViewPage) currentPage).changeOperatorPassword();
	}

	/**
	 * Action 'Close 3270'.
	 */
	public void close3270() {
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
	 * @param action    the action
	 * @param exception the exception
	 * @return the string
	 * @throws Exception the exception
	 */
	@Override
	public String manageException(String action, Exception exception) throws Exception {

		return IRobot.super.manageException(action, exception);
	}

}

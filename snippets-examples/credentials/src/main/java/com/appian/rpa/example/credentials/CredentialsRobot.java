package com.appian.rpa.example.credentials;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import com.appian.rpa.snippets.commons.credentials.CredentialsUtils;
import com.novayre.jidoka.client.api.ECredentialSearch;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.annotations.Robot;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;
import com.novayre.jidoka.client.api.exceptions.JidokaItemException;
import com.novayre.jidoka.client.api.execution.IUsernamePassword;

@Robot
public class CredentialsRobot implements IRobot {

	/** IJidokaServer instance */
	private IJidokaServer<?> server;

	/** Credentials utils instance */
	private CredentialsUtils credentialsUtils;

	/** The current item index. */
	private int currentItemIndex;

	/** Number of iterations */
	private static final int NUMBER_OF_LOOPS = 5;

	/** Default timeout seconds to wait for the crdentials */
	private static final int DEFAULT_TIMEOUT = 25;

	/** Credentials application name */
	private static final String APPLICATION_NAME = "TEST_ROBOT";

	/** Credentials 1 */
	private IUsernamePassword credentials1;

	/** Credentials 2 */
	private IUsernamePassword credentials2;

	/** Credentials 3 */
	private IUsernamePassword credentials3;

	@Override
	public boolean startUp() throws Exception {
		// Init server module
		this.server = JidokaFactory.getServer();

		// CredentialsUtils init
		this.credentialsUtils = CredentialsUtils.getInstance(this);

		return IRobot.super.startUp();
	}

	/**
	 * Inits modules and global variabless
	 */
	public void init() {
		// Init actions
		server.setNumberOfItems(NUMBER_OF_LOOPS);
	}

	/**
	 * Logins to the chosen application using the necessary credentials, either
	 * reserving it or not
	 */
	public void loginApp() {

		try {
			// First, reserves or gets the credentials needed. In this case, we are going to
			// get/reserve 3 credentials.
			// Gets the first credentials and reserve it, getting the first listed one
			credentials1 = credentialsUtils.getCredentials(APPLICATION_NAME, true, ECredentialSearch.FIRST_LISTED,
					DEFAULT_TIMEOUT);

			// Then it gets the second credentials, searching them by user without reserving
			// it
			credentials2 = credentialsUtils.getCredentialsByUser(APPLICATION_NAME, "test2", false, DEFAULT_TIMEOUT);

			// Finally it gets the third credentials, searching them by user and reserving
			// it
			// Then it gets the second credentials, searching them by user without reserving
			// it
			credentials3 = credentialsUtils.getCredentialsByUser(APPLICATION_NAME, "test3", true, DEFAULT_TIMEOUT);

			// Here we should do the required login actions on the app
			// As this is a blank robot, we are going to show the credentials in the log.
			server.info(String.format("Using the credentials 1 with USER: %s and PASSWORD: %s",
					credentials1.getUsername(), credentials1.getPassword()));

			server.info(String.format("Using the credentials 2 with USER: %s and PASSWORD: %s",
					credentials2.getUsername(), credentials2.getPassword()));

			server.info(String.format("Using the credentials 3 with USER: %s and PASSWORD: %s",
					credentials3.getUsername(), credentials3.getPassword()));
		} catch (Exception e) {
			throw new JidokaFatalException("Error logging into the app", e);
		}

	}

	/**
	 * Main loop where the required actions on the app are made
	 */
	public void doActions() {

		try {
			// In this loop, we must put the main actions on the app
			server.info("Processing Action" + currentItemIndex);
			server.setCurrentItemResultToOK();
		} catch (Exception e) {
			throw new JidokaItemException("Error performing the actions");
		}

	}

	/**
	 * Checks if there are more pending items
	 */
	public String hasMoreItems() {
		try {

			if (currentItemIndex < NUMBER_OF_LOOPS) {

				// set the stats for the current item
				currentItemIndex++;
				server.setCurrentItem(currentItemIndex, "Action" + currentItemIndex);

				return "yes";
			}

			return "no";

		} catch (Exception e) {
			throw new JidokaFatalException("Not possible to evaluate item", e);
		}
	}

	/**
	 * Closes the app and release the used credentials
	 */
	public void closeApp() {

		try {
			// It closes the app and releases the credentials if were reserved. We are going
			// to release the credentials1 and then release the others on the cleanUp
			credentialsUtils.releaseCredentials(APPLICATION_NAME, credentials1.getUsername());
		} catch (Exception e) {
			throw new JidokaFatalException("Error closing the app", e);
		}
	}

	/**
	 * Action 'End'.
	 */
	public void end() {

		// continue the process, here the robot ends its execution

	}

	@Override
	public String[] cleanUp() throws Exception {

		credentialsUtils.releaseAllCredentials(APPLICATION_NAME);

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

		// We get the message of the exception
		String errorMessage = ExceptionUtils.getRootCause(exception).getMessage();

		// We send a screenshot to the log so the user can see the screen in the moment
		// of the error
		// This is a very useful thing to do
		server.sendScreen("Screenshot at the moment of the error");

		// If we have a FatalException we should abort the execution.
		if (ExceptionUtils.indexOfThrowable(exception, JidokaFatalException.class) >= 0) {

			server.error(StringUtils.isBlank(errorMessage) ? "Fatal error" : errorMessage);
			return IRobot.super.manageException(action, exception);
		}

		// If the error is processing one items we must mark it as a warning and go on
		// with the next item
		if (ExceptionUtils.indexOfThrowable(exception, JidokaItemException.class) >= 0) {

			server.setCurrentItemResultToWarn(errorMessage);
			reset();
			return "hasMoreItems";
		}

		server.warn("Unknown exception!");

		// If we have any other exception we must abort the execution, we don't know
		// what has happened

		return IRobot.super.manageException(action, exception);
	}

	/**
	 * This method reset the robot state to a stable state after a
	 * JidokaItemException is thrown
	 */
	private void reset() {
		// In this case the method is empty because
		// the robot loop reset always to a stable state.
	}

}

package com.appian.rpa.snippets.examples.credentials;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import com.appian.rpa.snippets.credentials.CredentialsUtils;
import com.novayre.jidoka.client.api.ECredentialSearch;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.annotations.Robot;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;
import com.novayre.jidoka.client.api.exceptions.JidokaItemException;
import com.novayre.jidoka.client.api.execution.IUsernamePassword;

/**
 * 
 * The Credentials Robot provides a complete usage example from all the
 * CredentialsUtils snippet methods. The robot will retrieve three existent
 * credentials (Username + Password) associated to the application "TEST_ROBOT".
 * In case that these three credentials were not previously created in the
 * console side, the application will throw an exception.
 */

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

	/** Credentials application name 1 */
	private static final String APPLICATION_NAME1 = "TEST_ROBOT1";

	/** Credentials application name 2 */
	private static final String APPLICATION_NAME2 = "TEST_ROBOT2";

	/** Credentials 1 */
	private IUsernamePassword credentials1;

	/** Credentials 2 */
	private IUsernamePassword credentials2;

	/** Credentials 3 */
	private IUsernamePassword credentials3;

	/**
	 * Overrides the startup method to initialize some variables involved in our
	 * process.
	 */
	@Override
	public boolean startUp() throws Exception {
		// Init server module
		server = JidokaFactory.getServer();
		credentialsUtils = new CredentialsUtils();

		return IRobot.super.startUp();
	}

	/**
	 * The start method initializes modules and global variables
	 */
	public void start() {
		server.setNumberOfItems(NUMBER_OF_LOOPS);
	}

	/**
	 * This method retrieves the three credentials associated to the same TEST_ROBOT
	 * application, whether the number of usages is limited or not. A
	 * JidokaFatalException is thrown in case that the number of retrievals was
	 * already exceeded or simply the desired credential does not exist.
	 */
	public void retrieveCredentialsFromConsole() {

		try {
			// First, reserves or gets the credentials needed. In this case, we are going to
			// get/reserve 3 credentials.
			// Gets the first credentials and reserve it, getting the first listed one
			credentials1 = credentialsUtils.getCredential(APPLICATION_NAME1, true, ECredentialSearch.FIRST_LISTED,
					DEFAULT_TIMEOUT);

			// Then it gets the second credentials, searching them by user without reserving
			// it
			credentials2 = credentialsUtils.getCredentialByUser(APPLICATION_NAME2, "test2", false, DEFAULT_TIMEOUT);

			// Finally it gets the third credentials, searching them by user and reserving
			// it
			// Then it gets the second credentials, searching them by user without reserving
			// it
			credentials3 = credentialsUtils.getCredentialByUser(APPLICATION_NAME2, "test3", true, DEFAULT_TIMEOUT);

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
	 * This method let the process confirm if each credential was successfully
	 * retrieved or not.
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
	 * Close the app and release the used credentials
	 */
	public void closeApp() {

		try {
			// It closes the app and releases the credentials if were reserved. We are going
			// to release the credentials1 and then release the others on the cleanUp
			credentialsUtils.releaseCredential(APPLICATION_NAME1, credentials1.getUsername());
		} catch (Exception e) {
			throw new JidokaFatalException("Error closing the app", e);
		}
	}

	/**
	 * Any further actions to close the robot process can be performed here.
	 */
	public void end() {

		// continue the process, here the robot ends its execution

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

		credentialsUtils.releaseAllCredentials();

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

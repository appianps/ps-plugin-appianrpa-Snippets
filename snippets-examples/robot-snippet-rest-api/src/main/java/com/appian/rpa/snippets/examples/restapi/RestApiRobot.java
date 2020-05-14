package com.appian.rpa.snippets.examples.restapi;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import com.appian.rpa.snippets.restapi.RestApiUtils;
import com.novayre.jidoka.client.api.ECredentialSearch;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.annotations.Robot;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;
import com.novayre.jidoka.client.api.execution.IUsernamePassword;

/**
 * The Rest API Robot provides a complete usage example from all the
 * correspondent snippet methods. Given a queueItem ID, this robotic process
 * updates the number of attempts to 3. It also set the status as "PENDING". An
 * error will be thrown if the Item does not exist, or the Status was not
 * previously marked as "FINISHED_WARN".
 * 
 */
@Robot
public class RestApiRobot implements IRobot {

	/**
	 * Server
	 */
	private IJidokaServer<?> server;

	/**
	 * ApiRestUtils instance
	 */
	private RestApiUtils restApiUtils;

	/**
	 * Environment variable Console url
	 */
	private static final String EV_CONSOLE_URL = "consoleUrl";

	/**
	 * Environment variable endpoint
	 */
	private static final String EV_ENDPOINT = "endpoint";

	/**
	 * Credential for the API key
	 */
	private static final String CREDENTIAL_API_KEY = "API_KEY";

	/**
	 * Item id
	 */
	private static final String ITEM_ID = "itemID";

	/**
	 * Console url
	 */
	private String consoleUrl;

	/**
	 * Rest API Endpoint
	 */
	private String endpoint;

	/**
	 * API Key
	 */
	private String apiKey;
	/**
	 * Item ID from the queue Item.
	 */
	private String itemID;

	/**
	 * Override startup method to initialise some variables involved in our process.
	 */
	@Override
	public boolean startUp() throws Exception {
		this.server = JidokaFactory.getServer();
		return IRobot.super.startUp();
	}

	/**
	 * Initialize the rest of the elements to be used during the workflow actions.
	 */

	public void start() {
		restApiUtils = RestApiUtils.getInstance();

		this.consoleUrl = server.getEnvironmentVariables().get(EV_CONSOLE_URL);
		this.endpoint = server.getEnvironmentVariables().get(EV_ENDPOINT);
		this.itemID = server.getEnvironmentVariables().get(ITEM_ID);
		// Gets the credentials
		IUsernamePassword credentials = server.getCredential(CREDENTIAL_API_KEY, false, ECredentialSearch.FIRST_LISTED);

		// Check if the credentials are null
		if (credentials == null) {
			throw new JidokaFatalException("Robot doesn't have access to any credentials");
		}

		this.apiKey = credentials.getPassword();
	}

	/**
	 * Set the number of retries to 3 for the desired queueItem.
	 * 
	 * @throws IOException
	 */

	public void resetRetryNumber() throws IOException {

		try {

			Map<String, Object> body = new HashMap<>();

			body.put("id", itemID);
			body.put("remainingAttempts", 3);

			restApiUtils.restApiCall(this.consoleUrl, this.endpoint, this.apiKey, body);

		} catch (Exception e) {
			throw new JidokaFatalException(e.getMessage(), e);
		}

	}

	/**
	 * This is the last action from the robot workflow.
	 */
	public void end() {

		server.info("The robot execution ended succesfuly");
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

		// nothing to cleanUp
		return new String[0];
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

		server.warn("Unknown exception!");

		// If we have any other exception we must abort the execution, we don't know
		// what has happened

		return IRobot.super.manageException(action, exception);
	}

}

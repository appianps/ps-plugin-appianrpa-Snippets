package com.appian.rpa.robot.RestAPI;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import com.appian.rpa.snippets.commons.restapi.RestApiUtils;
import com.novayre.jidoka.client.api.ECredentialSearch;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.annotations.Robot;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;
import com.novayre.jidoka.client.api.execution.IUsernamePassword;

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
	
	private String itemID;

	@Override
	public boolean startUp() throws Exception {
		this.server = (IJidokaServer<?>) JidokaFactory.getServer();
		return IRobot.super.startUp();
	}

	public void init() {
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

	public void end() {
		// end robot
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

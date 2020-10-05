package com.appian.rpa.snippets.examples.credentials;

import org.apache.commons.lang.StringUtils;

import com.appian.rpa.snippets.credentials.CredentialsUtils;
import com.novayre.jidoka.client.api.ECredentialSearch;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.INano;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.JidokaMethod;
import com.novayre.jidoka.client.api.JidokaParameter;
import com.novayre.jidoka.client.api.annotations.Nano;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;
import com.novayre.jidoka.client.api.execution.IUsernamePassword;

/**
 *
 * The CredentialsUtils Class provides the actions to retrieve and manage Appian
 * RPA Console Credentials.
 */

@Nano
public class CredentialsSnippetLibraries implements INano {

	/** IJidokaServer instance */
	private IJidokaServer<?> server;

	/** Credentials utils instance */
	private CredentialsUtils credentialsUtils;

	/** Number of iterations */
	private static final int NUMBER_OF_LOOPS = 5;

	/** Credentials application name */
	private static final String APPLICATION_NAME = "TEST_ROBOT";

	/** Credentials */
	private IUsernamePassword credentials;

	/**
	 * The start method initializes modules and global variables
	 */
	private void start() {
		server = JidokaFactory.getServer();
		credentialsUtils = new CredentialsUtils();
				
		server.setNumberOfItems(NUMBER_OF_LOOPS);
	}

	/**
	 * This method retrieves the three credentials associated to the same TEST_ROBOT
	 * application, whether the number of usages is limited or not. A
	 * JidokaFatalException is thrown in case that the number of retrievals was
	 * already exceeded or simply the desired credential does not exist.
	 */
	@JidokaMethod(name = "Get Credentials", description = "Get Credentials From RPA console")
	public void getCredentials(
			@JidokaParameter(defaultValue = "", name = "Credentials application name * ") String applicationName,
			@JidokaParameter(defaultValue = "", name = "Credentials username (optional)") String username,
			@JidokaParameter(defaultValue = "", name = "Reserve credentials * ") String reserveCredentials,
			@JidokaParameter(defaultValue = "25", name = "Timeout * ") String timeout) {

		// First, reserves or gets the credentials needed. In this case, we are going to
		// Gets the first credentials and reserve it, if it is requested
		try {
			this.start();
			if (StringUtils.isEmpty(username)) {
				credentials = credentialsUtils.getCredentials(APPLICATION_NAME, Boolean.parseBoolean(reserveCredentials),
						ECredentialSearch.FIRST_LISTED, Integer.parseInt(timeout));
			} else {
				credentials = credentialsUtils.getCredentialsByUser(APPLICATION_NAME, username, Boolean.parseBoolean(reserveCredentials),
						Integer.parseInt(timeout));
			}

			// Here we should do the required login actions on the app
			// As this is a blank robot, we are going to show the credentials in the log.
			server.info(String.format("Using credentials with Application name: %s and User: %s", applicationName,
					credentials.getUsername()));

		} catch (Exception e) {
			throw new JidokaFatalException("Error getting the credentials", e);
		}

	}

	/**
	 * Close the app and release the used credentials
	 */
	@JidokaMethod(name = "Release Credentials", description = "Release given credentials From RPA console if they were reserved")
	public void releaseCredentials(@JidokaParameter(defaultValue = "", name = "Credentials application name * ") String applicationName,
			@JidokaParameter(defaultValue = "", name = "Credentials username (optional)") String username,
			@JidokaParameter(defaultValue = "", name = "Release if was reserved (optional)") String release) {

		try {
			if(Boolean.parseBoolean(release)) {
				credentialsUtils.releaseCredentials(applicationName, username);
			}
			
		} catch (Exception e) {
			throw new JidokaFatalException("Error releasing the credentials", e);
		}
	}

}

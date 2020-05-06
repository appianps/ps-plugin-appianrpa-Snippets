package com.appian.rpa.snippets.commons.credentials;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.novayre.jidoka.client.api.*;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;
import com.novayre.jidoka.client.api.exceptions.JidokaUnsatisfiedConditionException;
import com.novayre.jidoka.client.api.execution.IUsernamePassword;
import com.novayre.jidoka.client.api.multios.IClient;

public class CredentialsUtils {

	/** CredentialsUtils instance */
	private static CredentialsUtils credentialsUtilsInstance;

	/** Server module */
	private IJidokaServer<?> server;

	/** Client module */
	private IClient client;

	/** WaitFor instance */
	private IWaitFor waitFor;

	/** Credentials in use */
	private Map<IUsernamePassword, Boolean> credentialsInUse = new HashMap<>();

	/**
	 * Private constructor restricted to this class itself
	 * 
	 * @param robot {@link IRobot} instance
	 */
	private CredentialsUtils(IRobot robot) {
		this.server = (IJidokaServer<?>) JidokaFactory.getServer();
		this.client = IClient.getInstance(robot);
		this.waitFor = client.getWaitFor(robot);
	}

	/**
	 * 
	 * Static method to create instance of CredentialsUtils class
	 * 
	 * @param robot {@link IRobot} instance
	 * 
	 * @return CredentialsUtils instance
	 */
	public static CredentialsUtils getInstance(IRobot robot) {
		if (credentialsUtilsInstance == null) {
			credentialsUtilsInstance = new CredentialsUtils(robot);
		}

		return credentialsUtilsInstance;
	}

	/**
	 * Gets the given credential {@code currentCredentialApplication}. It waits for
	 * the credential until the given {@code timeout} is over.
	 * 
	 * @param application    Credential application
	 * @param reserve        True if you want to reserve the credentials
	 * @param search         Algorithm to search the credential
	 * @param timeOutSeconds Maximum waiting time for the credential
	 * 
	 * @return The {@link IUsernamePassword} object of the credential
	 */
	public IUsernamePassword getCredentials(String application, Boolean reserve, ECredentialSearch search,
			Integer timeOutSeconds) {
		try {

			// This is used to avoid the java error "Local Variable
			// Defined in an Enclosing Scope Must be Final or Effectively Final"
			IUsernamePassword[] credentials = new IUsernamePassword[1];

			// Wait until the credentials are free or the timeout is over
			this.waitFor.wait(timeOutSeconds, "Waiting for the credentials", true, false, () -> {
				// Gets the credentials
				credentials[0] = server.getCredential(application, reserve, search);

				// Check if the credentials are returned
				return credentials[0] != null;
			});

			// We put the new credentials in use into the credentialsInUse map
			credentialsInUse.put(credentials[0], reserve);

			// Return the credentials
			return credentials[0];

		} catch (JidokaUnsatisfiedConditionException e) {
			throw new JidokaFatalException("Credentials not available");
		}
	}

	/**
	 * Gets the given credentials {@code currentCredentialApplication} with the
	 * given {@code userName}. It waits for the credential until the given
	 * {@code timeout} is over.
	 * 
	 * @param application    Credentials application
	 * @param userName       Username to filter by
	 * @param reserve        True if you want to reserve the credentials
	 * @param search         Algorithm to search the credential
	 * @param timeOutSeconds Maximum waiting time for the credential
	 * 
	 * @return The {@link IUsernamePassword} object of the credential
	 */
	public IUsernamePassword getCredentialsByUser(String application, String userName, Boolean reserve,
			Integer timeOutSeconds) {

		List<IUsernamePassword> credentialsList = new ArrayList<>();

		try {
			// Waits until the credentials list is retrieved
			this.waitFor.wait(timeOutSeconds, "Waiting for the credentials list", true, false, () -> {
				// Gets the credentials
				credentialsList.addAll(server.getCredentials(application));

				return !credentialsList.isEmpty();
			});
		} catch (Exception e) {
			throw new JidokaFatalException("Error retrieving the credentials list", e);
		}

		// Filter the credential by username
		IUsernamePassword credentials = credentialsList.stream().filter(c -> c.getUsername().equals(userName))
				.findFirst().orElse(null);

		try {
			// If reserve is true, it reserves the credentials
			if (reserve) {

				// Waits until the credentials are reserved
				this.waitFor.wait(timeOutSeconds, "Reserving the credentials", true, false, () -> 
					// Gets the credentials
					server.reserveCredential(application, credentials.getUsername())

				);

			}
		} catch (Exception e) {
			throw new JidokaFatalException("Error reserving the credentials", e);
		}

		// We put the new credentials in use into the credentialsInUse map
		credentialsInUse.put(credentials, reserve);

		// Return the credentials
		return credentials;
	}

	/**
	 * Releases the specified credentials
	 * 
	 * @param credentialApplication Credentials application
	 * @param userName              Username of the credentials
	 */
	public void releaseCredentials(String credentialApplication, String userName) {

		// Gets the credentials
		Entry<IUsernamePassword, Boolean> credentials = credentialsInUse.entrySet().stream()
				.filter(c -> c.getKey().getUsername().equals(userName)).findFirst().orElse(null);

		// If the credentials were reserved, it releases them
		if (credentials.getValue()) {

			server.releaseCredential(credentialApplication, userName);
			// We put the new credentials in use into the credentialsInUse map
			credentialsInUse.put(credentials.getKey(), false);

			server.info(String.format("Credentials %s with username: %s released", credentialApplication, userName));
		} else {
			server.info(String.format("Credentials %s with username: %s weren't reserved or were already released",
					credentialApplication, userName));
		}

	}

	/**
	 * Release all previously obtained credentials
	 * 
	 * @param credentialApplication Credentials application
	 */
	public void releaseAllCredentials(String credentialApplication) {

		for (Entry<IUsernamePassword, Boolean> credential : credentialsInUse.entrySet()) {
			releaseCredentials(credentialApplication, credential.getKey().getUsername());
		}
	}
}

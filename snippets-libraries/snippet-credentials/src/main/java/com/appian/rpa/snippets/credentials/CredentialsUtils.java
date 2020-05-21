package com.appian.rpa.snippets.credentials;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.novayre.jidoka.client.api.ECredentialSearch;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.IWaitFor;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;
import com.novayre.jidoka.client.api.exceptions.JidokaUnsatisfiedConditionException;
import com.novayre.jidoka.client.api.execution.IUsernamePassword;
import com.novayre.jidoka.client.api.multios.IClient;

/**
 * The CredentialsUtils Class provides the actions to retrieve and manage Appian
 * RPA Console credentials.
 *
 */
public class CredentialsUtils {

	/** Server module */
	private IJidokaServer<?> server;

	/** Client module */
	private IClient client;

	/** WaitFor instance */
	private IWaitFor waitFor;

	/** Credentials in use */
	private List<Credential> credentialsInUse = new ArrayList<>();

	/**
	 * Private constructor restricted to the class itself
	 * 
	 * @param robot {@link IRobot} instance
	 */
	public CredentialsUtils() {
		IRobot robot = IRobot.getDummyInstance();
		this.server = JidokaFactory.getServer();
		this.client = IClient.getInstance(robot);
		this.waitFor = client.getWaitFor(robot);
	}

	/**
	 * Gets the given credential {@code currentCredentialApplication}. It waits for
	 * the credential until the given {@code timeout} is over.
	 * 
	 * @param application    Credential application
	 * @param reserve        True if you want to reserve the credential
	 * @param search         Algorithm to search the credential
	 * @param timeOutSeconds Maximum waiting time for the credential
	 * 
	 * @return The {@link IUsernamePassword} object of the credential
	 */
	public IUsernamePassword getCredential(String application, Boolean reserve, ECredentialSearch search,
			Integer timeOutSeconds) {
		try {

			// This is used to avoid the java error "Local Variable
			// Defined in an Enclosing Scope Must be Final or Effectively Final"
			AtomicReference<IUsernamePassword> credential = new AtomicReference<>();

			// Waits until the credential is free or the timeout is over
			this.waitFor.wait(timeOutSeconds, "Waiting for the credentials", true, false, () -> {
				// Gets the credential
				credential.set(server.getCredential(application, reserve, search));

				// Checks if the credential is returned
				return credential.get() != null;
			});

			// We add the new credential in use to the credentialsInUse list
			credentialsInUse.add(new Credential(application, reserve, credential.get()));

			// Return the credential
			return credential.get();

		} catch (JidokaUnsatisfiedConditionException e) {
			throw new JidokaFatalException("Credential not available");
		}
	}

	/**
	 * Gets the given credential {@code currentCredentialApplication} with the given
	 * {@code userName}. It waits for the credential until the given {@code timeout}
	 * is over.
	 * 
	 * @param application    Credential application
	 * @param userName       Username to filter by
	 * @param reserve        True if you want to reserve the credential
	 * @param timeOutSeconds Maximum waiting time for the credential
	 * 
	 * @return The {@link IUsernamePassword} object of the credential
	 */
	public IUsernamePassword getCredentialByUser(String application, String userName, Boolean reserve,
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
		IUsernamePassword credential = credentialsList.stream().filter(c -> c.getUsername().equals(userName))
				.findFirst().orElse(null);

		try {
			// If reserve is true, it reserves the credential
			if (reserve) {

				// Waits until the credential is reserved
				this.waitFor.wait(timeOutSeconds, "Reserving the credentials", true, false, () ->
				// Gets the credential
				server.reserveCredential(application, credential.getUsername())

				);

			}
		} catch (Exception e) {
			throw new JidokaFatalException("Error reserving the credential", e);
		}

		// We update the selected credential as being used with the help of the
		// credentialsInUse list
		credentialsInUse.add(new Credential(application, reserve, credential));

		return credential;
	}

	/**
	 * Release the specified credential
	 * 
	 * @param credentialApplication Credential application
	 * @param userName              Credential username
	 */
	public void releaseCredential(String credentialApplication, String userName) {

		// Get credential
		Credential credential = credentialsInUse.stream()
				.filter(c -> c.getUsernamePassword().getUsername().equals(userName)).findFirst().orElse(null);

		// If credential was already retrieved, releases it
		if (credential.getReserved()) {

			server.releaseCredential(credentialApplication, userName);
			// Deletes the released credential from the credentials in use list
			credentialsInUse.remove(credential);

			server.info(String.format("Credential %s with username: %s released", credentialApplication, userName));
		} else {
			server.info(String.format("Credential %s with username: %s wasn't reserved or was already released",
					credentialApplication, userName));
		}

	}

	/**
	 * Release all retrieved credentials of the given application
	 * 
	 * @param credentialApplication Credential application
	 */
	public void releaseAllCredentials(String credentialApplication) {

		List<Credential> filteredCredentials = credentialsInUse.stream()
				.filter(c -> c.getApplication().equals(credentialApplication)).collect(Collectors.toList());

		for (Credential credential : filteredCredentials) {
			releaseCredential(credentialApplication, credential.getUsernamePassword().getUsername());
		}
	}

	/**
	 * Release all retrieved credentials
	 */
	public void releaseAllCredentials() {

		List<Credential> credentialsCopy = new ArrayList<>();
		credentialsCopy.addAll(credentialsInUse);

		for (Credential credential : credentialsCopy) {
			releaseCredential(credential.getApplication(), credential.getUsernamePassword().getUsername());
		}
	}
}

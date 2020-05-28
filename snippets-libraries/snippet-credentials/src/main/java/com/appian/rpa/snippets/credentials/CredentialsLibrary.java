package com.appian.rpa.snippets.credentials;

import java.util.Map;

import com.novayre.jidoka.client.api.ECredentialSearch;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.INano;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.JidokaMethod;
import com.novayre.jidoka.client.api.JidokaParameter;
import com.novayre.jidoka.client.api.annotations.Nano;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;
import com.novayre.jidoka.client.api.execution.IUsernamePassword;
import com.novayre.jidoka.client.lowcode.IRobotVariable;

import jodd.util.StringUtil;

/**
 * Nano robot using class CredentialsUtils for credential management
 */
@Nano
public class CredentialsLibrary implements INano {

	/** Credentials utils instances */
	private CredentialsUtils credentialUtils = null;

	/** Application name */
	private String application;

	/**
	 * Get a credential for an application with specific username and stores the
	 * value in a Robotic process variable.
	 * 
	 * @param application
	 * @param username
	 * @param credentialVar
	 * @throws JidokaFatalException
	 */
	@JidokaMethod(name = "Get Credential", description = "Get a credential for an application with specific username")
	public void getCredentialWithUsername(
			@JidokaParameter(defaultValue = "", name = "Application * ") String application,
			@JidokaParameter(defaultValue = "", name = "Username") String username,
			@JidokaParameter(defaultValue = "false", name = "Reserve (true/false)") String reserve,
			@JidokaParameter(defaultValue = "", name = "CredentialVar") String credentialVar)
			throws JidokaFatalException {

		IJidokaServer<?> server = JidokaFactory.getServer();

		this.credentialUtils = CredentialsUtils.getInstance();

		this.application = application;

		IUsernamePassword credential = null;

		Boolean reserveBool = (reserve == null || !reserve.equals("true")) ? false : true;

		if (StringUtil.isBlank(username)) {

			credential = credentialUtils.getCredentials(application, reserveBool, ECredentialSearch.DISTRIBUTED, 5);
		} else {

			credential = credentialUtils.getCredentialsByUser(application, username, reserveBool, 5);
		}

		if (credential == null) {

			server.warn("Haven't found a credential for application " + application);

			return;
		}

		server.debug("Found a credential for application " + application);

		Map<String, IRobotVariable> variables = server.getWorkflowVariables();

		IRobotVariable rv = variables.get(credentialVar);
		if (rv != null) {
			rv.setValue(credential);
		}
	}

	/**
	 * The Jidoka Platform calls this method when the robot is ending, either
	 * successfully or abruptly, in this latter case, throwing an exception.
	 * 
	 * @see com.novayre.jidoka.client.api.IRobot#cleanUp()
	 */
	@Override
	public void cleanUp() throws Exception {

		// If the browser was initialized, close it
		if (credentialUtils != null) {

			credentialUtils.releaseAllCredentials(application);
		}
	}

}
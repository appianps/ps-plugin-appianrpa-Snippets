package com.appian.rpa.snippets.credentials;

import com.novayre.jidoka.client.api.execution.IUsernamePassword;

/** 
 * Class that represents an instance of a credential 
 */
public class Credential {
	
	/** The credential application */
	private String application;
	
	/** True if the credential has been reserved */
	private Boolean reserved;
	
	/** Credential user name and password */
	private IUsernamePassword usernamePassword;
	
	/**
	 * Public constructor
	 * 
	 * @param application The credential application
	 * @param reserved True if the credential has been reserved 
	 * @param usernamePassword Credential username and password
	 */
	public Credential(String application, Boolean reserved, IUsernamePassword usernamePassword) {
		this.application = application;
		this.reserved = reserved;
		this.usernamePassword = usernamePassword;
	}
	

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public Boolean getReserved() {
		return reserved;
	}

	public void setReserved(Boolean reserved) {
		this.reserved = reserved;
	}

	public IUsernamePassword getUsernamePassword() {
		return usernamePassword;
	}

	public void setUsernamePassword(IUsernamePassword usernamePassword) {
		this.usernamePassword = usernamePassword;
	}

}

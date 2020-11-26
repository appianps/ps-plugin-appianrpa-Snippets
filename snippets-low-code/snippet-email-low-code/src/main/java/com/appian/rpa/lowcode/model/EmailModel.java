package com.appian.rpa.lowcode.model;

import java.io.File;

import org.apache.velocity.VelocityContext;

/**
 * Class to represent a row in a Excel sheet.
 *
 */
public class EmailModel {

	/** Email to address */
	private String[] toAddress;

	/** Email cc address */
	private String[] ccAddress;

	/** Email bcc address */
	private String[] bccAddress;

	/** Email from address */
	private String fromAddress;

	/** Email subject */
	private String subject;

	/** Email body */
	private String emailBody;

	/** Email attachments */
	private File[] attachments;

	/** Velocity context for filling the template */
	private VelocityContext velocityContext;

	/** Velocity template */
	private File velocityTemplate;

	/** Public non-parameters constructor */
	public EmailModel() {
		// Empty constructor
	}

	public String[] getToAddress() {
		return toAddress;
	}

	public void setToAddress(String[] toAddress) {
		this.toAddress = toAddress;
	}

	public String[] getCcAddress() {
		return ccAddress;
	}

	public void setCcAddress(String[] ccAddress) {
		this.ccAddress = ccAddress;
	}

	public String[] getBccAddress() {
		return bccAddress;
	}

	public void setBccAddress(String[] bccAddress) {
		this.bccAddress = bccAddress;
	}

	public String getFromAddress() {
		return fromAddress;
	}

	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getEmailBody() {
		return emailBody;
	}

	public void setEmailBody(String emailBody) {
		this.emailBody = emailBody;
	}

	public File[] getAttachments() {
		return attachments;
	}

	public void setAttachments(File[] attachments) {
		this.attachments = attachments;
	}

	public VelocityContext getVelocityContext() {
		return velocityContext;
	}

	public void setVelocityContext(VelocityContext velocityContext) {
		this.velocityContext = velocityContext;
	}

	public File getVelocityTemplate() {
		return velocityTemplate;
	}

	public void setVelocityTemplate(File velocityTemplate) {
		this.velocityTemplate = velocityTemplate;
	}

}

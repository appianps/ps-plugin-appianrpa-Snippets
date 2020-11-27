package com.appian.rpa.lowcode;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.INano;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.JidokaMethod;
import com.novayre.jidoka.client.api.JidokaParameter;
import com.novayre.jidoka.client.api.annotations.Nano;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;
import com.novayre.jidoka.client.lowcode.IRobotVariable;

/**
 * This library helps you on send email's from the server. You can configure a
 * lot of email parameters.
 */
@Nano
public class EmailManagerLibrary implements INano {

	/** Velocity template file. */
	private File velocityTemplate;

	/** IJidokaServer instance. */
	private IJidokaServer<?> server;

	/** EmailManager instance */
	private EmailManager emailManager;

	/** List of attachments */
	private List<File> attachments = new ArrayList<>();

	/**
	 * Is invoked before the first use of the library.
	 */
	@Override
	public void init() {

		this.server = JidokaFactory.getServer();

		this.emailManager = new EmailManager();

	}

	/**
	 * Send the mail using the server configuration. Must be defined a To Address
	 * {@linkplain EmailManagerLibrary#setToAddress(List)} in the manager
	 * <b>before</b> call this method.</br>
	 * To enter several 'to', 'cc' or 'bcc', type them separated by ';';
	 */
	@JidokaMethod(name = "Send an email", description = "Send the mail using the server configuration")
	public void send(@JidokaParameter(defaultValue = "", name = "From * ") String from,
			@JidokaParameter(defaultValue = "", name = "To * ") String to,
			@JidokaParameter(defaultValue = "", name = "Cc ") String cc,
			@JidokaParameter(defaultValue = "", name = "Bcc ") String bcc,
			@JidokaParameter(defaultValue = "", name = "Subject * ") String subject,
			@JidokaParameter(defaultValue = "", name = "Body * ") String emailBody) {

		String[] toAddresses = to.split(";");
		String[] ccAddresses = cc.split(";");
		String[] bccAddresses = bcc.split(";");

		emailManager.useServerConfiguration(true).fromAddress(from).toAddress(toAddresses).ccAddress(ccAddresses)
				.bccAddress(bccAddresses).subject(subject).emailBody(emailBody).attachments(this.attachments).send();

		server.debug("Mail sent.");

	}

	/**
	 * Send the mail using the server configuration. It is important to configure
	 * the SMTP server, setting an user and a password for the proper functioning.
	 */
	@JidokaMethod(name = "Send an email with template", description = "Send the mail using a velocity template.")
	public void sendWithTemplate(@JidokaParameter(defaultValue = "", name = "From * ") String from,
			@JidokaParameter(defaultValue = "", name = "To * ") String to,
			@JidokaParameter(defaultValue = "", name = "Cc ") String cc,
			@JidokaParameter(defaultValue = "", name = "Bcc ") String bcc,
			@JidokaParameter(defaultValue = "", name = "Subject * ") String subject,
			@JidokaParameter(defaultValue = "", name = "Template file instruction * ") String template) {

		velocityTemplate = Paths.get(server.getCurrentDir(), "Templates/emailTemplate.vm").toFile();

		if (!velocityTemplate.exists()) {
			throw new JidokaFatalException(
					"You must include the velocity template on the support files directory 'Templates/emailTemplate.vm'");
		}

		String[] toAddresses = to.split(";");
		String[] ccAddresses = cc.split(";");
		String[] bccAddresses = bcc.split(";");

		emailManager.useServerConfiguration(true).fromAddress(from).toAddress(toAddresses).ccAddress(ccAddresses)
				.bccAddress(bccAddresses).subject(subject).attachments(this.attachments).send();

		server.debug("Mail sent.");
	}

	/**
	 * Send the mail using the server configuration. It is important to configure
	 * the SMTP server, setting an user and a password for the proper functioning.
	 */
	@JidokaMethod(name = "Set the email attachments", description = "Set the email attachments")
	public void setAttachments(@JidokaParameter(defaultValue = "", name = "Attachment") String attachment) {

		String name = server.getWorkflowVariables().get("file").getFileNameValue();
		
		this.attachments.add(Paths.get(server.getCurrentDir(), name).toFile());

	}

}

package com.appian.rpa.snippets;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import com.appian.rpa.snippets.model.EmailModel;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;
import com.novayre.jidoka.mail.api.IMail;
import com.novayre.jidoka.mail.api.MailAttachment;
import com.novayre.jidoka.mail.api.MailSendOptions;

public class EmailManager implements IRobot {

	/**
	 * HTML pattern to identify the mail body.
	 */
	private static final String HTML_PATTERN = "<(\'[^\']*\'|'[^']*'|[^'\'>])*>";

	/** Email model instance */
	private EmailModel email;

	/** MailSendOptions instance */
	private MailSendOptions mailSendOptions;

	/** Server instance */
	private IJidokaServer<?> server;

	/** IMail instance */
	private IMail mail;

	/** SMTP host */
	private String host;

	/** SMTP port */
	private int port;

	/** True if uses the server SMTP configuration */
	private boolean useServerConfiguration;

	/**
	 * Public constructor
	 */
	public EmailManager() {
		email = new EmailModel();
		mailSendOptions = new MailSendOptions();

		this.server = JidokaFactory.getServer();

		this.mail = IMail.getInstance(IRobot.getDummyInstance());
	}

	/**
	 * Send the mail using the server configuration. Must be defined a To Address
	 * {@linkplain EmailManager#setToAddress(List)} in the manager <b>before</b>
	 * call this method.
	 */
	public void send() {

		try {
			if (StringUtils.isAllBlank(this.email.getToAddress())) {

				server.debug("To address can't be empty");
				return;
			}

			String body = "";

			if (!StringUtils.isBlank(email.getEmailBody())) {
				body = this.email.getEmailBody();
			} else {
				body = fillTemplate(this.email.getVelocityTemplate());
			}

			server.debug("Starting notification.");

			if (this.useServerConfiguration) {
				mailSendOptions.setUseServerConfiguration(true);
			} else {
				if (StringUtils.isBlank(this.host) || this.port == 0) {
					throw new JidokaFatalException(
							"You must configure the post and host SMPT or use the server configuration");
				}

				mailSendOptions.setHost(this.host);
				mailSendOptions.setPort(this.port);
			}

			mailSendOptions.toAddress(this.email.getToAddress()).ccAddress(this.email.getCcAddress())
					.bccAddress(this.email.getBccAddress()).fromAddress(this.email.getFromAddress())
					.subject(this.email.getSubject());

			server.debug("Address info configured.");

			setBodyFormat(body);

			setAttachments();

			server.debug("Sending mail.");

			mail.sendMailFromServer(mailSendOptions);

			server.debug("Mail sent.");

		} catch (IOException e) {
			throw new JidokaFatalException("Error sending the email", e);
		}
	}

	private String fillTemplate(File template) {

		Properties p = new Properties();
		p.setProperty("input.encoding", "UTF-8");
		p.setProperty("output.encoding", "UTF-8");
		p.setProperty("file.resource.loader.path", template.getParent().toString());
		org.apache.velocity.app.VelocityEngine ve = new org.apache.velocity.app.VelocityEngine(p);

		Template t = ve.getTemplate(template.getName(), "UTF-8");

		Writer w = new StringWriter();

		t.merge(this.email.getVelocityContext(), w);

		return w.toString();
	}

	private void setBodyFormat(String body) {

		if (StringUtils.isBlank(body)) {

			server.debug("Mail without body.");
			return;
		}

		Pattern htmlPatter = Pattern.compile(HTML_PATTERN);
		Matcher htmlMatcher = htmlPatter.matcher(body);

		if (htmlMatcher.lookingAt()) {
			server.debug("HTML body configured.");
			mailSendOptions.setHtmlContent(body);
		} else {
			server.debug("TEXT body configured.");
			mailSendOptions.setTextContent(body);
		}
	}

	private void setAttachments() {

		File[] attachments = this.email.getAttachments();

		if (attachments == null || attachments.length == 0) {
			server.debug("No attachment files configured");
			return;
		}

		MailAttachment[] attachmentsArray = new MailAttachment[attachments.length];

		for (int i = 0; i < attachments.length; i++) {

			MailAttachment toAttach = new MailAttachment(attachments[i]);

			toAttach.setFileName(attachments[i].getName());
			toAttach.setName(attachments[i].getName());

			server.debug("Attached file: " + attachments[i].getAbsolutePath());

			attachmentsArray[i] = toAttach;
		}

		mailSendOptions.attachments(attachmentsArray);

	}

	public EmailManager host(String host) {
		this.host = host;
		return this;
	}

	public EmailManager port(int port) {
		this.port = port;
		return this;
	}

	public EmailManager useServerConfiguration(boolean useServerConfiguration) {
		this.useServerConfiguration = useServerConfiguration;
		return this;
	}

	public EmailManager toAddress(String[] toAddress) {
		this.email.setToAddress(toAddress);
		return this;
	}

	public EmailManager ccAddress(String[] ccAddress) {
		this.email.setCcAddress(ccAddress);
		return this;
	}

	public EmailManager bccAddress(String[] bccAddress) {
		this.email.setBccAddress(bccAddress);
		return this;
	}

	public EmailManager fromAddress(String fromAddress) {
		this.email.setFromAddress(fromAddress);
		return this;
	}

	public EmailManager attachments(List<File> attachments) {
		File[] array = new File[attachments.size()];
		this.email.setAttachments(attachments.toArray(array));
		return this;
	}

	public EmailManager subject(String subject) {
		this.email.setSubject(subject);
		return this;
	}

	public EmailManager emailBody(String emailBody) {
		this.email.setEmailBody(emailBody);
		return this;
	}

	public EmailManager velocityConfiguration(File template, Map<String, Object> velocityContext) {

		VelocityContext context = new VelocityContext();

		for (Entry<String, Object> entry : velocityContext.entrySet()) {
			context.put(entry.getKey(), entry.getValue());
		}

		this.email.setVelocityContext(context);
		this.email.setVelocityTemplate(template);
		return this;
	}

}

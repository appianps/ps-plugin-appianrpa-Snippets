package com.appian.rpa.snippets;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Paths;
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
import com.novayre.jidoka.client.api.INano;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.JidokaMethod;
import com.novayre.jidoka.client.api.JidokaParameter;
import com.novayre.jidoka.client.api.annotations.Nano;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;
import com.novayre.jidoka.mail.api.IMail;
import com.novayre.jidoka.mail.api.MailAttachment;
import com.novayre.jidoka.mail.api.MailSendOptions;

/**
 * This library helps you on send email's from the server. You can configure a
 * lot of email parameters.
 */
@Nano
public class EmailManagerLibrary implements INano{

	/** HTML pattern to identify the mail body. */
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
	
	/** Velocity template file. */
	private File velocityTemplate;

	/**
	 * Is invoked before the first use of the library.
	 */
	public void init() {
		
		email = new EmailModel();
		mailSendOptions = new MailSendOptions();
		server = JidokaFactory.getServer();
	    mail = IMail.getInstance(IRobot.getDummyInstance());
	}
	
	
	/**
	 * Send the mail using the server configuration. Must be defined a To Address
	 * {@linkplain EmailManagerLibrary#setToAddress(List)} in the manager <b>before</b>
	 * call this method.</br>
	 */
	@JidokaMethod(name = "Send an email", description = "Send the mail using the server configuration")
	public void send(@JidokaParameter(defaultValue = "", name = "From * ") String from, 
			@JidokaParameter(defaultValue = "", name = "To * ") String to, 
			@JidokaParameter(defaultValue = "", name = "Cc ") String cc, 
			@JidokaParameter(defaultValue = "", name = "Bcc ") String bcc, 
			@JidokaParameter(defaultValue = "", name = "Subject * ") String subjectInstruction, 
			@JidokaParameter(defaultValue = "", name = "Body * ") String emailBody) {
		
		
		this.email.setFromAddress(from);
		this.email.setToAddress(to == null ? new String[0] : to.split(";"));
		this.email.setCcAddress(cc == null ? new String[0] : cc.split(";"));
		this.email.setBccAddress(bcc == null ? new String[0] : bcc.split(";"));
		this.email.setEmailBody(emailBody);
		this.email.setSubject(server.getParameters().get(subjectInstruction));
		
	
		try {
			if (StringUtils.isAllBlank(this.email.getToAddress())) {

				server.debug("To address can't be empty");
				return;
			}

			String body = this.email.getEmailBody();
			
			server.debug("Starting notification.");

			mailSendOptions.setUseServerConfiguration(true);
			
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
	
	/**
	 * Send the mail using the server configuration.
	 * It is important to configure the SMTP server, setting an user and a
	 * password for the proper functioning.
	 */
	@JidokaMethod(name = "Send an email with template", description = "Send the mail using a velocity template.")
	public void sendWithTemplate(@JidokaParameter(defaultValue = "", name = "From * ") String from, 
			@JidokaParameter(defaultValue = "", name = "To * ") String to, 
			@JidokaParameter(defaultValue = "", name = "Cc ") String cc, 
			@JidokaParameter(defaultValue = "", name = "Bcc ") String bcc, 
			@JidokaParameter(defaultValue = "", name = "Subject * ") String subjectInstruction, 
			@JidokaParameter(defaultValue = "", name = "Template file instruction * ") String templateInstruction) {
		
			this.email.setFromAddress(server.getParameters().get(from));
			this.email.setToAddress(server.getParameters().get(to).split(";"));
			this.email.setCcAddress(server.getParameters().get(cc).split(";"));
			this.email.setBccAddress(server.getParameters().get(bcc).split(";"));
			this.velocityTemplate = Paths.get(server.getCurrentDir(), server.getParameters().get(templateInstruction)).toFile();
			this.email.setVelocityTemplate(velocityTemplate);
			this.email.setSubject(server.getParameters().get(subjectInstruction));
		
	
		try {
			if (StringUtils.isAllBlank(this.email.getToAddress())) {

				server.debug("To address can't be empty");
				return;
			}

			String body = fillTemplate(this.email.getVelocityTemplate());

			server.debug("Starting notification.");

			mailSendOptions.setUseServerConfiguration(true);
			
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
	

	/**
	 * Sets the velocity template and the velocity context.</br>
	 * If the "body" parameter of the email is set, this has no effect.</br>
	 * For more information about Velocity see <a href=
	 * "https://velocity.apache.org/engine/2.2/getting-started.html">Velocity
	 * Documentation</a>
	 * 
	 * @param template        The Velocity template.
	 * @param velocityContext The velocity context.
	 * @return this (fluent API)
	 */
	public EmailManagerLibrary velocityConfiguration(File template, Map<String, Object> velocityContext) {

		VelocityContext context = new VelocityContext();

		for (Entry<String, Object> entry : velocityContext.entrySet()) {
			context.put(entry.getKey(), entry.getValue());
		}

		this.email.setVelocityContext(context);
		this.email.setVelocityTemplate(template);
		return this;
	}

	/**
	 * Fill the velocity template and returns the email body.
	 * 
	 * @param template Velocity template.
	 * @return The body String.
	 */
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

	/**
	 * Sets the body format, either text or HTML.
	 * 
	 * @param body Returns the body formatted.
	 */
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

	/**
	 * Sets the attachments files on the send mail configuration.
	 */
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


}

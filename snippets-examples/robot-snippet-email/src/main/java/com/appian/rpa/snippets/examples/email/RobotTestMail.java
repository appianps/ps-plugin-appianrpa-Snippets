package com.appian.rpa.snippets.examples.email;

import java.awt.AWTException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.appian.rpa.snippets.EmailManager;
import com.appian.rpa.snippets.examples.email.model.CovidModel;
import com.appian.rpa.snippets.examples.email.params.EInstructions;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.annotations.Robot;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;
import com.novayre.jidoka.client.api.exceptions.JidokaItemException;
import com.novayre.jidoka.mail.api.IMail;
import com.novayre.jidoka.mail.api.MailAttachment;
import com.novayre.jidoka.mail.api.MailSendOptions;

/**
 * This robot sends an email with the given parameters on the RPA console. Also,
 * it uses a Velocity template to fill the body dynamically. The attachment sent
 * is a screenshot.
 */
@Robot
public class RobotTestMail implements IRobot {

	/** Velocity template file. */
	private File velocityTemplate;

	/** IJidokaServer instance. */
	private IJidokaServer<?> server;

	/** EmailManager instance */
	private EmailManager emailManager;

	/** CovidModel item */
	private CovidModel currentItem;

	/** List of attachments */
	private List<File> attachments = new ArrayList<>();

	/**
	 * Startup method
	 * 
	 * @throws Exception
	 */
	@Override
	public boolean startUp() throws Exception {

		server = JidokaFactory.getServer();

		return true;
	}

	/**
	 * Start action
	 */
	public void start() {

		this.emailManager = new EmailManager();

		velocityTemplate = Paths.get(server.getCurrentDir(), "Templates/testTemplate.vm").toFile();

		currentItem = new CovidModel();
		currentItem.setAreaToSearch("USA");
		currentItem.setActiveCases(800000);
		currentItem.setFatalCases(100000);
		currentItem.setRecoveredCases(250000);

		server.debug("Robot initialized");

	}

	/**
	 * Prepares the attachments by taking a screenshot to send.
	 * 
	 * @throws IOException
	 * @throws AWTException
	 */
	public void prepareAttachment() throws AWTException, IOException {

		File outputfile1 = Paths.get(server.getCurrentDir(), "Attachment1.png").toFile();

		BufferedImage bi = server.getScreen();

		ImageIO.write(bi, "png", outputfile1);

		this.attachments.add(outputfile1);

	}

	/**
	 * Sends the email with the given parameters, using the SMTP server
	 * configuration.
	 */
	public void sendMail() {

		String fromAddress = EInstructions.FROM.getInstruction().getAsString();
		String[] toAddresses = EInstructions.TO.getInstruction().getAsString().split(";");
		String[] ccAddresses = EInstructions.CC.getInstruction().getAsString().split(";");
		String[] bccAddresses = EInstructions.BCC.getInstruction().getAsString().split(";");
		String subject = EInstructions.SUBJECT.getInstruction().getAsString();
		Map<String, Object> velocityContext = new HashMap<>();
		velocityContext.put("CURRENT_ITEM", currentItem);
		velocityContext.put("DATE", new Date(server.getExecution(0).getCurrentExecution().getExecutionDate()));

		emailManager.useServerConfiguration(true).fromAddress(fromAddress).toAddress(toAddresses).ccAddress(ccAddresses)
				.bccAddress(bccAddresses).subject(subject).attachments(this.attachments)
				.velocityConfiguration(this.velocityTemplate, velocityContext).send();

	}

	/**
	 * End action
	 * 
	 * @throws Exception
	 */
	public void end() {
		// No end actions
	}

	@Override
	public String[] cleanUp() throws Exception {
		return IRobot.super.cleanUp();
	}

	/**
	 * Any type of error should be managed in this method.
	 */
	@Override
	public String manageException(String action, Exception exception) throws Exception {

		// Get the exception message
		String errorMessage = ExceptionUtils.getRootCause(exception).getMessage();

		// Send a screenshot to the log so the user can see the screen in the moment
		// of the error. This is one of the best ways to trace errors clearly.
		server.sendScreen("Screenshot of the error");

		// Whenever a JidokaFatalException is thrown, the execution should be aborted.
		if (ExceptionUtils.indexOfThrowable(exception, JidokaFatalException.class) >= 0) {

			server.error(StringUtils.isBlank(errorMessage) ? "Fatal error" : errorMessage);
			return IRobot.super.manageException(action, exception);
		}

		server.warn("Unknown exception!");

		// If we have any other exception we must abort the execution, as we do not know
		// what happened

		return IRobot.super.manageException(action, exception);
	}

}

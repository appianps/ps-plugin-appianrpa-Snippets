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

import com.appian.rpa.snippets.EmailManager;
import com.appian.rpa.snippets.examples.email.model.CovidModel;
import com.appian.rpa.snippets.examples.email.params.EInstructions;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.annotations.Robot;

/**
 * test-mail-robot
 */
@Robot
public class RobotTestMail implements IRobot {

	/**
	 * Velocity template file.
	 */
	private File velocityTemplate;

	/**
	 * The JidokaServer instance.
	 */
	private IJidokaServer<?> server;

	private EmailManager emailManager;

	private CovidModel currentItem;

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
	 * Action "start"
	 * 
	 * @throws Exception
	 */
	public void start() throws Exception {

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
	 * Action 'Prepare Attachment'
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
	 * Action 'Send Mail'
	 * 
	 * @throws IOException
	 */
	public void sendMailTest() {

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

		// .host("localhost").port(25).

	}

	/**
	 * Action "end"
	 * 
	 * @throws Exception
	 */
	public void end() throws Exception {
	}

}

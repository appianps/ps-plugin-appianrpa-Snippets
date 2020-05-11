package com.appian.rpa.robot.applications;

import java.io.Serializable;

import com.appian.rpa.snippets.commons.application.ApplicationManager;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.annotations.Robot;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;

//TODO document javadoc for the whole class
@Robot
public class ApplicationsRobot implements IRobot {

	private ApplicationManager applicationManager;
	private IJidokaServer<Serializable> server;

	@SuppressWarnings("unchecked")
	public boolean startUp() throws Exception {
		server = (IJidokaServer<Serializable>) JidokaFactory.getServer();

		return true;

	}

	public void start() {

		applicationManager = new ApplicationManager(this, "stockManager.exe", "C:/Users/appian/Desktop/", ".*Stock manager.*");

	}

	/**
	 * Init application
	 */
	public void startApplication() {

		try {
			applicationManager.startApplication();
		} catch (Exception e) {
			throw new JidokaFatalException("Error starting the application");
		}
	}

	public void end() {
		server.info("end");
	}

}

package com.appian.robot.demo.pages;

import com.appian.rpa.snippet.IBM3270Commons;
import com.appian.rpa.snippet.TextInScreen;
import com.appian.rpa.snippet.page.IBM3270Page;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.exceptions.JidokaException;
import com.novayre.jidoka.client.api.multios.IClient;

/**
 * Class to manage Netview Page
 */
public class NetViewPage extends IBM3270Page {

	/**
	 * IBM3270Commons instance
	 */
	private IBM3270Commons commons;

	/**
	 * Jidoka server instance
	 */
	private IJidokaServer<?> server;

	/**
	 * Client Module Instance
	 */
	protected IClient client;

	/**
	 * Class constructor
	 * 
	 * @param client
	 * @param robot
	 * @throws JidokaException
	 */
	public NetViewPage(IBM3270Commons commons) throws JidokaException {
		super(commons);
		this.commons = commons;
		client = IClient.getInstance(commons.getRobot());
		server = JidokaFactory.getServer();
	}

	/**
	 * Unique text on each page for recognition
	 * 
	 * @return A unique text in this server
	 */
	@Override
	public String getUnivocalRegex() {

		return ConstantsTexts.NETVIEW_UNIVOCAL_TEXT;
	}

	/**
	 * Changes the password of an operator
	 */
	public void changeOperatorPassword() {

		TextInScreen textInScreen = commons.locateText(2, "ID ==>");
		commons.moveToCoodinates(textInScreen, 8, 0);
		commons.write(ConstantsTexts.TEST_ID_OPERATOR);
		commons.enter();
		client.pause(2000);

		// Change Password Page
		server.sendScreen("Change password page");
		commons.waitTillTextDisappears(ConstantsTexts.NETVIEW_UNIVOCAL_TEXT);

		commons.write(ConstantsTexts.TEST_PWD_OPERATOR, false);
		commons.pressDown(2);
		commons.pressLeft(ConstantsTexts.TEST_PWD_OPERATOR.length());

		commons.write(ConstantsTexts.TEST_NEW_PWD_OPERATOR, false);
		commons.pressDown(2);
		commons.pressLeft(ConstantsTexts.TEST_NEW_PWD_OPERATOR.length());

		commons.write(ConstantsTexts.TEST_NEW_PWD_OPERATOR, false);
		commons.enter();
		client.pause(1000);

		commons.waitTillTextDisappears(2, ConstantsTexts.PWD_UNIVOCAL_TEXT);

		TextInScreen textInScreenPwd = commons.locateText(ConstantsTexts.INVALID_USER_UNIVOCAL_TEXT);

		if (textInScreenPwd != null) {
			server.sendScreen("Password could not be changed: invalid opertator");
		} else {
			server.sendScreen("Password changed");
		}
	}

	/**
	 * Indicates the name of the current page (class)
	 * 
	 * @return
	 */
	@Override
	public String getPageName() {
		return this.getClass().getSimpleName();
	}

}

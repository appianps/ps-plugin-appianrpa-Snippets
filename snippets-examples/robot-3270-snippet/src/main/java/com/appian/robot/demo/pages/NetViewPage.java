package com.appian.robot.demo.pages;

import com.appian.rpa.snippet.ConstantsWaits;
import com.appian.rpa.snippet.IBM3270Commons;
import com.appian.rpa.snippet.TextInScreen;
import com.appian.rpa.snippet.page.RemotePage;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.exceptions.JidokaException;
import com.novayre.jidoka.client.api.multios.IClient;

/**
 * Class to manage Netview Page
 */
public class NetViewPage extends RemotePage {

	/**
	 * Specific implementation of the common class
	 */
	private IBM3270Commons commons;

	/**
	 * Clas constructor
	 * 
	 * @param client
	 * @param robot
	 */
	public NetViewPage(IClient client, IRobot robot, IBM3270Commons commons) {
		super(client, robot);
		this.commons = commons;
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
	 * Check that the robot is on the correct page (current page)
	 * 
	 * @throws JidokaGenericException
	 */
	@Override
	public RemotePage assertIsThisPage() throws JidokaException {

		try {
			commons.locateText(ConstantsWaits.DEFAULT_NUMBER_OF_RETRIES_LOCATING_TEXT, getUnivocalRegex());
		} catch (Exception e) {
			server.debug(e.getMessage());
			throw new JidokaException(getPageName());
		}

		return this;
	}

	@Override
	public void selectAllText() {
		commons.selectAllText();
	}

	@Override
	public void activateWindow() {
		commons.activateWindow();
	}

	@Override
	public void moveToBottonRightCorner() {
		commons.moveToBottonRightCorner();
	}

	@Override
	public String[] splitScreenLines(String screen) {
		return commons.splitScreenLines(screen);
	}

	@Override
	public String getWindowTitleRegex() {
		return commons.getWindowTitleRegex();
	}

}

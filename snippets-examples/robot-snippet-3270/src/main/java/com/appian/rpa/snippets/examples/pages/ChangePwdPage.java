package com.appian.rpa.snippets.examples.pages;

import com.appian.rpa.snippets.ibm3270.IBM3270Commons;
import com.appian.rpa.snippets.ibm3270.TextInScreen;
import com.appian.rpa.snippets.ibm3270.page.IBM3270Page;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.exceptions.JidokaException;

/**
 * Class to manage Change Password Page
 */
public class ChangePwdPage extends IBM3270Page {

	/**
	 * Jidoka server instance
	 */
	private IJidokaServer<?> server;

	/**
	 * Class constructor
	 * 
	 * @param commons
	 * @throws JidokaException
	 */
	public ChangePwdPage(IBM3270Commons commons) throws JidokaException {
		super(commons);
		server = JidokaFactory.getServer();
	}

	/**
	 * Unique text on each page for recognition
	 * 
	 * @return A unique text in this server
	 */
	@Override
	public String getUnivocalRegex() {

		return ConstantsTexts.PWD_UNIVOCAL_TEXT;
	}

	/**
	 * Changes the password of an operator
	 * 
	 * @throws JidokaException
	 */
	public NetViewPage changeOperatorPassword() throws JidokaException {

		commons.write(ConstantsTexts.TEST_PWD_OPERATOR, false);
		commons.pressDown(2);
		commons.pressLeft(ConstantsTexts.TEST_PWD_OPERATOR.length());

		commons.write(ConstantsTexts.TEST_NEW_PWD_OPERATOR, false);
		commons.pressDown(2);
		commons.pressLeft(ConstantsTexts.TEST_NEW_PWD_OPERATOR.length());

		commons.write(ConstantsTexts.TEST_NEW_PWD_OPERATOR, false);
		commons.enter();

		commons.waitTillTextDisappears(3, ConstantsTexts.PWD_UNIVOCAL_TEXT);

		NetViewPage netViewPage = new NetViewPage(commons);

		TextInScreen textInScreenPwd = commons.locateText(ConstantsTexts.INVALID_USER_UNIVOCAL_TEXT);

		if (textInScreenPwd != null) {
			server.sendScreen("Password could not be changed: invalid opertator");
		} else {
			server.sendScreen("Password changed");
		}

		return netViewPage;

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

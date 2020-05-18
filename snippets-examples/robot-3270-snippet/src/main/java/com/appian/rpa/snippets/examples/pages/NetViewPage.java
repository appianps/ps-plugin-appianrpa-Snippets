package com.appian.rpa.snippets.examples.pages;

import com.appian.rpa.snippet.IBM3270Commons;
import com.appian.rpa.snippet.TextInScreen;
import com.appian.rpa.snippet.page.IBM3270Page;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.exceptions.JidokaException;

/**
 * Class to manage Netview Page
 */
public class NetViewPage extends IBM3270Page {

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
	public NetViewPage(IBM3270Commons commons) throws JidokaException {
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

		return ConstantsTexts.NETVIEW_UNIVOCAL_TEXT;
	}

	/**
	 * Go to the page to change the operator password
	 * 
	 * @throws JidokaException
	 */
	public ChangePwdPage goToChangePasswordPage() throws JidokaException {

		TextInScreen textInScreen = commons.locateText(2, "ID ==>");
		commons.moveToCoodinates(textInScreen, 8, 0);
		commons.write(ConstantsTexts.TEST_ID_OPERATOR);
		commons.enter();

		// Change Password Page
		server.sendScreen("Change password page");
		commons.waitTillTextDisappears(3, ConstantsTexts.NETVIEW_UNIVOCAL_TEXT);

		ChangePwdPage changePwdPage = new ChangePwdPage(commons);

		return changePwdPage;
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

package com.appian.robot.demo.pages;

import com.appian.rpa.snippet.IBM3270Commons;
import com.appian.rpa.snippet.page.IBM3270Page;
import com.novayre.jidoka.client.api.exceptions.JidokaException;
import com.novayre.jidoka.client.api.multios.IClient;

/**
 * Class to manage Netview Page
 */
public class WelcomePage extends IBM3270Page {

	/**
	 * Client Module Instance
	 */
	protected IClient client;

	/**
	 * Class constructor
	 * 
	 * @param commons
	 * @throws JidokaException
	 */
	public WelcomePage(IBM3270Commons commons) throws JidokaException {
		super(commons);
		client = IClient.getInstance(commons.getRobot());
	}

	/**
	 * Go to the page indicated as parameter
	 * 
	 * @param page Name of the page in the menu
	 * @throws JidokaException
	 */
	public NetViewPage goToPage(String page) throws JidokaException {
		commons.write(page);
		client.pause(1000);
		commons.enter();
		client.pause(1000);
		return new NetViewPage(commons);
	}

	/**
	 * Unique text on each page for recognition
	 * 
	 * @return A unique text in this server
	 */
	@Override
	public String getUnivocalRegex() {

		return ConstantsTexts.WELCOME_UNIVOCAL_TEXT;
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

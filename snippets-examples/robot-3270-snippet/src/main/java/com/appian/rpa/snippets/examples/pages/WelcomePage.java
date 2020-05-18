package com.appian.rpa.snippets.examples.pages;

import com.appian.rpa.snippet.IBM3270Commons;
import com.appian.rpa.snippet.page.IBM3270Page;
import com.novayre.jidoka.client.api.exceptions.JidokaException;

/**
 * Class to manage Netview Page
 */
public class WelcomePage extends IBM3270Page {

	/**
	 * Class constructor
	 * 
	 * @param commons
	 * @throws JidokaException
	 */
	public WelcomePage(IBM3270Commons commons) throws JidokaException {
		super(commons);
	}

	/**
	 * Go to the page indicated as parameter
	 * 
	 * @param page Name of the page in the menu
	 * @throws JidokaException
	 */
	public NetViewPage goToPage(String page) throws JidokaException {
		commons.write(page);
		commons.enter();
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

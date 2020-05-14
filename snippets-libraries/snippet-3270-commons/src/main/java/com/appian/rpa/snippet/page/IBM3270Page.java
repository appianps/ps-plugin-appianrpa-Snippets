package com.appian.rpa.snippet.page;

import com.appian.rpa.snippet.ConstantsWaits;
import com.appian.rpa.snippet.IBM3270Commons;
import com.novayre.jidoka.client.api.exceptions.JidokaException;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;

/**
 * Class to manage IBM3270 Pages
 */
public abstract class IBM3270Page {

	/**
	 * IBM3270Commons instance
	 */
	private IBM3270Commons commons;

	/**
	 * Class constructor
	 * 
	 * @param commons
	 * @throws JidokaException
	 */
	public IBM3270Page(IBM3270Commons commons) throws JidokaFatalException {
		this.commons = commons;
		assertIsThisPage();
	}

	/**
	 * Unique text on each page for recognition
	 * 
	 * @return A unique text on each page for recognition
	 */
	public abstract String getUnivocalRegex();

	/**
	 * Check that the robot is on the correct page (current page)
	 * 
	 * @throws JidokaFatalException
	 */
	public Boolean assertIsThisPage() throws JidokaFatalException {

		String univocalRegex = getUnivocalRegex();

		try {
			if (univocalRegex != null) {
				commons.locateText(ConstantsWaits.DEFAULT_NUMBER_OF_RETRIES_LOCATING_TEXT, getUnivocalRegex());
			}
		} catch (Exception e) {

			throw new JidokaFatalException(getPageName());
		}

		return true;
	}

	/**
	 * Indicates the name of the current page (class)
	 * 
	 * @return
	 */
	public String getPageName() {
		return this.getClass().getSimpleName();
	}

	/**
	 * 
	 * @return
	 */
	public IBM3270Commons getCommons() {
		return commons;
	}

	/**
	 * 
	 * @param commons
	 */
	public void setCommons(IBM3270Commons commons) {
		this.commons = commons;
	}

}

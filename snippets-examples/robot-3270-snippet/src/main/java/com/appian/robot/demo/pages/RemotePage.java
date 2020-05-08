package com.appian.robot.demo.pages;

import com.appian.rpa.snippet.ConstantsWaits;
import com.appian.rpa.snippet.clients.FDZCommonsExtended;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.exceptions.JidokaException;
import com.novayre.jidoka.client.api.multios.IClient;


/**
 * Abstract class to be extended for each type of page
 */
public abstract class RemotePage extends FDZCommonsExtended {
	
	
	/**
	 * Unique text on each page for recognition
	 * @return
	 */
	public abstract String getUnivocalRegex();
	
	
	/**
	 * Constructor
	 * @param server
	 * @param windows
	 * @param robot
	 */
	public RemotePage(IClient client, IRobot robot) {
		super(client, robot);
	}

	/**
	 * Check that the robot is on the correct page (current page)
	 * 
	 * @throws JidokaGenericException
	 */
	public RemotePage assertIsThisPage() throws JidokaException {

		try {
			locateText(ConstantsWaits.HIGH_NUMBER_OF_RETRIES_LOCATING_TEXT, getUnivocalRegex());
		} catch (Exception e) {
			server.debug(e.getMessage());
			throw new JidokaException(getPageName());
		}
		
		return this;
	}

	/**
	 * Indicates the name of the current page (class)
	 * @return
	 */
	public String getPageName() {
		return this.getClass().getSimpleName();
	}

}

package com.appian.rpa.snippet.clients;

import com.appian.rpa.snippet.IBM3270Commons;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.windows.api.IWindows;


/**
 * IBM3270Commons extension for FanDeZhi client
 */
public class FDZCommonsExtended extends IBM3270Commons {
	
	/**
	 * The Constant WINDOW_TITLE_REGEX.
	 */
	public static final String WINDOW_TITLE_REGEX = ".*3270";
	
	
	/**
	 * Default constructor
	 * @param server
	 * @param windows
	 * @param robot
	 */
	public FDZCommonsExtended(IJidokaServer<?> server, IWindows windows, IRobot robot) {
		super(server, windows, robot);
	}

	
	/**
	 * Select all text in the screen
	 */
	public void selectAllText() {
		keyboard.down().pause(); 
		keyboard.control("a").pause();
	}
	
	
	/**
	 * Activate a window by title
	 */
	public void activateWindow() {
		
		windows.activateWindow(WINDOW_TITLE_REGEX);
		
		windows.pause();
	}

}

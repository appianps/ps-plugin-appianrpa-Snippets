package com.appian.rpa.snippet.clients;

import com.appian.rpa.snippet.ConstantsWaits;
import com.appian.rpa.snippet.IBM3270Commons;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.windows.api.IWindows;


/**
 * IBM3270Commons extension for SGC client
 */
public class SGCCommonsExtended extends IBM3270Commons {
	
	/**
	 * The Constant TITLE_REGEX.
	 */
	public static final String IBM3270_LOGIN_TITLE_REGEX = "(?i)SGC verde - wc3270";
	
	
	/**
	 * Default constructor
	 * @param server
	 * @param windows
	 * @param robot
	 */
	public SGCCommonsExtended(IJidokaServer<?> server, IWindows windows, IRobot robot) {
		super(server, windows, robot);
	}

	
	/**
	 * Select all text in the screen
	 */
	public void selectAllText() {
		
		keyboard.down().pause(); 
		keyboard.control("g").pause().control("g").pause();
		
		keyboard.alt(" ").pause(ConstantsWaits.PAUSE_MENU_COPY).type("e").pause(ConstantsWaits.PAUSE_MENU_COPY).type("s").pause();
		keyboard.alt(" ").pause(ConstantsWaits.PAUSE_MENU_COPY).type("e").pause(ConstantsWaits.PAUSE_MENU_COPY).type("o").pause();
		
	}
	
	
	/**
	 * Activate a window by title
	 */
	public void activateWindow() {
		
		windows.activateWindow(IBM3270_LOGIN_TITLE_REGEX);
		
		windows.pause();
	}

}

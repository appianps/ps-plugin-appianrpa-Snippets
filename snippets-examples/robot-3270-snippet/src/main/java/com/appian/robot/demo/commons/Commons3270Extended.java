package com.appian.robot.demo.commons;

import com.appian.rpa.snippet.IBM3270Commons;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.windows.api.IWindows;


/**
 * IBM3270Commons extension with specific implementation of abstract methods
 */
public class Commons3270Extended extends IBM3270Commons {
	
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
	public Commons3270Extended(IJidokaServer<?> server, IWindows windows, IRobot robot) {
		super(server, windows, robot);
	}

	
	/**
	 * Select all text in the screen
	 */
	public void selectAllText() {
		
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

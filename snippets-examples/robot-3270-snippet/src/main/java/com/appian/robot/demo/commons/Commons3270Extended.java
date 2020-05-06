package com.appian.robot.demo.commons;

import com.appian.rpa.snippet.IBM3270Commons;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.windows.api.IWindows;


/**
 *
 */
public class Commons3270Extended extends IBM3270Commons {
	
	
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
		keyboard.down().pause(); 
		keyboard.control("a").pause();
	}

}

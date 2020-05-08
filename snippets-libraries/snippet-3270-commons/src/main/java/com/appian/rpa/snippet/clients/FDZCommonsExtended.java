package com.appian.rpa.snippet.clients;

import com.appian.rpa.snippet.IBM3270Commons;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.multios.IClient;


/**
 * IBM3270Commons extension for FanDeZhi client
 */
public class FDZCommonsExtended extends IBM3270Commons {
	
	/**
	 * The Constant WINDOW_TITLE_REGEX.
	 */
	public static final String WINDOW_TITLE_REGEX = ".*3270";
	
	/**
	 * Default X-coordinate
	 */
	private static final int MAX_COORD_X = 80;
	
	/**
	 * Default Y-coordinate
	 */
	private static final int MAX_COORD_Y = 24;
	
	
	
	/**
	 * Default constructor
	 * @param server
	 * @param client
	 * @param robot
	 */
	public FDZCommonsExtended(IClient client, IRobot robot) {
		
		super(client, robot);
		
		setMaxCoordX(MAX_COORD_X);
		setMaxCoordY(MAX_COORD_Y);
	}

	
	/**
	 * Select all text in the screen
	 */
	public void selectAllText() {
		
		moveToBottonRightCorner(); 
		keyboard.control("a").pause();
	}
	
	
	/**
	 * Activate a window by title
	 */
	public void activateWindow() {
		
		client.activateWindow(WINDOW_TITLE_REGEX);
		
		client.pause();
	}
	
	/**
	 * Move the cursor to the bottom right corner of the screen
	 */
	@Override
	public void moveToBottonRightCorner() {
		
		keyboard.down().pause(); 
		keyboard.control("g").pause().control("g").pause();
	}

	/**
	 * Split the lines of text on the screen
	 */
	@Override
	public String[] splitScreenLines(String screen) {
		return screen.split("(?<=\\G.{80})");
	}
	
	
}

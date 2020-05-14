package com.appian.rpa.snippet.clients;

import com.appian.rpa.snippet.ConstantsWaits;
import com.appian.rpa.snippet.IBM3270Commons;
import com.novayre.jidoka.client.api.IRobot;

/**
 * IBM3270Commons extension for SGC client
 */
public class SGCEmulatorManager extends IBM3270Commons {

	/**
	 * The Constant TITLE_REGEX.
	 */
	public static final String IBM3270_LOGIN_TITLE_REGEX = "(?i)SGC verde - wc3270";

	/**
	 * Line separator
	 */
	private static final String LINE_SEPARATOR = "\n";

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
	 * 
	 * @param server
	 * @param client
	 * @param robot
	 */
	public SGCEmulatorManager(IRobot robot) {

		super(robot);

		setMaxCoordX(MAX_COORD_X);
		setMaxCoordY(MAX_COORD_Y);
	}

	/**
	 * Select all text in the screen
	 */
	@Override
	public void selectAllText() {

		moveToBottonRightCorner();

		keyboard.alt(" ").pause(ConstantsWaits.PAUSE_MENU_COPY).type("e").pause(ConstantsWaits.PAUSE_MENU_COPY)
				.type("s").pause();
		keyboard.alt(" ").pause(ConstantsWaits.PAUSE_MENU_COPY).type("e").pause(ConstantsWaits.PAUSE_MENU_COPY)
				.type("o").pause();

	}

	/**
	 * Activate a window by title
	 */
	@Override
	public void activateWindow() {

		client.activateWindow(IBM3270_LOGIN_TITLE_REGEX);

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
		return screen.split(LINE_SEPARATOR);
	}

	/**
	 * Returns the window title regex
	 * 
	 * @return
	 */
	@Override
	public String getWindowTitleRegex() {
		return IBM3270_LOGIN_TITLE_REGEX;
	}
}

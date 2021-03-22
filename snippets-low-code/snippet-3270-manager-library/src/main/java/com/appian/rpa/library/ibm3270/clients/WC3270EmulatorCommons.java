package com.appian.rpa.library.ibm3270.clients;

import com.appian.rpa.library.ibm3270.ConstantsWaits;
import com.appian.rpa.library.ibm3270.IBM3270Commons;
import com.novayre.jidoka.client.api.IRobot;

/**
 * IBM3270Commons extension for wc3270
 */
public class WC3270EmulatorCommons extends IBM3270Commons {

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
	public WC3270EmulatorCommons(IRobot robot, String windowsTitle3270) {

		super(robot, windowsTitle3270);

		setMaxCoordX(MAX_COORD_X);
		setMaxCoordY(MAX_COORD_Y);
	}

	/**
	 * Select all text in the screen
	 */
	@Override
	public void selectAllText() {

		moveToBottomRightCorner();
		keyboard.control("a").pause();
	}

	/**
	 * Move the cursor to the designated coordinates
	 */
	@Override
	public void moveToCoordinates(int targetXCoodinate, int targetYCoodinate) {
		server.debug(String.format("We're moving to the coordinates (%d, %d)", targetYCoodinate, targetXCoodinate));
		activateWindow();
		keyboard.down().pause();
		moveToBottomRightCorner();
		windows.characterPause(10);
		keyboard.left(getMaxCoordX() - targetXCoodinate);
		keyboard.up(getMaxCoordY() - targetYCoodinate);
		windows.characterPause(ConstantsWaits.DEFAULT_CHARACTER_PAUSE);
	}

	/**
	 * Split the lines of text on the screen
	 */
	@Override
	public String[] splitScreenLines(String screen) {
		return screen.split("(?<=\\G.{80})");
	}

	/**
	 * Move the cursor to the bottom right corner, this must be configured as
	 * shortcut in the keymap file -> 'Ctrl<Key>g: MoveCursor(23,79)'
	 */
	private void moveToBottomRightCorner() {
		
		keyboard.control("g").pause().control("g").pause();
	}

}

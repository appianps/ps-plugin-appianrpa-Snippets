package com.appian.rpa.snippet.clients;

import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import com.appian.rpa.snippet.IBM3270Commons;
import com.novayre.jidoka.client.api.IRobot;

/**
 * IBM3270Commons extension for PCOMM emulator
 */
public class PCOMMEmulatorManager extends IBM3270Commons {

	/**
	 * The Constant WINDOW_TITLE_REGEX.
	 */
	public static final String WINDOW_TITLE_REGEX = "Session.*";

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
	public PCOMMEmulatorManager(IRobot robot) {

		super(robot);

		setMaxCoordX(MAX_COORD_X);
		setMaxCoordY(MAX_COORD_Y);
	}

	/**
	 * Select all text in the screen
	 */
	@Override
	public void selectAllText() {

		keyboard.alt("e").pause();
		keyboard.type("a").pause();

	}

	/**
	 * Activate a window by title
	 */
	@Override
	public void activateWindow() {

		client.activateWindow(WINDOW_TITLE_REGEX);

		client.pause();
	}

	/**
	 * Move the cursor to the bottom right corner of the screen
	 */
	@Override
	public void moveToBottonRightCorner() {
		// Not needed
	}

	/**
	 * Split the lines of text on the screen
	 */
	@Override
	public String[] splitScreenLines(String screen) {
		return screen.split("(?<=\\G.{80})");
	}

	/**
	 * Returns the window title regex
	 * 
	 * @return
	 */
	@Override
	public String getWindowTitleRegex() {
		return WINDOW_TITLE_REGEX;
	}

	/**
	 * Copy the selected text
	 * 
	 * @return
	 * @throws IOException
	 * @throws UnsupportedFlavorException
	 */
	@Override
	public String copyText() throws IOException, UnsupportedFlavorException {

		keyboard.alt("e").pause();
		keyboard.type("c").pause();

		return client.clipboardGet();
	}

}

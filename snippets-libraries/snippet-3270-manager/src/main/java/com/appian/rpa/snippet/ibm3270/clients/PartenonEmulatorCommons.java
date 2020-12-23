package com.appian.rpa.snippet.ibm3270.clients;

import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import com.appian.rpa.snippet.ibm3270.IBM3270Commons;
import com.novayre.jidoka.client.api.IRobot;

/**
 * Commons para partenon, This must be renames
 */
public class PartenonEmulatorCommons extends IBM3270Commons {


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
	public PartenonEmulatorCommons(IRobot robot, String windowsTitle3270) {

		super(robot, windowsTitle3270);

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
		return screen.split("\\n");
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

		return windows.clipboardGet();
	}

}

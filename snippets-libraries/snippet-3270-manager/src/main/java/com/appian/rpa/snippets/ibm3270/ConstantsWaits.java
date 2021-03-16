package com.appian.rpa.snippets.ibm3270;

/**
 * Constants class with the different types of pauses required
 */
public class ConstantsWaits {

	/**
	 * Default pause - to imitate human behavior
	 */
	public static final long DEFAULT_PAUSE = 500;

	/**
	 * One second pause after Ctrl-G
	 */
	public static final long PAUSE_AFTER_CONTROL_G = 1000;

	/**
	 * Pause typing - to imitate human behavior
	 */
	public static final long PAUSE_MENU_COPY = 100;

	/**
	 * Pause writing - to imitate human behavior
	 */
	public static final long DEFAULT_CHARACTER_PAUSE = 50;

	/**
	 * Default number of retries tying to locate a text
	 */
	public static final int DEFAULT_NUMBER_OF_RETRIES_LOCATING_TEXT = 5;

	/**
	 * High number of retries tying to locate a text
	 */
	public static final int HIGH_NUMBER_OF_RETRIES_LOCATING_TEXT = 20;

	/**
	 * Huge number of retries tying to locate a text
	 */
	public static final int HUGE_NUMBER_OF_RETRIES_LOCATING_TEXT = 40;

}

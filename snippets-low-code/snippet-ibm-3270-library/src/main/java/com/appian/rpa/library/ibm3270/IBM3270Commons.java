package com.appian.rpa.library.ibm3270;

import java.awt.Point;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.novayre.jidoka.client.api.*;
import org.apache.commons.lang3.StringUtils;

import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;
import com.novayre.jidoka.client.api.exceptions.JidokaUnsatisfiedConditionException;
import com.novayre.jidoka.windows.api.IWindows;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.ptr.IntByReference;

/**
 * Class for operating an IBM 3270 terminal
 */
public abstract class IBM3270Commons {

	/** Space character */
	private static final String SPACE = " ";

	/** Space in HTML */
	private static final String SPACE_HTML = "&#160;";

	/** Jidoka Server Instance */
	protected IJidokaServer<?> server;

	/** IRobot instance */
	private IRobot robot;
//	private IJidokaRobot jrobot;

	/** WaitFor instance */
	private IWaitFor waitFor;

	/** Trace screenshots? */
	private Boolean traceScreenshots = true;

	/** Max X-coordinate value */
	private int maxCoordX = 80;

	/** Max Y-coordinate value */
	private int maxCoordY = 24;

	/** Client Module Instance */
	protected IWindows windows;

	/** Keyboard module instance */
	protected IKeyboard keyboard;

	protected String windowsTitle3270;

	/**
	 * Instantiates a new IBM3270Commons
	 * 
	 * @param robot IRobot instance (i.e. this)
	 */
	public IBM3270Commons(IRobot robot, String windowsTitle3270) {

		this.windowsTitle3270 = windowsTitle3270;
		this.server = JidokaFactory.getServer();
		this.windows = IWindows.getInstance(robot);
		this.robot = robot;
		waitFor = windows.waitFor(robot);
		keyboard = windows.keyboard();

//		tried below out for typing special characters without success
//		this.jrobot = IJidokaRobot.getInstance(robot);
//		jrobot.setVariant(EKeyboardVariant.valueOf("ALT"));
//		keyboard = jrobot.getKeyboard();
//		sequence = jrobot.getKeyboardSequence();

	}

	/**
	 * Abstract method to select all text in screen
	 */
	public abstract void selectAllText();

	/**
	 * Activate a window by title
	 */
	public void activateWindow() {

		windows.activateWindow(windowsTitle3270);
		windows.pause(50);
	}

	public void close() throws IOException {
		server.warn("Closing Window");
		while (windows.activateWindow(windowsTitle3270)) {
			server.warn("Closing Window Activate");
			HWND whandle = windows.getWindow(windowsTitle3270).gethWnd();
			boolean result = windows.destroyWindow(whandle);
			if (result) {
				server.info("Emul 3270 closed");
			} else {
				server.info("Emul 3270 not closed. Killing process");
				windows.getWindow(windowsTitle3270);
				IntByReference PIDRef = new IntByReference();
				User32 user32 = User32.INSTANCE;
				user32.GetWindowThreadProcessId(whandle, PIDRef);
				if (PIDRef.getValue() != 0) {
					String cmd = "taskkill /F /PID " + PIDRef.getValue();
					Runtime.getRuntime().exec(cmd);
					server.info("Emul 3270 process ended.");

					windows.pause(1000);
				} else {
					server.warn("Error closing Emul 3270");
				}
			}
		}
	}

	/**
	 * Abstract method to move the cursor to the bottom right corner of the screen
	 */
	public abstract void moveToCoordinates(int targetXCoodinate, int targetYCoodinate, boolean log);

	/**
	 * Abstract method to split the lines of text on the screen
	 * 
	 * @param screen
	 */
	public abstract String[] splitScreenLines(String screen);

	/**
	 * Returns the first text it finds on the screen among those passed as a
	 * parameter
	 * 
	 * @param text Text to find
	 * @return TextInScreen object with text position
	 */
	public TextInScreen locateText(String... text) {

		return locateText(ConstantsWaits.DEFAULT_NUMBER_OF_RETRIES_LOCATING_TEXT, true, null, text);
	}

	/**
	 * Returns the first text it finds on the screen among those passed as a
	 * parameter
	 * 
	 * @param retries
	 * @param text    Text to find
	 * @return TextInScreen object with text position
	 */
	public TextInScreen locateText(int retries, String... text) {

		return locateText(retries, true, null, text);
	}

	/**
	 * Returns the first text it finds on the screen among those passed as a
	 * parameter
	 * 
	 * @param cachedScreen
	 * @param text         Text to find
	 * @return TextInScreen object with text position
	 */
	public TextInScreen locateText(List<String> cachedScreen, String... text) {

		return locateText(ConstantsWaits.DEFAULT_NUMBER_OF_RETRIES_LOCATING_TEXT, true, cachedScreen, text);
	}

	/**
	 * Returns the first text it finds on the screen among those passed as a
	 * parameter
	 * 
	 * @param retries                  Number of attempts
	 * @param throwExceptionIfnotFound Indicates if an exception is thrown if the
	 *                                 text is not found
	 * @param cachedScreen             List of lines of text on the screen
	 * @param text                     Text to find
	 * @return TextInScreen Object with text position
	 */
	public TextInScreen locateText(int retries, boolean throwExceptionIfnotFound, List<String> cachedScreen,
			String... text) {

		AtomicReference<TextInScreen> textInScreen = new AtomicReference<TextInScreen>();

		String textForDebug = "";
		if (text.length == 1) {
			textForDebug = "Searching the text  " + text[0];
		} else {
			StringBuffer sb = new StringBuffer("Searching the text  ");
			for (String t : text) {
				if (StringUtils.isNotEmpty(t)) {
					sb.append(t).append(", ");
				}
			}
			textForDebug = sb.toString().substring(0, sb.toString().length() - 2);
		}

		try {
			waitFor.wait(retries, textForDebug, false, false, () -> {

				// List of lines on screen
				List<String> screen;

				if (cachedScreen == null) {
					screen = scrapScreen();
				} else {
					screen = cachedScreen;
				}

				boolean samePage = true;
				do {
					for (String t : text) {

						if (StringUtils.isEmpty(t)) {
							continue;
						}

						Pattern p = Pattern.compile(t);

						for (int y = 0; y < screen.size(); y++) {

							Matcher m = p.matcher(screen.get(y));
							if (m.find()) {
								server.warn("Text Match " + screen.get(y));
								int x = screen.get(y).indexOf(t);

								TextInScreen res = new TextInScreen();
								res.setText(t);
								res.setPointInScreen(new Point(x + 1, y + 1));
								res.setScreen(screen);
								textInScreen.set(res);

								return true;
							}
						}
					}

				} while (!samePage);

				windows.pause(500);
				return false;
			});

		} catch (JidokaUnsatisfiedConditionException e) {
			e.printStackTrace();
			;
		}

		if (textInScreen.get() == null) {
			if (throwExceptionIfnotFound) {
				throw new JidokaFatalException(String.format("Text not found  %s", textForDebug));
			} else {
				server.debug(String.format("Text not found  %s", textForDebug));
				return null;
			}
		}

		server.debug(String.format("Text found %s in (%d, %d)", textInScreen.get().getText(),
				(int) textInScreen.get().getPointInScreen().getX(),
				(int) textInScreen.get().getPointInScreen().getY()));

		return textInScreen.get();
	}

	/**
	 * Gets all the text on the active screen
	 * 
	 * @return List of lines on screen
	 */
	public List<String> scrapScreen() {

		List<String> res = null;
		String screen = null;

		try {

			activateWindow();

//			server.debug("Getting all the text on the active screen");

			windows.characterPause(300);

			selectAllText();

			windows.pause(1000);

			screen = copyText();

			if (screen == null) {

				throw new JidokaFatalException("Unable to read the screen");
			}
//			server.warn("Text " + screen);

			String[] lines = splitScreenLines(screen);

			res = Arrays.asList(lines);

			if (traceScreenshots) {
				logScreen(res, false);
			}

			keyboard.home();

		} catch (Exception e) {
			throw new JidokaFatalException(e.getMessage(), e);
		}
		finally {
			windows.characterPause(0);
		}

		return res;
	}

	/**
	 * Performs a wait until the text disappears
	 * 
	 * @param text Text expected to disappear
	 */
	public void waitTillTextDisappears(String text) {

		waitTillTextDisappears(ConstantsWaits.DEFAULT_NUMBER_OF_RETRIES_LOCATING_TEXT, text);
	}

	/**
	 * Performs a wait until the text disappears, as many attempts are made as the
	 * retries parameter indicates
	 * 
	 * @param retries Number of attempts
	 * @param text    Text expected to disappear
	 */
	public void waitTillTextDisappears(int retries, String text) {

		boolean disappearedText = false;

		try {
			disappearedText = waitFor.wait(retries, "Waiting for the text to disappear  " + text, false, false, () -> {
				if (locateText(1, false, null, null, text) == null) {
					return true;
				}
				;
				windows.pause(1000);
				return false;
			});
		} catch (JidokaUnsatisfiedConditionException e) {
			;
		}

		if (!disappearedText) {
			throw new JidokaFatalException("The text has not disappeared :" + text);
		}
	}

	/**
	 * Moves the cursor to the start of the indicated text, makes a correction using
	 * the offset parameters
	 * 
	 * @param text    Text to which the course should be moved
	 * @param offsetX X-coordinate adjustment
	 * @param offsetY Y-coordinate adjustment
	 */
	public void moveToCoordinates(String text, int offsetX, int offsetY, int retries) {

		if (text == null) {
			throw new JidokaFatalException("The coordinates to move to are null");
		}
		moveToCoordinates(locateText(retries, text), offsetX, offsetY);
	}

	/**
	 * Moves the cursor to the start of the indicated text, makes a correction using
	 * the offset parameters
	 * 
	 * @param textInScreen TestInScreen instance
	 * @param offsetX      X-coordinate adjustment
	 * @param offsetY      Y-coordinate adjustment
	 */

	private void moveToCoordinates(TextInScreen textInScreen, int offsetX, int offsetY) {

		if (textInScreen == null) {
			throw new JidokaFatalException("The coordinates to move to are null");
		}

		moveToCoordinates(textInScreen.getPointInScreen(), offsetX, offsetY);
	}

	/**
	 * Move the cursor to a point on the screen
	 * 
	 * @param pointOnScreen Class Point instance
	 * @param offsetX       X-coordinate adjustment
	 * @param offsetY       Y-coordinate adjustment
	 */
	private void moveToCoordinates(Point pointOnScreen, int offsetX, int offsetY) {
		if (pointOnScreen == null) {
			throw new JidokaFatalException("The coordinates to move to are null");
		}

		int targetXCoodinate = (int) (pointOnScreen.getX() + offsetX);
		int targetYCoodinate = (int) (pointOnScreen.getY() + offsetY);
		moveToCoordinates(targetXCoodinate, targetYCoodinate,false);
	}

	/**
	 * Write the text on screen
	 * 
	 * @param text Text to write
	 * @return IBM3270Commons instance
	 */
	public IBM3270Commons write(String text) {

		return write(text, true);
	}

	/**
	 * Write the text on the screen
	 * 
	 * @param text Text to write
	 * @param log  Indicates if the text to be written is shown in the log
	 * @return IBM3270Commons instance
	 */
	public IBM3270Commons write(String text, Boolean log) {

		activateWindow();

		if (text == null) {
			text = "";
		}

		if (log) {
			server.debug(String.format("Writing text %s", text));
		}
		if(text.matches("^[A-Za-z0-9]*$")){
			windows.characterPause(25);
			keyboard.type(text);
			if (log) {
				server.debug("Typing all text "+ text + " with pause " + windows.getCharacterPause());
			}
			windows.characterPause(0);
		} else{
			if (log) {
				server.debug("Typing single characters "+ text);
			}
			for (int i = 0; i < text.length(); i++) {
				String toPressString = text.substring(i, i + 1);

				char toPressChar = toPressString.charAt(0);

				if (toPressString.matches("[A-Za-z0-9]")) {
					keyboard.type(toPressString);
				} else {
					if (log) {
						server.debug(String.format("Writing special character %s", toPressChar));
					}
					writeSpecial(toPressChar);
				}

				windows.pause(25);
			}
		}
		return this;
	}

	/**
	 * Write Special Character
	 *
	 * @param toPressChar
	 * @return IBM3270Commons instance
	 */
	public void writeSpecial(char toPressChar) {
		switch (toPressChar) {

			//	require shift key held down
			case '~': windows.keyboardSequence().pressShift().press(KeyEvent.VK_BACK_QUOTE).release(KeyEvent.VK_BACK_QUOTE).releaseShift().apply(); break;
			case '!': windows.keyboardSequence().pressShift().press(KeyEvent.VK_1).release(KeyEvent.VK_1).releaseShift().apply(); break;
			case '@': windows.keyboardSequence().pressShift().press(KeyEvent.VK_2).release(KeyEvent.VK_2).releaseShift().apply(); break;
			case '#': windows.keyboardSequence().pressShift().press(KeyEvent.VK_3).release(KeyEvent.VK_3).releaseShift().apply(); break;
			case '$': windows.keyboardSequence().pressShift().press(KeyEvent.VK_4).release(KeyEvent.VK_4).releaseShift().apply(); break;
			case '%': windows.keyboardSequence().pressShift().press(KeyEvent.VK_5).release(KeyEvent.VK_5).releaseShift().apply(); break;
			case '^': windows.keyboardSequence().pressShift().press(KeyEvent.VK_6).release(KeyEvent.VK_6).releaseShift().apply(); break;
			case '&': windows.keyboardSequence().pressShift().press(KeyEvent.VK_7).release(KeyEvent.VK_7).releaseShift().apply(); break;
			case '*': windows.keyboardSequence().pressShift().press(KeyEvent.VK_8).release(KeyEvent.VK_8).releaseShift().apply(); break;
			case '(': windows.keyboardSequence().pressShift().press(KeyEvent.VK_9).release(KeyEvent.VK_9).releaseShift().apply(); break;
			case ')': windows.keyboardSequence().pressShift().press(KeyEvent.VK_0).release(KeyEvent.VK_0).releaseShift().apply(); break;
			case '_': windows.keyboardSequence().pressShift().press(KeyEvent.VK_MINUS).release(KeyEvent.VK_MINUS).releaseShift().apply(); break;
			case '+': windows.keyboardSequence().pressShift().press(KeyEvent.VK_EQUALS).release(KeyEvent.VK_EQUALS).releaseShift().apply(); break;
			case '{': windows.keyboardSequence().pressShift().press(KeyEvent.VK_OPEN_BRACKET).release(KeyEvent.VK_OPEN_BRACKET).releaseShift().apply(); break;
			case '}': windows.keyboardSequence().pressShift().press(KeyEvent.VK_CLOSE_BRACKET).release(KeyEvent.VK_CLOSE_BRACKET).releaseShift().apply(); break;
			case '|': windows.keyboardSequence().pressShift().press(KeyEvent.VK_BACK_SLASH).release(KeyEvent.VK_BACK_SLASH).releaseShift().apply(); break;
			case ':': windows.keyboardSequence().pressShift().press(KeyEvent.VK_SEMICOLON).release(KeyEvent.VK_SEMICOLON).releaseShift().apply(); break;
			case '"': windows.keyboardSequence().pressShift().press(KeyEvent.VK_QUOTE).release(KeyEvent.VK_QUOTE).releaseShift().apply(); break;
			case '<': windows.keyboardSequence().pressShift().press(KeyEvent.VK_COMMA).release(KeyEvent.VK_COMMA).releaseShift().apply(); break;
			case '>': windows.keyboardSequence().pressShift().press(KeyEvent.VK_PERIOD).release(KeyEvent.VK_PERIOD).releaseShift().apply(); break;
			case '?': windows.keyboardSequence().pressShift().press(KeyEvent.VK_SLASH).release(KeyEvent.VK_SLASH).releaseShift().apply(); break;

			//	do not require shift key held down
			case '-': windows.keyboardSequence().press(KeyEvent.VK_MINUS).release(KeyEvent.VK_MINUS).apply(); break;
			case '=': windows.keyboardSequence().press(KeyEvent.VK_EQUALS).release(KeyEvent.VK_EQUALS).apply(); break;
			case '`': windows.keyboardSequence().press(KeyEvent.VK_BACK_QUOTE).release(KeyEvent.VK_BACK_QUOTE).apply(); break;
			case '[': windows.keyboardSequence().press(KeyEvent.VK_OPEN_BRACKET).release(KeyEvent.VK_OPEN_BRACKET).apply(); break;
			case ']': windows.keyboardSequence().press(KeyEvent.VK_CLOSE_BRACKET).release(KeyEvent.VK_CLOSE_BRACKET).apply(); break;
			case '\\': windows.keyboardSequence().press(KeyEvent.VK_BACK_SLASH).release(KeyEvent.VK_BACK_SLASH).apply(); break;
			case ';': windows.keyboardSequence().press(KeyEvent.VK_SEMICOLON).release(KeyEvent.VK_SEMICOLON).apply(); break;
			case '\'': windows.keyboardSequence().press(KeyEvent.VK_QUOTE).release(KeyEvent.VK_QUOTE).apply(); break;
			case ',': windows.keyboardSequence().press(KeyEvent.VK_COMMA).release(KeyEvent.VK_COMMA).apply(); break;
			case '.': windows.keyboardSequence().press(KeyEvent.VK_PERIOD).release(KeyEvent.VK_PERIOD).apply(); break;
			case '/': windows.keyboardSequence().press(KeyEvent.VK_SLASH).release(KeyEvent.VK_SLASH).apply(); break;
			case ' ': windows.keyboardSequence().press(KeyEvent.VK_SPACE).release(KeyEvent.VK_SPACE).apply(); break;
			}
		}

	/**
	 * Press PF
	 * 
	 * @param pf
	 * @return IWindows instance
	 */
	public IWindows pressPF(int pf) {

		activateWindow();

		server.debug(String.format("Press PF %d", pf));

		if (pf > 12) {

			windows.keyboardSequence().pressShift().typeFunction(pf - 12).releaseShift().apply();

		} else {

			keyboard.function(pf);
		}

		windows.pause(5);

		return windows;
	}

	/**
	 * Press enter
	 * 
	 * @return IBM3270Commons instance
	 */
	public IBM3270Commons enter() {

		activateWindow();

		server.debug("Pressing enter");

		keyboard.enter();

		return this;
	}

	/**
	 * Press enter
	 * 
	 * @return IBM3270Commons instance
	 */
	public IBM3270Commons control() {
		activateWindow();
		keyboard.control(null);
		return this;
	}

	/**
	 * Press Tab
	 * 
	 * @return IBM3270Commons instance
	 */
	public IBM3270Commons tab() {

		activateWindow();

		server.debug("Pressing tab");

		keyboard.tab();

		return this;
	}

	/**
	 * Press left
	 * 
	 * @return IBM3270Commons instance
	 */
	public IBM3270Commons pressLeft() {

		return pressLeft(1);
	}

	/**
	 * Press left
	 * 
	 * @param repetition Number of times to perform the action
	 * @return IBM3270Commons instance
	 */
	public IBM3270Commons pressLeft(int repetition) {

		activateWindow();

		keyboard.left(repetition);

		return this;
	}

	/**
	 * Press right
	 * 
	 * @return IBM3270Commons instance
	 */
	public IBM3270Commons pressRight() {

		return pressRight(1);
	}

	/**
	 * Press right
	 * 
	 * @param repetition Number of times to perform the action
	 * @return IBM3270Commons instance
	 */
	public IBM3270Commons pressRight(int repetition) {

		activateWindow();

		keyboard.right(repetition);

		return this;
	}

	/**
	 * Press down
	 * 
	 * @return IBM3270Commons instance
	 */
	public IBM3270Commons pressDown() {

		return pressDown(1);
	}

	/**
	 * Press down
	 * 
	 * @param repetition Number of times to perform the action
	 * @return IBM3270Commons instance
	 */
	public IBM3270Commons pressDown(int repetition) {

		activateWindow();

		keyboard.down(repetition);

		return this;
	}

	/**
	 * Press up
	 * 
	 * @return IBM3270Commons instance
	 */
	public IBM3270Commons pressUp() {

		return pressUp(1);
	}

	/**
	 * Press Up
	 * 
	 * @param repetition Number of times to perform the action
	 * @return IBM3270Commons instance
	 */
	public IBM3270Commons pressUp(int repetition) {

		activateWindow();

		keyboard.up(repetition);

		return this;
	}

	/**
	 * Gets Max X-coordinate value
	 */
	public int getMaxCoordX() {
		return maxCoordX;
	}

	/**
	 * Sets Max X-coordinate value
	 */
	public void setMaxCoordX(int maxCoordX) {
		this.maxCoordX = maxCoordX;
	}

	/**
	 * Gets Max Y-coordinate value
	 */
	public int getMaxCoordY() {
		return maxCoordY;
	}

	/**
	 * Sets Max Y-coordinate value
	 */
	public void setMaxCoordY(int maxCoordY) {
		this.maxCoordY = maxCoordY;
	}

	/**
	 * Copy the selected text
	 * 
	 * @return String with all copied text
	 * @throws IOException
	 * @throws UnsupportedFlavorException
	 */
	public String copyText() throws IOException, UnsupportedFlavorException {
		return windows.copyAndGet();
	}

	/**
	 * Sends all screen text to the log
	 * 
	 * @param screen List of lines on screen
	 * @param warn   Log level
	 */
	private void logScreen(List<String> screen, boolean warn) {

		logLine("--------------------------------------------------------------------------------", warn);

		for (String line : screen) {

			if (JidokaFactory.getServer().isConnected()) {

				line = StringUtils.replaceAll(line, SPACE, SPACE_HTML);
			}

			logLine(line, warn);
		}

		logLine("--------------------------------------------------------------------------------", warn);
	}

	/**
	 * Sends all lines of text to the log
	 * 
	 * @param line Line of text on screen
	 * @param warn Log level
	 */
	private void logLine(String line, boolean warn) {

		if (warn) {
			server.warn(line);
		} else {
			server.trace(line);
		}
	}

	/**
	 * Return a robot instance
	 * 
	 * @return robot instance
	 */
	public IRobot getRobot() {
		return robot;
	}

	/**
	 * Set a robot instance
	 * 
	 * @param robot
	 */
	public void setRobot(IRobot robot) {
		this.robot = robot;
	}

	/**
	 * Get traceScreenshot field
	 * 
	 * @return
	 */
	public Boolean getTraceScreenshots() {
		return traceScreenshots;
	}

	/**
	 * Set traceScreenshot field
	 * 
	 * @param traceScreenshots
	 */
	public void setTraceScreenshots(Boolean traceScreenshots) {
		this.traceScreenshots = traceScreenshots;
	}

}

package com.appian.rpa.snippet;

import java.awt.Point;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IKeyboard;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.IWaitFor;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;
import com.novayre.jidoka.client.api.exceptions.JidokaUnsatisfiedConditionException;
import com.novayre.jidoka.client.api.multios.IClient;

/**
 * Class for operating an IBM 3270 client
 */
public abstract class IBM3270Commons {

	/**
	 * Space character
	 */
	private static final String SPACE = " ";

	/**
	 * Space in HTML
	 */
	private static final String SPACE_HTML = "&#160;";

	/**
	 * Jidoka Server Instance
	 */
	protected IJidokaServer<?> server;

	/**
	 * Client Module Instance
	 */
	protected IClient client;

	/**
	 * IRobot instance
	 */
	protected IRobot robot;

	/**
	 * Waitfor instance
	 */
	protected IWaitFor waitFor;

	/**
	 * Keyboard module instance
	 */
	protected IKeyboard keyboard;

	/**
	 * Trace screenshots?
	 */
	private Boolean traceScreenshots = true;

	/**
	 * Max X-coordinate value
	 */
	private int maxCoordX = 80;

	/**
	 * Max Y-coordinate value
	 */
	private int maxCoordY = 24;

	/**
	 * Instantiates a new IBM3270Commons
	 */
	public IBM3270Commons(IClient client, IRobot robot) {

		this.server = JidokaFactory.getServer();
		this.client = client;
		this.robot = robot;
		waitFor = client.waitFor(robot);
		keyboard = client.keyboard();
	}

	/**
	 * Abstract method to select all text in screen
	 */
	public abstract void selectAllText();

	/**
	 * Abstract method to activate a window by its title
	 */
	public abstract void activateWindow();

	/**
	 * Abstract method to move the cursor to the bottom right corner of the screen
	 */
	public abstract void moveToBottonRightCorner();

	/**
	 * Abstract method to split the lines of text on the screen
	 * 
	 * @param screen
	 */
	public abstract String[] splitScreenLines(String screen);

	/**
	 * Returns the window title regex
	 * 
	 * @return
	 */
	public abstract String getWindowTitleRegex();

	/**
	 * Returns the first text it finds on the screen among those passed as a
	 * parameter
	 * 
	 * @param text
	 * @return
	 */
	public TextInScreen locateText(String... text) {

		return locateText(ConstantsWaits.DEFAULT_NUMBER_OF_RETRIES_LOCATING_TEXT, true, null, text);
	}

	/**
	 * Returns the first text it finds on the screen among those passed as a
	 * parameter
	 * 
	 * @param retries
	 * @param text
	 * @return
	 */
	public TextInScreen locateText(int retries, String... text) {

		return locateText(retries, true, null, text);
	}

	/**
	 * Returns the first text it finds on the screen among those passed as a
	 * parameter
	 * 
	 * @param cachedScreen
	 * @param text
	 * @return
	 */
	public TextInScreen locateText(List<String> cachedScreen, String... text) {

		return locateText(ConstantsWaits.DEFAULT_NUMBER_OF_RETRIES_LOCATING_TEXT, true, cachedScreen, text);
	}

	/**
	 * Returns the first text it finds on the screen among those passed as a
	 * parameter
	 * 
	 * @param retries
	 * @param throwExceptionIfnotFound
	 * @param cachedScreen
	 * @param pagination
	 * @param text
	 * @return
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

				// List of lines in screen
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
								int x = screen.get(y).indexOf(t);

								TextInScreen res = new TextInScreen();
								res.setText(t);
								res.setPointInScreen(new Point(x + 1, y - 1));
								res.setScreen(screen);
								textInScreen.set(res);

								return true;
							}
						}
					}

				} while (!samePage);

				client.pause(1000);
				return false;
			});

		} catch (JidokaUnsatisfiedConditionException e) {
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
				(int) textInScreen.get().getPointInScreen().getY(),
				(int) textInScreen.get().getPointInScreen().getX()));

		return textInScreen.get();
	}

	/**
	 * Gets all the text on the active screen
	 * 
	 * @return
	 */
	public List<String> scrapScreen() {

		List<String> res = null;
		String screen = null;

		try {

			activateWindow();

			server.debug("Getting all the text on the active screen");

			selectAllText();

			client.pause(1000);

			screen = copyText();

			if (screen == null) {

				throw new JidokaFatalException("Unable to read the screen");
			}

			String[] lines = splitScreenLines(screen);

			res = Arrays.asList(lines);

			if (traceScreenshots) {
				logScreen(res, false);
			}

			keyboard.home();

		} catch (Exception e) {
			throw new JidokaFatalException(e.getMessage(), e);
		}

		return res;
	}

	/**
	 * Press PF
	 * 
	 * @param pf
	 * @return
	 */
	public IClient pressPF(int pf) {

		activateWindow();

		server.debug(String.format("Press PF %d", pf));

		if (pf > 12) {

			client.keyboardSequence().pressShift().typeFunction(pf - 12).releaseShift().apply();

		} else {

			keyboard.function(pf);
		}

		client.pause();

		return client;
	}

	/**
	 * Performs a wait until the text disappears
	 * 
	 * @param text
	 */
	public void waitTillTextDisappears(String text) {

		waitTillTextDisappears(ConstantsWaits.DEFAULT_NUMBER_OF_RETRIES_LOCATING_TEXT, text);
	}

	/**
	 * Performs a wait until the text disappears, as many attempts are made as the
	 * retries parameter indicates
	 * 
	 * @param retries
	 * @param text
	 */
	public void waitTillTextDisappears(int retries, String text) {

		boolean disappearedText = false;

		try {
			disappearedText = waitFor.wait(retries, "Waiting for the text to disappear  " + text, false, false, () -> {
				if (locateText(1, false, null, null, text) == null) {
					return true;
				}
				;
				client.pause(1000);
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
	 * @param text
	 * @param offsetX
	 * @param offsetY
	 */
	public void moveToCoodinates(String text, int offsetX, int offsetY) {

		if (text == null) {

			throw new JidokaFatalException("The coordinates to move to are null");
		}

		moveToCoodinates(locateText(text), offsetX, offsetY);
	}

	/**
	 * Moves the cursor to the start of the indicated text, makes a correction using
	 * the offset parameters
	 * 
	 * @param textInScreen
	 * @param offsetX
	 * @param offsetY
	 */
	public void moveToCoodinates(TextInScreen textInScreen, int offsetX, int offsetY) {

		if (textInScreen == null) {

			throw new JidokaFatalException("The coordinates to move to are null");
		}

		moveToCoodinates(textInScreen.getPointInScreen(), offsetX, offsetY);
	}

	/**
	 * Move the cursor to a point in the screen
	 * 
	 * @param pointInScreen
	 * @param offsetX
	 * @param offsetY
	 */
	public void moveToCoodinates(Point pointInScreen, int offsetX, int offsetY) {

		activateWindow();

		if (pointInScreen == null) {

			throw new JidokaFatalException("The coordinates to move to are null");
		}

		int targetXCoodinate = (int) (pointInScreen.getX() + offsetX);
		int targetYCoodinate = (int) (pointInScreen.getY() + offsetY);

		server.debug(String.format("We're moving to the coordinates (%d, %d)", targetYCoodinate, targetXCoodinate));

		moveToBottonRightCorner();

		client.characterPause(10);
		keyboard.left(getMaxCoordX() - targetXCoodinate);
		keyboard.up(getMaxCoordY() - targetYCoodinate);
		client.characterPause(ConstantsWaits.DEFAULT_CHARACTER_PAUSE);
	}

	/**
	 * Write the text on screen
	 * 
	 * @param text
	 * @return
	 */
	public IBM3270Commons write(String text) {

		return write(text, true);
	}

	/**
	 * Write the text on screen
	 * 
	 * @param text Text to write
	 * @param log  Indicates if the text to be written is shown in the log
	 * @return
	 */
	public IBM3270Commons write(String text, Boolean log) {

		activateWindow();

		if (text == null) {
			text = "";
		}

		if (log) {
			server.debug(String.format("Writing text %s", text));
		}

		keyboard.type(text).pause();

		return this;
	}

	/**
	 * Press enter
	 * 
	 * @return
	 */
	public IBM3270Commons enter() {

		activateWindow();

		server.debug("Pressing enter");

		keyboard.enter().pause();

		return this;
	}

	/**
	 * Press Tab
	 * 
	 * @return
	 */
	public IBM3270Commons tab() {

		activateWindow();

		server.debug("Pressing tab");

		keyboard.tab().pause();

		return this;
	}

	/**
	 * Press left
	 * 
	 * @return
	 */
	public IBM3270Commons pressLeft() {

		return pressLeft(1);
	}

	/**
	 * Press left
	 * 
	 * @param repetition
	 * @return
	 */
	public IBM3270Commons pressLeft(int repetition) {

		activateWindow();

		keyboard.left(repetition).pause();

		return this;
	}

	/**
	 * Press right
	 * 
	 * @return
	 */
	public IBM3270Commons pressRight() {

		return pressRight(1);
	}

	/**
	 * Press right
	 * 
	 * @param repetition
	 * @return
	 */
	public IBM3270Commons pressRight(int repetition) {

		activateWindow();

		keyboard.right(repetition).pause();

		return this;
	}

	/**
	 * Press down
	 * 
	 * @return
	 */
	public IBM3270Commons pressDown() {

		return pressDown(1);
	}

	/**
	 * Press down
	 * 
	 * @param repetition
	 * @return
	 */
	public IBM3270Commons pressDown(int repetition) {

		activateWindow();

		keyboard.down(repetition).pause();

		return this;
	}

	/**
	 * Press up
	 * 
	 * @return
	 */
	public IBM3270Commons pressUp() {

		return pressUp(1);
	}

	/**
	 * Press Up
	 * 
	 * @param repetition
	 * @return
	 */
	public IBM3270Commons pressUp(int repetition) {

		activateWindow();

		keyboard.up(repetition).pause();

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
	 * @return
	 * @throws IOException
	 * @throws UnsupportedFlavorException
	 */
	public String copyText() throws IOException, UnsupportedFlavorException {
		return client.copyAndGet();
	}

	/**
	 * Sends all screen text to the log
	 * 
	 * @param screen
	 * @param warn
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
	 * Sends all line text to the log
	 * 
	 * @param line
	 * @param warn Log level
	 */
	private void logLine(String line, boolean warn) {

		if (warn) {
			server.warn(line);
		} else {
			server.trace(line);
		}
	}

	public IRobot getRobot() {
		return robot;
	}

	public void setRobot(IRobot robot) {
		this.robot = robot;
	}

	public Boolean getTraceScreenshots() {
		return traceScreenshots;
	}

	public void setTraceScreenshots(Boolean traceScreenshots) {
		this.traceScreenshots = traceScreenshots;
	}

}

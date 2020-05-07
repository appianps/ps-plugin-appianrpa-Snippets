package com.appian.rpa.snippet;

import java.awt.Point;
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
import com.novayre.jidoka.windows.api.IWindows;


/**
 * Class for operating an IBM 3270 terminal
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
	 * X-coordinate
	 */
	private static final int MAX_COORD_X = 80;
	
	/**
	 * Y-coordinate
	 */
	private static final int MAX_COORD_Y = 48;
	
	/**
	 * Line separator
	 */
	private static final String LINE_SEPARATOR = "\n";

	/**
	 * Jidoka Server Instance
	 */
	protected IJidokaServer<?> server;

	/**
	 * Windows Server Instance
	 */
	protected IWindows windows;
	
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
	 * Mock Page
	 */
	private String mockPage;
	
	/**
	 * Trace screenshots?
	 */
	private Boolean traceScreenshots = true;
	
	
	
	/**
	 * Instantiates a new IBM3270Commons 
	 */
	public IBM3270Commons(IJidokaServer<?> server, IWindows windows, IRobot robot) {
		
		this.server = server;
		this.windows = windows; 
		this.robot = robot;
		waitFor = windows.waitFor(robot);
		keyboard = windows.keyboard();
	}
	
	/**
	 * Abstract method to select all text in screen
	 */
	public abstract void selectAllText();

	/**
	 * Abstract method to activate a window by its title
	 */
	public abstract void activateWindow() ;
	
	
	/**
	 * Returns the first text it finds on the screen among those passed as a parameter
	 * @param text
	 * @return
	 */
	public TextInScreen locateText(String... text) {
			
			return locateText(ConstantsWaits.DEFAULT_NUMBER_OF_RETRIES_LOCATING_TEXT, true,  null, text);
	}
	
	
	/**
	 * Returns the first text it finds on the screen among those passed as a parameter
	 * @param retries
	 * @param text
	 * @return
	 */
	public TextInScreen locateText(int retries, String... text) {
		
		return locateText(retries, true, null, text);
	}

	
	/**
	 * Returns the first text it finds on the screen among those passed as a parameter
	 * @param cachedScreen
	 * @param text
	 * @return
	 */
	public TextInScreen locateText(List<String> cachedScreen, String... text) {
		
		return locateText(ConstantsWaits.DEFAULT_NUMBER_OF_RETRIES_LOCATING_TEXT, true, cachedScreen, text);
	}

	
	/**
	 * Returns the first text it finds on the screen among those passed as a parameter
	 * @param retries
	 * @param throwExceptionIfnotFound
	 * @param cachedScreen
	 * @param pagination
	 * @param text
	 * @return
	 */
	public TextInScreen locateText(int retries, boolean throwExceptionIfnotFound, List<String> cachedScreen, String... text) {
		
		AtomicReference<TextInScreen> textInScreen = new AtomicReference<TextInScreen>();
		
		String textForDebug = "";
		if(text.length == 1) {
			textForDebug = "Searching the text  " + text[0];
		} else {
			StringBuffer sb = new StringBuffer("Searching the text  ");
			for(String t : text) {
				if(StringUtils.isNotEmpty(t)) {
					sb.append(t).append(", ");
				}
			}
			textForDebug = sb.toString().substring(0, sb.toString().length()-2);
		}
		
 		try {
			waitFor.wait(retries, textForDebug, false, false,
				() -> {

					// List of lines in screen
					List<String> screen;
					
					if(cachedScreen == null) {
						screen = scrapScreen();
					} else {
						screen = cachedScreen;
					}	
					
					boolean samePage = true;
					do {
						for(String t : text) {
							
							if(StringUtils.isEmpty(t)) {
								continue;
							}
							
							Pattern p = Pattern.compile(t);
							
							// Line 0 is the menu bar
							for(int y=1; y < screen.size(); y++) {
								
								Matcher m = p.matcher(screen.get(y));
								if(m.find()) {
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
					
					} while(!samePage);
					windows.pause(1000);
					return false;
				});
		} catch (JidokaUnsatisfiedConditionException e) {;}
		
		if(textInScreen.get() == null) {
			if(throwExceptionIfnotFound) {
				throw new JidokaFatalException(String.format("Text not found  %s", textForDebug));
			} else {
				server.debug(String.format("Text not found  %s", textForDebug));
				return null;
			}
		}
		
		server.debug(String.format("Text found %s in (%d, %d)", textInScreen.get().getText(), (int)textInScreen.get().getPointInScreen().getY(), (int)textInScreen.get().getPointInScreen().getX()));
		
		return textInScreen.get();
	}

	
	/**
	 * Gets all the text on the active screen
	 * @return
	 */
	public List<String> scrapScreen() {
		
		String screen = null;
		
		activateWindow();
		
		server.debug("Getting all the text on the active screen");
		
		selectAllText();
		
		windows.pause(500);
		
		try {
			screen = windows.copyAndGet();
		} catch (Exception e) {
			throw new JidokaFatalException(e.getMessage(), e);
		}
		
		if(screen == null) {
			
			throw new JidokaFatalException("Unable to read the screen");
		}
		
		String[] lines = screen.split(LINE_SEPARATOR);
		
		List<String> res =  Arrays.asList(lines);
		
		if(traceScreenshots) {
			logScreen(res, false);
		}
		
		keyboard.home();
		
		return res;
	}

	
	/**
	 * Press PF 
	 * @param pf
	 * @return
	 */
	public IWindows pressPF(int pf) {

		activateWindow();
		
		server.debug(String.format("Press PF %d", pf));
		
		if(pf>12) {
			
			windows.keyboardSequence().pressShift().typeFunction(pf-12).releaseShift().apply();
		} else {
			
			keyboard.function(pf);
		}
		
		windows.pause();
		
		return windows;
	}
	
	/**
	 * Performs a wait until the text disappears
	 * @param text
	 */
	public void waitTillTextDisappears(String text) {

		waitTillTextDisappears(ConstantsWaits.DEFAULT_NUMBER_OF_RETRIES_LOCATING_TEXT, text);
	}
	
	
	/**
	 * Performs a wait until the text disappears,
	 * as many attempts are made as the retries parameter indicates
	 * @param retries
	 * @param text
	 */
	public void waitTillTextDisappears(int retries, String text) {
		
		boolean disappearedText = false;
		
		try {
			disappearedText = waitFor.wait(retries, "Waiting for the text to disappear  " + text, false, false, () -> {
					if(locateText(1, false, null, null, text) == null) {
						return true;
					};
					windows.pause(1000);
					return false;
				});
		} catch (JidokaUnsatisfiedConditionException e) {;}
		
		if(!disappearedText) {
			throw new JidokaFatalException("The text has not disappeared :"+ text);
		}
	}

	
	public void moveToCoodinates(String text, int offsetX, int offsetY) {
		
		if(text == null) {
			
			throw new JidokaFatalException("The coordinates to move to are null");
		}
		
		moveToCoodinates(locateText(text), offsetX, offsetY);
	}

	public void moveToCoodinates(TextInScreen textInScreen, int offsetX, int offsetY) {
		
		if(textInScreen == null) {
			
			throw new JidokaFatalException("The coordinates to move to are null");
		}
		
		moveToCoodinates(textInScreen.getPointInScreen(), offsetX, offsetY);
	}
	
	
	/**
	 * Move the cursor to a point in the screen
	 * @param pointInScreen
	 * @param offsetX
	 * @param offsetY
	 */
	public void moveToCoodinates(Point pointInScreen, int offsetX, int offsetY) {
		
		activateWindow();
		
		if(pointInScreen == null) {
		
			throw new JidokaFatalException("The coordinates to move to are null");
		}
		
		int targetXCoodinate = (int) (pointInScreen.getX() + offsetX);
		int targetYCoodinate = (int) (pointInScreen.getY() + offsetY);

		server.debug(String.format("We're moving to the coordinates (%d, %d)", targetYCoodinate, targetXCoodinate));
		
		keyboard.control("g").pause();
		
		windows.characterPause(10);
		keyboard.left(MAX_COORD_X - targetXCoodinate);
		keyboard.up(MAX_COORD_Y - targetYCoodinate);
		windows.characterPause(ConstantsWaits.DEFAULT_CHARACTER_PAUSE);
	}
	
	/**
	 * Write the text on screen
	 * @param text
	 * @return
	 */
	public IBM3270Commons write(String text) {
		
		activateWindow();
		
		if(text == null) {
			
			text = "";
		}
		
		server.debug(String.format("Writing text %s", text));
		
		keyboard.type(text).pause();
		
		return this;
	}
	
	
	
	/**
	 * Press enter
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
	 * @param repetition
	 * @return
	 */
	public IBM3270Commons pressLeft() {
		
		return pressLeft(1);
	}
	
	/**
	 * Press left
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
	 * @param repetition
	 * @return
	 */
	public IBM3270Commons pressRight() {
		
		return pressRight(1);
	}
	
	/**
	 * Press right
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
	 * @param repetition
	 * @return
	 */
	public IBM3270Commons pressDown() {
		
		return pressDown(1);
	}
	
	/**
	 * Press down
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
	 * @param repetition
	 * @return
	 */
	public IBM3270Commons pressUp() {
		
		return pressUp(1);
	}
	
	/**
	 * Press Up
	 * @param repetition
	 * @return
	 */
	public IBM3270Commons pressUp(int repetition) {
		
		activateWindow();
		
		keyboard.up(repetition).pause();
		
		return this;
	}
	
	
	
	
	/**
	 * Sends all screen text to the log
	 * @param screen
	 * @param warn
	 */
	private void logScreen(List<String> screen, boolean warn) {
		
		logLine("--------------------------------------------------------------------------------", warn);
		
		for(String line : screen) {
			
			if(JidokaFactory.getServer().isConnected()) {
			
				line = StringUtils.replaceAll(line, SPACE, SPACE_HTML);
			}
			
			logLine(line, warn);
		}
		
		logLine("--------------------------------------------------------------------------------", warn);
	}

	/**
	 * Sends all line text to the log
	 * @param line
	 * @param warn Log level
	 */
	private void logLine(String line, boolean warn) {
		
		if(warn) {
			server.warn(line);
		} else {
			server.trace(line);
		}
	}
	
	
	
	public IJidokaServer<?> getServer() {
		return server;
	}

	public void setServer(IJidokaServer<?> server) {
		this.server = server;
	}

	public IWindows getWindows() {
		return windows;
	}

	public void setWindows(IWindows windows) {
		this.windows = windows;
	}

	public IRobot getRobot() {
		return robot;
	}

	public void setRobot(IRobot robot) {
		this.robot = robot;
	}

	public String getMockPage() {
		return mockPage;
	}

	public void setMockPage(String mockPage) {
		this.mockPage = mockPage;
	}

	public Boolean getTraceScreenshots() {
		return traceScreenshots;
	}

	public void setTraceScreenshots(Boolean traceScreenshots) {
		this.traceScreenshots = traceScreenshots;
	}

}

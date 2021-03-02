package com.appian.rpa.snippet.ibm3270.clients;

import java.awt.Point;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.appian.rpa.snippet.ibm3270.IBM3270Commons;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.exceptions.JidokaItemException;

import mmarquee.automation.AutomationException;
import mmarquee.automation.UIAutomation;
import mmarquee.automation.controls.ListItem;
import mmarquee.automation.controls.Window;

/**
 * IBM3270Commons extension for PCOMM emulator
 */
public class PCOMMEmulatorCommons extends IBM3270Commons {

	/**
	 * Default X-coordinate
	 */
	private static final int MAX_COORD_X = 80;

	/**
	 * Default Y-coordinate
	 */
	private static final int MAX_COORD_Y = 24;

	/**
	 * UI Automation instance
	 */
	private UIAutomation automation = UIAutomation.getInstance();

	private final String CURSOR_INFO_REGEXP = "The cursor is on row \\d{1,2}, column \\d{1,2}.";

	/**
	 * Default constructor
	 * 
	 * @param server
	 * @param client
	 * @param robot
	 */
	public PCOMMEmulatorCommons(IRobot robot, String windowsTitle3270) {

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
	 * Move the cursor to the designated coordinates
	 */
	@Override
	public void moveToCoordinates(int targetXCoodinate, int targetYCoodinate) {
		server.debug(String.format("We're moving to the coordinates (%d, %d)", targetXCoodinate, targetYCoodinate));
		activateWindow();
		Point cursorPos = getCursorCoordinates();
		
		if (cursorPos.x > targetXCoodinate) {
			server.debug("Moving " + (cursorPos.x - targetXCoodinate) + " left.");
			pressLeft(cursorPos.x - targetXCoodinate);
		} else {
			server.debug("Moving " + (targetXCoodinate - cursorPos.x) + " right.");
			pressRight(targetXCoodinate - cursorPos.x);
		}
		
		if (cursorPos.y > targetYCoodinate) {
			server.debug("Moving " + (cursorPos.y - targetYCoodinate) + " up.");
			pressUp(cursorPos.y - targetYCoodinate);
		} else {
			server.debug("Moving " + (targetYCoodinate - cursorPos.y) + " down.");
			pressDown(targetYCoodinate - cursorPos.y);
		}
		cursorPos = getCursorCoordinates();
		server.debug(String.format("Coordinates (%s, %s) reached.", cursorPos.x, cursorPos.y));
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

	/**
	 * Return an array containing the current cursor coordinates {x,y} in the emulator display. If not found return an Exception.
	 * 
	 * @return String[]
	 */
	public Point getCursorCoordinates() {
		
		try {
			List<ListItem> components = getOIALines();
			for (ListItem li : components) {

				server.debug(li.getName());
				int cursorX = 0, cursorY = 0;
				if (StringUtils.isNotBlank(li.getName()) && li.getName().matches(CURSOR_INFO_REGEXP)) {
					
					Pattern p = Pattern.compile("(?<=row )\\d+");
			        Matcher m = p.matcher(li.getName());
			        if (m.find()) {
			        
			        	cursorY = Integer.parseInt(m.group());
			        }
			        Pattern p2 = Pattern.compile("(?<=column )\\d+");
			        Matcher m2 = p2.matcher(li.getName());
			        if (m2.find()) {
			        
			        	cursorX = Integer.parseInt(m2.group());
			        }
			        return new Point(cursorX, cursorY);
				}
			}

		} catch (AutomationException e) {
			server.error(e.getMessage());
			e.printStackTrace();
		}
		
		throw new JidokaItemException("Error locating the cursor.");
	}
	
	private List<ListItem> getOIALines() throws AutomationException {
		
		server.debug("test 0");
		Window window = automation.getDesktopWindow(Pattern.compile(windowsTitle3270));
		
		server.debug("test 1");
		
		try {
			window.getList(0);
			
			server.debug("test 2");
				
		} catch (IndexOutOfBoundsException e) {
			server.sendScreen("OIA is not visible, restoring.");
			keyboard.alt("v").pause(500).type("o");
		}
		
		window = automation.getDesktopWindow(Pattern.compile(windowsTitle3270));
		
		server.debug("test 3");
		
		try {
			window.getList(0); 
				
		} catch (IndexOutOfBoundsException e) {
			throw new AutomationException("OIA not available");
		}
		
		List<ListItem> components = window.getList(0).getItems();
		
		server.debug("test 4");
		
		return components;
	}
}

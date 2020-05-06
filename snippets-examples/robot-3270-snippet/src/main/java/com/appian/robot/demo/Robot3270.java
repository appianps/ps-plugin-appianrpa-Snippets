package com.appian.robot.demo;

import java.io.IOException;
import java.io.Serializable;
import java.text.MessageFormat;

import com.appian.robot.demo.commons.Commons3270Extended;
import com.appian.robot.demo.pages.WelcomePage;
import com.appian.rpa.snippet.IBM3270AppManager;
import com.appian.rpa.snippet.IBM3270Commons;
import com.appian.rpa.snippet.TextInScreen;
import com.appian.rpa.snippet.page.RemotePage;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.annotations.Robot;
import com.novayre.jidoka.client.api.exceptions.JidokaException;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;
import com.novayre.jidoka.windows.api.IWindows;

@Robot
public class Robot3270 implements IRobot {

	/**
	 * Jidoka server instance
	 */
	private IJidokaServer<Serializable> server;

	/**
	 * Windows module instance
	 */
	private IWindows windows;
	
	/**
	 * IBM3270Commons snippet instance
	 */
	private IBM3270Commons ibm3270Commons;
	
	/**
	 * IBM3270AppManager instance
	 */
	private IBM3270AppManager appManager;

	/**
	 * Application Name
	 */
	public static final String APP_NAME = "3270APP";
	
	/**
	 * Application Name
	 */
	public static final String WINDOW_TITLE_REGEX = ".*3270";

	
	
	/**
	 * Startup method. This <code>startUp</code> method is called prior to calling
	 * the first workflow method defined
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean startUp() throws Exception {
		
		server = (IJidokaServer<Serializable>) JidokaFactory.getServer();

		windows = IWindows.getInstance(this);
		
		return true;
	}

	
	/**
	 * Action 'Init'.
	 * <p>
	 * Initializes Jidoka modules.
	 * 
	 * @throws JidokaException if the input file couldn't be read
	 * @throws IOException
	 */
	public void init() {

		try {
			
			ibm3270Commons = new Commons3270Extended(server, windows, this);
			appManager = new IBM3270AppManager(server, windows, this);
			
			server.debug("Robot initialized");
		} catch (Exception e) {
			throw new JidokaFatalException("Error initializing");
		}
	}
	
	
	/**
	 * Action 'Open 3270'.
	 */
	public void open3270 () {
		appManager.openIBM3270(APP_NAME, WINDOW_TITLE_REGEX);
	}
	
	
	/**
	 * Action 'Close 3270'.
	 */
	public void close3270 () {
		appManager.closeIBM3270();
	}
	
	/**
	 * Action 'Locate text'.
	 */
	public void locateText () {
		TextInScreen textInScreen = ibm3270Commons.locateText(2, "Welcome");
		if(textInScreen != null) {
			server.info(MessageFormat.format("Text '%s' found", "Welcome"));
		}
	}
	
	
	/**
	 * Action 'Change screen'.
	 * @throws JidokaException 
	 */
	public void changeScreen () throws JidokaException {
		server.sendScreen("Before moving to AOF page");
		
		ibm3270Commons.moveToCoodinates("==>", 3, 1);
		windows.pause(1000);
		ibm3270Commons.write("AOF");
		windows.pause(1000);
		ibm3270Commons.enter();
		checkScreen(new WelcomePage(server, windows, this));
		
		server.sendScreen("Moved to AOF page");
	}
	
	
	/**
	 * Action 'Change screen'.
	 * @param welcomePage 
	 * @throws JidokaException 
	 */
	private Boolean checkScreen (RemotePage remotePage) throws JidokaException {
		
		server.info("Verifying that we are on the right page");
		
		try {
			remotePage.assertIsThisPage();
			return true;
		} catch (Exception e) {
			server.info("Wrong page");
			return false;
		}
	}
	
	
	/**
	 * Action 'End'.
	 */
	public void end() {

		// continue the process, here the robot ends its execution

	}

	/**
	 * Clean up.
	 *
	 * @return the string[]
	 * @throws Exception the exception
	 * @see com.novayre.jidoka.client.api.IRobot#cleanUp()
	 */
	@Override
	public String[] cleanUp() throws Exception {
		appManager.closeIBM3270();
		return IRobot.super.cleanUp();
	}

	/**
	 * Manage exception.
	 *
	 * @param action    the action
	 * @param exception the exception
	 * @return the string
	 * @throws Exception the exception
	 */
	@Override
	public String manageException(String action, Exception exception) throws Exception {

		
		return IRobot.super.manageException(action, exception);
	}

}

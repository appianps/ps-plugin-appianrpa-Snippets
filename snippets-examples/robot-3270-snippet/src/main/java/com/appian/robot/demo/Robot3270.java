package com.appian.robot.demo;

import java.io.IOException;
import java.io.Serializable;

import com.appian.robot.demo.commons.IBM3270AppManager;
import com.appian.robot.demo.pages.ConstantsTexts;
import com.appian.robot.demo.pages.NetViewPage;
import com.appian.robot.demo.pages.RemotePage;
import com.appian.rpa.snippet.TextInScreen;
import com.appian.rpa.snippet.clients.FDZCommonsExtended;
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
	private FDZCommonsExtended ibm3270Commons;
	
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
	
	
	public RemotePage currentPage;

	
	
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
			
			ibm3270Commons = new FDZCommonsExtended(server, windows, this);
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
	 * Action 'Validate page'.
	 */
	public void validateMainPage () {
		TextInScreen textInScreen = ibm3270Commons.locateText(2, ConstantsTexts.WELCOME_UNIVOCAL_TEXT);
		if(textInScreen != null) {
			server.info(String.format("Text %s found", ConstantsTexts.WELCOME_UNIVOCAL_TEXT));
		}
	}
	
	
	/**
	 * Action 'Change screen'.
	 * @throws JidokaException 
	 */
	public void goToNetView () throws JidokaException {
		
		server.sendScreen("Screenshot before moving to NetView page");
		ibm3270Commons.write("NETVIEW");
		windows.pause(1000);
		ibm3270Commons.enter();
		currentPage = checkScreen(new NetViewPage(server, windows, this));
		
		server.sendScreen("Moved to NetView page");
	}
	
	/**
	 * Action 'Change password'.
	 * @throws JidokaException 
	 */
	public void changePassword() throws JidokaException {
		
		server.info("Change NetView Password");
				
		((NetViewPage) currentPage).changeOperatorPassword(ibm3270Commons);
	}
	
		
	/**
	 * Action 'Close 3270'.
	 */
	public void close3270 () {
		appManager.closeIBM3270();
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
	

	/**
	 * Method Change screen.
	 * @param welcomePage 
	 * @throws JidokaException 
	 */
	private RemotePage checkScreen (RemotePage remotePage) throws JidokaException {
		
		server.info("Verifying that we are on the right page");
		
		try {
			return remotePage.assertIsThisPage();
		} catch (Exception e) {
			server.info("Wrong page");
			return null;
		}
	}

}

package com.appian.robot.demo.pages;

import com.appian.rpa.snippet.TextInScreen;
import com.appian.rpa.snippet.clients.FDZCommonsExtended;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.exceptions.JidokaException;
import com.novayre.jidoka.windows.api.IWindows;


/**
 * Class to manage Welcome Page
 */
public class NetViewPage extends RemotePage {


	public NetViewPage(IJidokaServer< ? > server, IWindows windows, IRobot robot) throws JidokaException {
		super(server, windows, robot);
	}
	
	
	@Override 
	public String getUnivocalRegex() {
		
		return ConstantsTexts.NETVIEW_UNIVOCAL_TEXT;
	}
	
	
	
	/**
	 * Changes the password of an operator
	 */
	public void changeOperatorPassword(FDZCommonsExtended ibm3270Commons) {
		
		TextInScreen textInScreen = ibm3270Commons.locateText(2, "ID ==>");
		ibm3270Commons.moveToCoodinates(textInScreen, 6, 1);
		ibm3270Commons.write(ConstantsTexts.TEST_ID_OPERATOR);
		ibm3270Commons.enter();
		windows.pause(2000);
		
		// Change Password Page
		server.sendScreen("Change password page");
		ibm3270Commons.waitTillTextDisappears(ConstantsTexts.NETVIEW_UNIVOCAL_TEXT);
		
		ibm3270Commons.write(ConstantsTexts.TEST_PWD_OPERATOR);
		ibm3270Commons.pressDown(2);
		ibm3270Commons.pressLeft(3);
		
		ibm3270Commons.write(ConstantsTexts.TEST_NEW_PWD_OPERATOR);
		ibm3270Commons.pressDown(2);
		ibm3270Commons.pressLeft(3);
		
		ibm3270Commons.write(ConstantsTexts.TEST_NEW_PWD_OPERATOR);
		ibm3270Commons.enter();
		windows.pause(1000);

		ibm3270Commons.waitTillTextDisappears(2, ConstantsTexts.PWD_UNIVOCAL_TEXT);
		
		ibm3270Commons.pressDown(8);
		TextInScreen textInScreenPwd = ibm3270Commons.locateText(ConstantsTexts.INVALID_USER_UNIVOCAL_TEXT);
		
		if(textInScreenPwd != null) {
			server.sendScreen("Password could not be changed: invalid opertator");
		} else {
			server.sendScreen("Password changed");
		}
	}
	
}

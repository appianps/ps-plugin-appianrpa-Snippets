package com.appian.robot.demo.pages;

import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.exceptions.JidokaException;
import com.novayre.jidoka.windows.api.IWindows;


/**
 * Class to manage Welcome Page
 */
public class LoginPage extends RemotePage {


	public LoginPage(IJidokaServer< ? > server, IWindows windows, IRobot robot) throws JidokaException {
		super(server, windows, robot);
	}
	
	
	@Override 
	public String getUnivocalRegex() {
		
		return ConstantsTexts.LOGIN_UNIVOCAL_TEXT;
	}
	
	

}

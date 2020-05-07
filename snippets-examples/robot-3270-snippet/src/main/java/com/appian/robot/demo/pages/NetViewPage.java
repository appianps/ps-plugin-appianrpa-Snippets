package com.appian.robot.demo.pages;

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
	 * Login to stablish a session with the NetView program
	 */
	public void loginNV() {
		
	}
	
	/**
	 * Login to stablish a session with the NetView program
	 */
	public void changeNVPassword() {
		
	}
	
}

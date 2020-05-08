package com.appian.robot.demo.pages;

import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.multios.IClient;


/**
 * Class to manage Welcome Page
 */
public class WelcomePage extends RemotePage {


	public WelcomePage(IClient client, IRobot robot) {
		super(client, robot);
	}
	
	
	@Override 
	public String getUnivocalRegex() {
		
		return ConstantsTexts.WELCOME_UNIVOCAL_TEXT;
	}
	
	

}

package com.appian.robot.demo.pages;

import com.appian.rpa.snippet.page.RemotePage;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.exceptions.JidokaException;
import com.novayre.jidoka.windows.api.IWindows;


/**
 * Class to manage Welcome Page
 */
public class WelcomePage extends RemotePage {


	public WelcomePage(IJidokaServer< ? > server, IWindows windows, IRobot robot) throws JidokaException {
		super(server, windows, robot);
	}
	@Override 
	public String getUnivocalRegex() {
		
		return ConstantsTexts.WELCOME_UNIVOCAL_TEXT;
	}

}

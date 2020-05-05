package com.appian.robot.demo.nasdaq.automation;

import java.io.Serializable;
import java.util.regex.Pattern;

import com.appian.robot.demo.nasdaq.excel.EnterpriseModel;
import com.appian.robot.demo.nasdaq.robot.IApplicationManager;
import com.appian.robot.demo.nasdaq.robot.MainRobot;
import com.appian.rpa.snippets.commons.application.ApplicationManager;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IKeyboard;
import com.novayre.jidoka.client.api.IWaitFor;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.exceptions.JidokaItemException;
import com.novayre.jidoka.client.api.exceptions.JidokaUnsatisfiedConditionException;
import com.novayre.jidoka.client.api.multios.IClient;

import mmarquee.automation.AutomationException;
import mmarquee.automation.UIAutomation;
import mmarquee.automation.controls.Window;

public class UIAutomationManager implements IApplicationManager {

	private IClient client;
	private Window appWindow;
	private IKeyboard keyboard;
	private IWaitFor waitFor;
	private IJidokaServer<?> server;
	private UIAutomation automation;
	/** Application Manager instance */
	private ApplicationManager applicationManager;

	public UIAutomationManager(MainRobot robot) {

		this.server = JidokaFactory.getServer();
		this.client = IClient.getInstance(robot);
		this.waitFor = client.waitFor(robot);
		this.automation = UIAutomation.getInstance();
		this.keyboard = client.keyboard();
		
		// ApplicationManager init
		applicationManager = new ApplicationManager(robot, StockApplicationConstants.General.APP_NAME,
				StockApplicationConstants.General.APP_FOLDER, StockApplicationConstants.Titles.APP_MAIN_WINDOW_TITLE);

	}

	@SuppressWarnings({ "unchecked", "unused" })
	private void fillFieldsOld(EnterpriseModel currentItem) {

		try {

			// We set the focus on the main tab
			appWindow.getTab(0).getTabItems().get(0).getElement().setFocus();

			// Set 'Name' fields
			appWindow.getEditBoxByAutomationId(StockApplicationConstants.Fields.SYMBOL)
					.setValue(currentItem.getSymbols());
			appWindow.getEditBoxByAutomationId(StockApplicationConstants.Fields.COMPANY_NAME)
					.setValue(currentItem.getCompanyName());

			// Set 'Data' fields
			appWindow.getComboBoxByAutomationId(StockApplicationConstants.Fields.INC_DEC)
					.setValue(currentItem.getArrow());
			appWindow.getEditBoxByAutomationId(StockApplicationConstants.Fields.LAST_PRICE)
					.setValue(currentItem.getLastPrice());
			appWindow.getEditBoxByAutomationId(StockApplicationConstants.Fields.PRICING_CHANGE)
					.setValue(currentItem.getPriceChanging());
			appWindow.getEditBoxByAutomationId(StockApplicationConstants.Fields.PERCENT)
					.setValue(currentItem.getChangePercent());

			// Send a screenshot to the console
			server.sendScreen("Fields of 'Enterprise' filled");
		} catch (AutomationException e) {
			throw new JidokaItemException((IJidokaServer<Serializable>) JidokaFactory.getServer(),
					"Error filling the fields.", e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void fillFields(EnterpriseModel currentItem) {

		try {

			// Set 'Name' fields
			appWindow.getEditBoxByAutomationId(StockApplicationConstants.Fields.SYMBOL)
					.setValue(currentItem.getSymbols());
			keyboard.tab().type(currentItem.getCompanyName());

			// Set 'Data' fields
			keyboard.tab().type(currentItem.getArrow());
			keyboard.tab().type(currentItem.getLastPrice());
			keyboard.tab().type(currentItem.getPriceChanging());
			keyboard.tab().type(currentItem.getChangePercent());
			
			// This is for wait until the data is writen
			client.pause(1500);

			// Send a screenshot to the console
			server.sendScreen("Fields of 'Enterprise' filled");
		} catch (AutomationException e) {
			throw new JidokaItemException((IJidokaServer<Serializable>) JidokaFactory.getServer(),
					"Error filling the fields.", e);
		}
	}

	@Override
	public void clickSaveButton() {

		try {
			// Click on 'Save' button
			keyboard.alt("s");

			// Wait until 'Save' dialog has been loaded
			waitFor.wait(10, "Opening 'Save' dialog", true, false, () -> {
				try {
					return automation.getDesktopWindow(
							Pattern.compile(StockApplicationConstants.Titles.APP_INFO_WINDOW_TITLE)) != null;
				} catch (AutomationException e) {
					return false;
				}
			});

			// Send a screenshot to the console
			server.sendScreen("'Save' button clicked");

			// Click on 'Accept' button
			keyboard.enter();

		} catch (JidokaUnsatisfiedConditionException e) {
			throw new JidokaItemException("Error clicking the buttons");
		}

	}

	@Override
	public void newCompany() {

		try {

			applicationManager.activateWindow();
			
			// Get the app window
			appWindow = automation
					.getDesktopWindow(Pattern.compile(StockApplicationConstants.Titles.APP_MAIN_WINDOW_TITLE));

			// Open the "Actions" menu
			keyboard.alt("a");

			// Wait until menu has been opened
			client.pause(2000);

			// New Company
			keyboard.type("n");

			// Send a screenshot to the console
			server.sendScreen("Fields cleared");
		} catch (Exception e) {
			throw new JidokaItemException("Error cleaning applicaction fields.");
		}
	}

}

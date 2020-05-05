package com.appian.robot.demo.nasdaq.falcon;

import java.awt.AWTException;
import java.awt.Point;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.appian.robot.demo.nasdaq.automation.StockApplicationConstants;
import com.appian.robot.demo.nasdaq.excel.EnterpriseModel;
import com.appian.robot.demo.nasdaq.robot.IApplicationManager;
import com.appian.robot.demo.nasdaq.robot.MainRobot;
import com.appian.rpa.snippets.commons.application.ApplicationManager;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;
import com.novayre.jidoka.client.api.exceptions.JidokaItemException;
import com.novayre.jidoka.client.api.exceptions.JidokaUnsatisfiedConditionException;
import com.novayre.jidoka.client.api.multios.IClient;
import com.novayre.jidoka.falcon.api.FalconImageOptions;
import com.novayre.jidoka.falcon.api.IFalcon;
import com.novayre.jidoka.falcon.api.IFalconImage;
import com.novayre.jidoka.client.api.IWaitFor;

public class FalconManager implements IApplicationManager {

	/**
	 * Pause between actions like persons do
	 */
	private static final int PAUSE = 500;

	/**
	 * Falcon module
	 */
	private IFalcon falcon;

	/**
	 * Server
	 */
	private IJidokaServer<?> server;

	/**
	 * Client module
	 */
	private IClient client;

	/** Application Manager instance */
	private ApplicationManager applicationManager;
	
	/**
	 * IWaitFor module
	 */
	private IWaitFor waitFor;
	
	/**
	 * Environment variable images folder
	 */
	private static final String EV_IMAGES_FOLDER = "imagesFolder";
	
	/**
	 * Images folder to use
	 */
	private String imagesFolder;

	/**
	 * Actions menu image.
	 */
	private IFalconImage actionsMenuImage;

	/**
	 * New enterprise option image.
	 */
	private IFalconImage newEnterpriseImage;

	/**
	 * Symbol field image.
	 */
	private IFalconImage symbolFieldImage;

	/**
	 * Company name field image.
	 */
	private IFalconImage companyNameFieldImage;

	/**
	 * Inc/Dec field image.
	 */
	private IFalconImage incDecFieldImage;

	/**
	 * Last price field image.
	 */
	private IFalconImage lastPriceFieldImage;

	/**
	 * Pricing change field image.
	 */
	private IFalconImage pricingChangeFieldImage;

	/**
	 * Percent field image.
	 */
	private IFalconImage percentFieldImage;

	/**
	 * Save button image.
	 */
	private IFalconImage saveButtonImage;

	/**
	 * Saved text image.
	 */
	private IFalconImage savedTextImage;

	/**
	 * Accept button image.
	 */
	private IFalconImage acceptButtonImage;

	/**
	 * Action "Init".
	 * 
	 * @throws IOException if an I/O error occurs
	 */
	public FalconManager(MainRobot robot) throws IOException {

		// Get an instance of IJidokaServer to communicate with the server.
		server = (IJidokaServer<?>) JidokaFactory.getServer();

		/*
		 * Get an instance of IClient.
		 */
		client = IClient.getInstance(robot);

		// Get an instance of the Hawk-Eye module
		falcon = IFalcon.getInstance(robot, client);

		// ApplicationManager init
		applicationManager = new ApplicationManager(robot, StockApplicationConstants.General.APP_NAME,
				StockApplicationConstants.General.APP_FOLDER, StockApplicationConstants.Titles.APP_MAIN_WINDOW_TITLE);

		// Init waitFor
		waitFor = client.waitFor(robot);
		
		// Initialize the images folder
		imagesFolder = server.getEnvironmentVariables().get(EV_IMAGES_FOLDER);

		/*
		 * Set the standard pause after typing or using the mouse. This is not
		 * mandatory, but it is very usual.
		 */
		client.typingPause(PAUSE);

		// Initialize the IFalconImages objects to work with the images
		actionsMenuImage = initImage(0.0f, 0.05f, "actions-menu.png", "Actions menu");
		newEnterpriseImage = initImage(0.0f, 0.05f, "newEnterprise.png", "New enterprise");
		symbolFieldImage = initImage(0.0f, 0.05f, "symbol-field.png", "Symbol field");
		companyNameFieldImage = initImage(0.0f, 0.05f, "companyName-field.png", "Symbol field");
		incDecFieldImage = initImage(0.0f, 0.05f, "incDec-field.png", "Inc/Dec field");
		lastPriceFieldImage = initImage(0.0f, 0.05f, "lastPrice-field.png", "Last price field");
		pricingChangeFieldImage = initImage(0.0f, 0.05f, "pricingChange-field.png", "Pricing change field");
		percentFieldImage = initImage(0.0f, 0.05f, "percent-field.png", "Percent field");
		saveButtonImage = initImage(0.0f, 0.05f, "save-button.png", "Save button");
		savedTextImage = initImage(0.0f, 0.05f, "saved-text.png", "Saved text");
		acceptButtonImage = initImage(0.0f, 0.05f, "accept-button.png", "Accept button");
	}

	/**
	 * Initialize the IFalconImages objects to work with the images using the
	 * specified parameters.
	 * 
	 * @param noiseTol noise tolerance
	 * @param colorTol colore tolerance
	 * @param fileName file name in the dashboard
	 * @return the {@link IFalconImage} to use
	 * @throws IOException if an I/O error occurs
	 */
	private IFalconImage initImage(float noiseTol, float colorTol, String fileName, String description)
			throws IOException {

		// Build the root path of the images to search
		Path path = Paths.get(server.getCurrentDir(), "images\\" + imagesFolder);

		FalconImageOptions fio = new FalconImageOptions();
		fio.setNoiseTolerance(noiseTol);
		fio.setColorTolerance(colorTol);
		fio.setFile(path.resolve(fileName).toFile());
		fio.setDescription(description);
		return falcon.getImage(fio);
	}

	@Override
	public void newCompany() {

		try {

			// Activate app window
			applicationManager.activateWindow();

			// Wait until the app is showed.
			waitFor.image(10, true, actionsMenuImage);

			// Open the "File" menu
			actionsMenuImage.clickOnCenter();

			// Wait until the menu is loaded.
			waitFor.image(10, true, newEnterpriseImage);

			// Click on "New Enterprise"
			newEnterpriseImage.clickOnCenter();

			// Send a screenshot to the console
			server.sendScreen("Fields cleared");
		} catch (Exception e) {
			throw new JidokaItemException("Error cleaning applicaction fields.");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void fillFields(EnterpriseModel currentItem) {

		try {

			// Set 'Name' fields
			setFieldValue(symbolFieldImage, currentItem.getSymbols());
			setFieldValue(companyNameFieldImage, currentItem.getCompanyName());

			// Set 'Data' fields
			setFieldValue(incDecFieldImage, currentItem.getArrow());
			setFieldValue(lastPriceFieldImage, currentItem.getLastPrice());
			setFieldValue(pricingChangeFieldImage, currentItem.getPriceChanging());
			setFieldValue(percentFieldImage, currentItem.getChangePercent());

			// Send a screenshot to the console
			server.sendScreen("Fields of 'Company' filled");
		} catch (AWTException e) {
			throw new JidokaItemException((IJidokaServer<Serializable>) JidokaFactory.getServer(),
					"Error filling the fields.", e);
		}
	}

	private void setFieldValue(IFalconImage image, String value) throws AWTException {

		// Search the falcon image
		IFalconImage i = image.search(false);

		if (i.found()) {
			// Add pixels to click on the input field
			double newX = i.getPointsWhereFound().get(0).getX() + 153.0;
			double newY = i.getPointsWhereFound().get(0).getY() + 7.0;

			client.mouseLeftClick(new Point((int) newX, (int) newY));
		} else {
			throw new JidokaFatalException("Field not found");
		}

		client.keyboard().type(value);
	}

	@Override
	public void clickSaveButton() {

		try {
			// Click on 'Save' button
			if (saveButtonImage.search(false).found()) {
				saveButtonImage.clickOnCenter();
			} else {
				throw new JidokaFatalException("Save button not found");
			}

			// Wait until the dialog is loaded.
			waitFor.image(10, true, savedTextImage);

			// Click on 'Accept' button
			if (acceptButtonImage.search(false).found()) {
				acceptButtonImage.clickOnCenter();
			} else {
				throw new JidokaFatalException("Accept button not found");
			}

		} catch (AWTException | IOException | JidokaUnsatisfiedConditionException e) {
			throw new JidokaItemException("Error clicking 'Save' button");
		}
	}
}

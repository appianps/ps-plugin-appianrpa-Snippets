package com.appian.rpa;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.appian.rpa.snippets.QRUtils;
import com.novayre.jidoka.browser.api.EBrowsers;
import com.novayre.jidoka.browser.api.IWebBrowserSupport;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.annotations.Robot;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;
import com.novayre.jidoka.client.api.multios.IClient;
import com.novayre.jidoka.falcon.api.IFalcon;

@Robot
public class RobotQRDecoder implements IRobot {

	/**
	 * URL to navigate to.
	 */

	private static final String QR_SITE_URL = "QRCodeWebsite";
	private IJidokaServer<?> server;
	private IClient client;
	private IWebBrowserSupport browser;
	private String browserType;
	private QRUtils qrutils;
	private IFalcon falcon;
	String webURL;

	@Override
	public boolean startUp() throws Exception {

		server = JidokaFactory.getServer();
		client = IClient.getInstance(this);
		browser = IWebBrowserSupport.getInstance(this, client);
		falcon = IFalcon.getInstance(this, client);
		qrutils = new QRUtils();
		webURL = server.getParameters().get(QR_SITE_URL);
		return IRobot.super.startUp();

	}

	/**
	 * Action "start".
	 */
	public void start() {
		server.setNumberOfItems(1);
	}

	/**
	 * Open Web Browser
	 *
	 * @throws Exception
	 */
	public void openBrowser() throws Exception {

		browserType = server.getParameters().get("Browser");

		// Select browser type
		if (StringUtils.isBlank(browserType)) {
			server.info("Browser parameter not present. Using the default browser CHROME");
			browser.setBrowserType(EBrowsers.CHROME);
			browserType = EBrowsers.CHROME.name();
		} else {
			EBrowsers selectedBrowser = EBrowsers.valueOf(browserType);
			browserType = selectedBrowser.name();
			browser.setBrowserType(selectedBrowser);
			server.info("Browser selected: " + selectedBrowser.name());
		}

		// Set timeout to 60 seconds
		browser.setTimeoutSeconds(60);

		// Init the browser module
		browser.initBrowser();

		// This command is uses to make visible in the desktop the page (IExplore issue)
		if (EBrowsers.INTERNET_EXPLORER.name().equals(browserType)) {
			client.clickOnCenter();
			client.pause(3000);
		}

	}

	/**
	 * Navigate to Web Page
	 *
	 * @throws Exception
	 */
	public void navigateToWeb() throws Exception {

		server.setCurrentItem(1, webURL);

		// Navegate to HOME_URL address
		browser.navigate(webURL);

		// we save the screenshot, it can be viewed in robot execution trace page on the
		// console
		server.sendScreen("Screen after load page: " + webURL);

		server.setCurrentItemResultToOK("Success");
	}

	public void scrapImage() {
		// locate image, xpath, css etc
		WebElement element = browser.getElement(By.className("qr"));
		// get images source
		String elementsrc = element.getAttribute("src");

		try {
			// generate url
			URL imageURL = new URL(elementsrc);
			// read url and retrieve image
			BufferedImage saveImage = ImageIO.read(imageURL);
			// show image as log
			falcon.sendImage(saveImage, "QR Code");
			// decode image
			String result = qrutils.readQRImage(saveImage);
			server.warn("The QR Image contains this information: " + result);
		} catch (JidokaFatalException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			throw new JidokaFatalException(e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new JidokaFatalException(e.getMessage());
		}
	}

	/**
	 * @see com.novayre.jidoka.client.api.IRobot#cleanUp()
	 */
	@Override
	public String[] cleanUp() throws Exception {

		browserCleanUp();
		return null;
	}

	/**
	 * Close the browser.
	 */
	private void browserCleanUp() {

		// If the browser was initialized, close it
		if (browser != null) {
			try {
				browser.close();
				browser = null;

			} catch (Exception e) { // NOPMD
				// Ignore exception
			}
		}

		try {

			if (browserType != null) {

				switch (EBrowsers.valueOf(browserType)) {

				case CHROME:
					client.killAllProcesses("chromedriver.exe", 1000);
					break;

				case INTERNET_EXPLORER:
					client.killAllProcesses("IEDriverServer.exe", 1000);
					break;

				case FIREFOX:
					client.killAllProcesses("geckodriver.exe", 1000);
					break;

				default:
					break;

				}
			}

		} catch (Exception e) { // NOPMD
			// Ignore exception
		}

	}

	/**
	 * Last action of the robot.
	 */
	public void end() {
		server.info("End process");
	}

}

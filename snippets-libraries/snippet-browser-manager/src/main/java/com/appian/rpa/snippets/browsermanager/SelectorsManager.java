package com.appian.rpa.snippets.browsermanager;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.novayre.jidoka.browser.api.IWebBrowserSupport;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;
import com.novayre.jidoka.client.api.multios.IClient;

/**
 * Most of the web elements from a website (checkboxes, text, buttons) are
 * referred by a selector (you can inspect them by right clicking the target
 * element on your browser). This selector identifies in an unambiguous way a
 * web element. Therefore, the robot can interact with a website robustly.
 * 
 * The SelectorsManager class retrieves all the selectors involved in the
 * robotic process from the selectors.properties file.
 */
public class SelectorsManager {

	/** Browser module instance */
	protected IWebBrowserSupport browser;

	/** Client module instance */
	private IClient client;

	/** Selectors map */
	private Map<String, String> selectorsMapper = new HashMap<>();

	/** Server */
	private IJidokaServer<?> server;

	/** XPath selector suffix */
	private static final String XPATH_SUFFIX = "xpath";

	/** CSS selector selector suffix */
	private static final String CSS_SUFFIX = "css";

	/** Classname selector suffix */
	private static final String CLASSNAME_SUFFIX = "classname";

	/** Id selector suffix */
	private static final String ID_SUFFIX = "id";
	

	/**
	 * SelectorsManager Constructor
	 * 
	 * @param robot    The IRobot instance
	 * @param filePath Selectors properties file path
	 */
	public SelectorsManager() {

		IRobot robot = IRobot.getDummyInstance();

		Path filePath = Paths.get(JidokaFactory.getServer().getCurrentDir(), "browser", "selectors.properties");

		InputStream input;
			
		server = JidokaFactory.getServer();
		
		try {
			
			if (filePath != null && filePath.toFile().exists()) {
				
				input = new FileInputStream(filePath.toFile());
				
				server.info("Selector file configured from SUPPORT FILES");
				
			} else {
				
				input = this.getClass().getClassLoader().getResourceAsStream("selectors.properties");
				
				server.info("Selector file configured from CLASSPATH");
			}

			if(input == null) {
				server.info("No selectors file configured in Support Files ");
				return;
			}

			Properties selectorsFile = new Properties();

			selectorsFile.load(input);

			for (String key : selectorsFile.stringPropertyNames()) {
				String value = selectorsFile.getProperty(key);
				selectorsMapper.put(key, String.valueOf(value));
			}
		} catch (IOException ex) {
			throw new JidokaFatalException("Error reading the selectors file", ex);
		}

		this.client = IClient.getInstance(robot);
		this.browser = IWebBrowserSupport.getInstance(robot, client);
	}

	/**
	 * Gets the selector given by the key {@code key}
	 * 
	 * @param key Key to search the selector
	 * @return The selector
	 */
	public String getSelector(String key) {
		return selectorsMapper.get(key);
	}

	/**
	 * Finds the {@link WebElement} object using the selector saved in the selectors
	 * file, which is found filtering by the given {@code key}. The selector key
	 * must end in: {@link #XPATH_SUFFIX}, {@link #CSS_SUFFIX},
	 * {@link #CLASSNAME_SUFFIX} or {@link #ID_SUFFIX}. If the key does not end in
	 * one of these suffixes, it is returned null.
	 * 
	 * @param key Selector key from selectors.properties file (String)
	 * @return The {@link WebElement} object resulting from the selector search.
	 */
	public WebElement getElement(String key) {

		By by = getBy(key);

		return getElement(by);
	}
	
	/**
	 * Finds the {@link WebElement} object using {@link By}, if not found return null.
	 * @param by
	 * @return
	 */
	public WebElement getElement(By by) {

		if (by != null) {

			return browser.getElement(by);
		}

		return null;
	}

	/**
	 * This function retrieves the corresponding By element given a selector.
	 * 
	 * @param key Selector key from selectors.properties file (String)
	 * @return The {@link By} element depending on termination
	 */
	public By getBy(String key) {
		if (key.endsWith(XPATH_SUFFIX)) {
			return By.xpath(getSelector(key));
		} else if (key.endsWith(CSS_SUFFIX)) {
			return By.cssSelector(getSelector(key));
		} else if (key.endsWith(CLASSNAME_SUFFIX)) {
			return By.className(getSelector(key));
		} else if (key.endsWith(ID_SUFFIX)) {
			return By.id(getSelector(key));
		} else {
			return null;
		}
	}

	/**
	 * Find all {@link WebElement} objects using the selector saved in the selectors
	 * file, which can be found searching by the given {@code key}. The selector key
	 * must ends with {@link #XPATH_SUFFIX}, {@link #CSS_SUFFIX},
	 * {@link #CLASSNAME_SUFFIX} or {@link #ID_SUFFIX}. If the key does not end with
	 * one of these suffixes, the function will return an empty list.
	 * 
	 * @param key Selector key on the selectors file
	 * @return The @{@link List} of {@link WebElement} objects resulting from the
	 *         selector search.
	 */
	public List<WebElement> getAllElements(String key) {

		By by = getBy(key);

		return getAllElements(by);
	}
	
	/**
	 * Find all {@link WebElement} objects using {@link By}, if none element found return empty list.
	 * @param by
	 * @return
	 */
	public List<WebElement> getAllElements(By by) {

		if (by != null) {

			return browser.getElements(by);
		}

		return null;
	}
	
	/**
	 * Check if the {@link WebElement} exists in the DOM using the saved selector in
	 * selectors file, which can found filtering by the given {@code key}. Searches
	 * by the given {@code key}. The selector key must ends with
	 * {@link #XPATH_SUFFIX}, {@link #CSS_SUFFIX}, {@link #CLASSNAME_SUFFIX} or
	 * {@link #ID_SUFFIX}. If the key does not end with one of these suffixes, the
	 * function will return an empty list.
	 * 
	 * @param key Selector key on the selectors file
	 * @return true if the {@link WebElement} was found; false otherwise
	 */
	public Boolean existsElement(String key) {

		By by = getBy(key);

		return existsElement(by);
	}
	
	/**
	 * Check if the {@link WebElement} exists in the DOM using the given {@link By}
	 * @param by
	 * @return
	 */
	public Boolean existsElement(By by) {

		if (by != null) {

			return (browser.getElement(by) != null);
		}

		return false;
	}
}
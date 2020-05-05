package com.appian.robot.demo.nasdaq;

import org.openqa.selenium.WebElement;

import com.appian.robot.demo.nasdaq.excel.EnterpriseModel;
import com.appian.rpa.snippets.commons.browser.BrowserManager;
import com.appian.rpa.snippets.commons.browser.SelectorsManager;
import com.novayre.jidoka.client.api.exceptions.JidokaItemException;

/**
 * Class to manage Nasdaq Web
 * 
 */
public class NasdaqWebManager {
	
	/** BrowserManager instance */
	private BrowserManager browserManager;
	
	/** SelectorsManager isntance */
	private SelectorsManager selectorsManager;

	/** 
	 * NasdaqWebManager constructor
	 * 
	 * @param robot IRobot instance
	 */
	public NasdaqWebManager(BrowserManager browserManager) {
		
		this.browserManager = browserManager;
		this.selectorsManager = browserManager.getSelectorsManager();
	}

	/**
	 * Read data from the enterprise
	 * 
	 * @return
	 * @throws Exception
	 */
	public void getData(EnterpriseModel currentItem) {

		if (selectorsManager.getElement(NasdaqWebConstants.Selectors.EXISTING_COMPANY)!=null) {
			try {
				currentItem.setCompanyName(getCompanyName());
				currentItem.setArrow(getIncreaseDecrease());
				currentItem.setLastPrice(getLastPrice());
				if (currentItem.getArrow().equals(NasdaqWebConstants.Data.DECREASE_NAME)) {
					currentItem.setPriceChanging("-" + getPrincingChange());
					currentItem.setChangePercent("-" + getChangePercent());
				} else {
					currentItem.setPriceChanging(getPrincingChange());
					currentItem.setChangePercent(getChangePercent());
				}
			} catch (Exception e) {
				throw new JidokaItemException("Error getting the data.");
			}
		} else {
			throw new JidokaItemException(String.format("The company %s doesn't exist", currentItem.getSymbols()));
		}
	}

	/**
	 * Get company name from browser
	 * 
	 * @return
	 */
	private String getCompanyName() {

		WebElement companyNameWeb = selectorsManager.getElement(NasdaqWebConstants.Selectors.COMPANY_NAME);

		if (companyNameWeb != null) {
			return companyNameWeb.getText().replace("Common Stock", "");
		}

		return null;
	}

	/**
	 * Get status for Enterprise (Increase or Decrease)
	 * 
	 * @return
	 */
	private String getIncreaseDecrease() {

		if (selectorsManager.getElement(NasdaqWebConstants.Selectors.INCREASE_ARROW) != null) {
			return NasdaqWebConstants.Data.INCREASE_NAME;
		}

		if (selectorsManager.getElement(NasdaqWebConstants.Selectors.DECREASE_ARROW) != null) {
			return NasdaqWebConstants.Data.DECREASE_NAME;
		}

		return NasdaqWebConstants.Data.ARROW_VALUE_NULL;
	}

	/**
	 * Get last price
	 * 
	 * @return
	 */
	private String getLastPrice() {

		WebElement lastPriceElement = selectorsManager.getElement(NasdaqWebConstants.Selectors.LAST_PRICE);

		if (lastPriceElement != null) {
			return lastPriceElement.getText();
		}

		return null;
	}

	/**
	 * Get Princing Change
	 * 
	 * @return
	 */
	private String getPrincingChange() {

		WebElement princingChangeElement =selectorsManager.getElement(NasdaqWebConstants.Selectors.PRINCING_CHANGE);

		if (princingChangeElement != null) {
			return princingChangeElement.getText();
		}
		
		return null;
	}

	/**
	 * Get change percent
	 * 
	 * @return
	 */
	private String getChangePercent() {

		WebElement changePercentElement = selectorsManager.getElement(NasdaqWebConstants.Selectors.CHANGE_PERCENT);

		if (changePercentElement != null) {
			
			if(changePercentElement.getText().equals("")) {
				return NasdaqWebConstants.Data.UNCHANGE_STRING;
			}
			
			return changePercentElement.getText().replaceAll("[()]", "");
		}
		
		return null;
	}

	public void findCompany(String symbol) {

		browserManager.navigateTo(NasdaqWebConstants.General.WEB_URL + symbol);
	}
}

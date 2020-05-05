package com.appian.robot.demo.nasdaq;

/**
 * Class contains constants
 * 
 */
public final class NasdaqWebConstants {

	private NasdaqWebConstants() {
		//not called
	}
	
	/**
	 * General
	 */
	public static class General {
		
		private General() {
			//not called
		}
		
		public static final String WEB_URL = "https://new.nasdaq.com/market-activity/stocks/";
		
	}
	
	/**
	 * Class contains classnames from elements in the browser
	 */
	public static class Selectors {
		
		private Selectors() {
			//not called
		}

		// Identify element in the browser when page is load sucessfull
		public static final String COMPANY_NAME = "companyPage.companyName.classname";
		// Identify element to find the Arrow increase
		public static final String INCREASE_ARROW = "companyPage.increase.xpath";
		// Identify element to find the Arrow decrease
		public static final String DECREASE_ARROW = "companyPage.decrease.xpath";
		// Identify element to find the last price
		public static final String LAST_PRICE = "companyPage.lastPrice.classname";
		// Identify element to find the princing change
		public static final String PRINCING_CHANGE = "companyPage.pricingChange.classname";
		// Identify element to find the change percent
		public static final String CHANGE_PERCENT = "companyPage.changePercent.classname";
		// Identify element to define if the company exists
		public static final String EXISTING_COMPANY= "companyPage.existingCompany.classname";

	}

	/**
	 * Class contains values of atributes
	 */
	public static class Data {
		
		private Data() {
			//not called
		}

		// Value of increase
		public static final String INCREASE_NAME = "INCREASE";
		// Value of decrease
		public static final String DECREASE_NAME = "DECREASE";
		//Value of Price Change = UNCH and princing percent = UNCH
		public static final String UNCHANGE_STRING = "0.00%";
		//Value of INCREASE/DECREASE when is null
		public static final String ARROW_VALUE_NULL = "UNCHANGE";

	}

}

package com.appian.robot.demo.nasdaq.automation;

/**
 * Stick Manager Constants.
 * <p>
 * Constants used by the robot
 */
public class StockApplicationConstants {
	
	private StockApplicationConstants() {
		//not called
	}

	/**
	 * General
	 */
	public static class General {

		private General() {
			//not called
		}
		
		public static final String APP_NAME = "stockManager.exe";
		public static final String APP_FOLDER = "desktopApp";
	}
	
	/**
	 * Titles
	 */
	public static class Titles {

		private Titles() {
			//not called
		}
		
		public static final String APP_MAIN_WINDOW_TITLE = ".*Stock manager.*";
		public static final String APP_INFO_WINDOW_TITLE = "Info";
		public static final String ACTIONS_MENU = ".Actions.*";
	}

	/**
	 * Field control
	 */
	public static class Fields {

		private Fields() {
			//not called
		}
		
		// Tab name
		public static final String COMPANY_TAB_NAME = "Company";
		
		// Name
		public static final String SYMBOL = "14";
		public static final String COMPANY_NAME = "16";
		
		// Data
		public static final String INC_DEC = "19";
		public static final String LAST_PRICE = "21";
		public static final String PRICING_CHANGE = "23";
		public static final String PERCENT = "25";
	}

}

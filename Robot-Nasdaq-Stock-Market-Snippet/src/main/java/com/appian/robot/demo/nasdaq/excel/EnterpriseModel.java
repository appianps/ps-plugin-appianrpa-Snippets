package com.appian.robot.demo.nasdaq.excel;

import java.io.Serializable;

import com.appian.rpa.snippets.commons.excel.annotations.AExcelField;
import com.appian.rpa.snippets.commons.excel.annotations.AExcelFieldKey;

/**
 * Class represent one row in Excel file
 *
 */
public class EnterpriseModel implements Serializable {

	/** Serial */
	private static final long serialVersionUID = 1L;

	/** Symbols to search */
	@AExcelField(fieldName = "SYMBOL")
	@AExcelFieldKey
	private String symbols;
	
	/** Name from enterprise */
	@AExcelField(fieldName = "COMPANY NAME")
	private String companyName;

	/** Arrow red or green */
	@AExcelField(fieldName = "INCREASE / DECREASE")
	private String arrow;

	/** Last price from enterprise */
	@AExcelField(fieldName = "LAST PRICE")
	private String lastPrice;

	/** Number of price from Enterprise */
	@AExcelField(fieldName = "PRICING CHANGE")
	private String priceChanging;

	/** Change percent */
	@AExcelField(fieldName = "CHANGE PERCENT")
	private String changePercent;

	/* GETTERS AND SETTERS */

	public String getSymbols() {
		return symbols;
	}

	public void setSymbols(String symbols) {
		this.symbols = symbols;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getArrow() {
		return arrow;
	}

	public void setArrow(String arrow) {
		this.arrow = arrow;
	}

	public String getLastPrice() {
		return lastPrice;
	}

	public void setLastPrice(String lastPrice) {
		this.lastPrice = lastPrice;
	}

	public String getPriceChanging() {
		return priceChanging;
	}

	public void setPriceChanging(String priceChanging) {
		this.priceChanging = priceChanging;
	}

	public String getChangePercent() {
		return changePercent;
	}

	public void setChangePercent(String changePercent) {
		this.changePercent = changePercent;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
}

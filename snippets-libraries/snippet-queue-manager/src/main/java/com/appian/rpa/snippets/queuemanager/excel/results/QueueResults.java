package com.appian.rpa.snippets.queuemanager.excel.results;

import java.io.File;
import java.util.List;

/**
 * Queue results class
 * 
 * @param <T> Class T type
 */
public class QueueResults<T> {

	/** Items results */
	private List<T> itemsResults;

	/** Excel file */
	private File excelFile;

	/**
	 * Gets the Excel file
	 * 
	 * @return the excelFile
	 */
	public File getExcelFile() {
		return excelFile;
	}

	/**
	 * Sets the Excel file
	 * 
	 * @param excelFile the {@code excelFile} to set
	 */
	public void setExcelFile(File excelFile) {
		this.excelFile = excelFile;
	}

	/**
	 * Gets the items results
	 * 
	 * @return the itemsResults
	 */
	public List<T> getItemsResults() {
		return itemsResults;
	}

	/**
	 * Sets the items results
	 * 
	 * @param itemsResults the {@code itemsResults} to set
	 */
	public void setItemsResults(List<T> itemsResults) {
		this.itemsResults = itemsResults;
	}

}

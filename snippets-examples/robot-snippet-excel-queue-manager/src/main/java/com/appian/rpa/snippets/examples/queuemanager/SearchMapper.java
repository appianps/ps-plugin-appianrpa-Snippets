package com.appian.rpa.snippets.examples.queuemanager;

import com.appian.rpa.snippets.queuemanager.excel.mapper.AbstractItemFieldsMapper;

/**
 * 
 * The SearchMapper class provides utilities to customize the mapping rules from
 * the Excel target file.
 *
 */

public class SearchMapper extends AbstractItemFieldsMapper<SearchModel> {

	/**
	 * getSheetname returns a String containing the Sheet name where the data rows
	 * to be uploaded are located.
	 */

	@Override
	public String getSheetName() {
		return "TERMS";
	}

	/**
	 * getFirstRow returns the first row index as an int from the data rows sheet.
	 */

	@Override
	public int getFirstRow() {
		return 0;
	}

	/**
	 * returns the class indexing each excel row.
	 */

	@Override
	public Class<SearchModel> getTClass() {
		return SearchModel.class;
	}

}

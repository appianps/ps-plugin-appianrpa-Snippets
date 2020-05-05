package com.appian.robot.demo.nasdaq.excel;

import com.appian.rpa.snippets.commons.excel.mapper.AbstractItemFieldsMapper;

/**
 * Class that enables translation between an Excel row and the object
 * {@link EnterpriseModel} an object and Excel row
 * 
 */
public class EnterpriseMappedSearch extends AbstractItemFieldsMapper<EnterpriseModel> {

	@Override
	public String getSheetName() {
		return "SYMBOLS";
	}
	
	@Override
	public int getFirstRow() {
		return 0;
	}

	@Override
	public Class<EnterpriseModel> getTClass() {
		return EnterpriseModel.class;
	}

}

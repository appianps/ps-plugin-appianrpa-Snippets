package com.appian.rpa.snippets.examples.queuemanager;

import com.appian.rpa.snippets.queuemanager.excel.mapper.AbstractItemFieldsMapper;

public class SearchMapper extends AbstractItemFieldsMapper<SearchModel>{

	@Override
	public String getSheetName() {
		return "TERMS";
	}
	
	@Override
	public int getFirstRow() {
		return 0;
	}
	
	@Override
	public Class<SearchModel> getTClass() {
		return SearchModel.class;
	}

}

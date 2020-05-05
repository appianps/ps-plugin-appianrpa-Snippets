package com.appian.robot.demo.nasdaq.robot;

import com.appian.robot.demo.nasdaq.excel.EnterpriseModel;

public interface IApplicationManager {

	public void newCompany();
	
	public void fillFields(EnterpriseModel currentItem);
	
	public void clickSaveButton();
}

package com.appian.snippets.examples.params;

import com.appian.rpa.snippets.commons.instructions.EnvironmentVariable;

/**
 * 
 * Enum in which all the environment variables of a robot are defined
 *
 */
public enum EEnvironmentVariables {
	
	ITEMS_PER_ROBOT ("items_per_robot");
	
	private EnvironmentVariable environmentVariable;
	
	EEnvironmentVariables(String environmentVariableName){
		environmentVariable = new EnvironmentVariable(environmentVariableName);
	}
	
	public EnvironmentVariable getEnvironmentVariable(){
		
		return environmentVariable;
	}
}
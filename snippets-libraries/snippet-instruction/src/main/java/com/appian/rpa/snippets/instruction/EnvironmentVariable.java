package com.appian.rpa.snippets.instruction;

import java.io.File;
import java.nio.file.Paths;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;

/**
 * Class for the management of the robot instructions
 */
public class EnvironmentVariable {
	
	/**
	 * The JidokaServer instance.
	 */
	private IJidokaServer<?> server;
	
	private String name;
	
	private String parameter;
	
	/**
	 *	Environment variable constructor
	 *
	 *@param name Environment variable name
	 */
	public EnvironmentVariable(String name) {
		
		this.server = (IJidokaServer<?>) JidokaFactory.getServer();
		this.name = name;
		
		this.parameter = server.getEnvironmentVariables().get(name);
		
	}
	
	/**
	 * Returns the parameter value as a Boolean
	 * 
	 * @return The parameter value as Boolean
	 */
	public Boolean getAsBoolean() {
		
		return Boolean.valueOf(parameter);
	}
	
	/**
	 * Returns the parameter value as a Integer
	 * 
	 * @return The parameter value as a Integer
	 */
	public Integer getAsInteger() {
		
		if(StringUtils.isBlank(parameter)) {
			
			return null;
		}
		
		if(!NumberUtils.isNumber(parameter)) {
			throw new JidokaFatalException(String.format("The param %s must be a number. The received value is %s", name, parameter));
		} 

		return Integer.parseInt(parameter);		
	}	
	
	/**
	 * Returns the parameter value as a Long
	 * 
	 * @return The parameter value as a Long
	 */
	public Long getAsLong() {
		
		if(StringUtils.isBlank(parameter)) {
			
			return null;
		}
		
		if(!NumberUtils.isNumber(parameter)) {
			throw new JidokaFatalException(String.format("The param %s must be a number. The received value is %s", name, parameter));
		} 

		return Long.parseLong(parameter);	
	}
	
	/**
	 * Returns the parameter value as a String
	 * 
	 * @return The parameter value as a String
	 */
	public String getAsString() {
		
		return parameter;
	}
	
	/**
	 * Returns the parameter value as a File
	 * 
	 * @return The parameter value as a File
	 */
	public File getAsFile() {
		
		return Paths.get(server.getCurrentDir(), parameter).toFile();
	}
	
}

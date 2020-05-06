package com.appian.rpa.snippets.commons.instructions;

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
public class Instruction {
	
	/**
	 * The JidokaServer instance.
	 */
	private IJidokaServer<?> server;
	
	private String name;
	
	private Boolean required;

	private String parameter;
	
	/**
	 * 
	 */
	public Instruction(String name) {
		
		this(name, false);
	}
	
	/**
	 *	
	 */
	public Instruction(String name, Boolean required) {
		
		this.server = (IJidokaServer<?>) JidokaFactory.getServer();
		this.name = name;
		this.required = required;
		
		this.parameter = server.getParameters().get(name);
		
	}
	
	/**
	 * Returns the parameter value as a Boolean
	 * 
	 * @return
	 */
	public Boolean getAsBoolean() {
		
		checkRequired();
		
		return Boolean.valueOf(parameter);
	}
	
	/**
	 * Returns the parameter value as a Integer
	 * 
	 * @return
	 */
	public Integer getAsInteger() {
		
		checkRequired();
		
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
	 * @return
	 */
	public Long getAsLong() {
		
		checkRequired();
		
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
	 * @return
	 */
	public String getAsString() {
		
		checkRequired();
		
		return parameter;
	}
	
	/**
	 * Returns the parameter value as a File
	 * 
	 * @return
	 */
	public File getAsFile() {
		
		checkRequired();
		
		return Paths.get(server.getCurrentDir(), parameter).toFile();
	}
	
	
	private void checkRequired() {
		
		if (StringUtils.isBlank(parameter) && required) {
			throw new JidokaFatalException(String.format("The param %s is mandatory", name));
		}
	}
	
}

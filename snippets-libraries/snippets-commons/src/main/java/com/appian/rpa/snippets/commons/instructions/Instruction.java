package com.appian.rpa.snippets.commons.instructions;

import java.io.File;
import java.nio.file.Paths;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;

/**
<<<<<<< HEAD
 * Class for the management of the robot instructions.
 * 
 * Each object of this class represents a different instruction
 */
public class Instruction {

=======
 * Class for the management of the robot instructions
 */
public class Instruction {
	
>>>>>>> PS-460-queueWithoutExcel
	/**
	 * The JidokaServer instance.
	 */
	private IJidokaServer<?> server;
<<<<<<< HEAD

	/**
	 * Name of the instruction
	 */
	private String name;

	/**
	 * <code>true</code> if the instruction is required for the robot (mandatory)
	 */
	private Boolean required;

	/**
	 * This stores the value of the instruction in the console
	 */
	private String parameter;

	/**
	 * Constructor for an instruction not required
	 * 
	 * @param name of the instruction
	 */
	public Instruction(String name) {

		this(name, false);
	}

	/**
	 * Constructor for an instruction
	 * 
	 * @param name     of the instruction
	 * @param required <code>true</code> if the instruction is required (mandatory)
	 *                 for the robot
	 */
	public Instruction(String name, Boolean required) {

		this.server = JidokaFactory.getServer();
		this.name = name;
		this.required = required;

		this.parameter = server.getParameters().get(name);
	}

=======
	
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
	
>>>>>>> PS-460-queueWithoutExcel
	/**
	 * Returns the parameter value as a Boolean
	 * 
	 * @return
	 */
	public Boolean getAsBoolean() {
<<<<<<< HEAD

		checkRequired();

		return Boolean.valueOf(parameter);
	}

=======
		
		checkRequired();
		
		return Boolean.valueOf(parameter);
	}
	
>>>>>>> PS-460-queueWithoutExcel
	/**
	 * Returns the parameter value as a Integer
	 * 
	 * @return
	 */
	public Integer getAsInteger() {
<<<<<<< HEAD

		checkRequired();

		if (StringUtils.isBlank(parameter)) {

			return null;
		}

		if (!NumberUtils.isNumber(parameter)) {
			throw new JidokaFatalException(
					String.format("The param %s must be a number. The received value is %s", name, parameter));
		}

		return Integer.parseInt(parameter);
	}

=======
		
		checkRequired();
		
		if(StringUtils.isBlank(parameter)) {
			
			return null;
		}
		
		if(!NumberUtils.isNumber(parameter)) {
			throw new JidokaFatalException(String.format("The param %s must be a number. The received value is %s", name, parameter));
		} 

		return Integer.parseInt(parameter);		
	}	
	
>>>>>>> PS-460-queueWithoutExcel
	/**
	 * Returns the parameter value as a Long
	 * 
	 * @return
	 */
	public Long getAsLong() {
<<<<<<< HEAD

		checkRequired();

		if (StringUtils.isBlank(parameter)) {

			return null;
		}

		if (!NumberUtils.isNumber(parameter)) {
			throw new JidokaFatalException(
					String.format("The param %s must be a number. The received value is %s", name, parameter));
		}

		return Long.parseLong(parameter);
	}

=======
		
		checkRequired();
		
		if(StringUtils.isBlank(parameter)) {
			
			return null;
		}
		
		if(!NumberUtils.isNumber(parameter)) {
			throw new JidokaFatalException(String.format("The param %s must be a number. The received value is %s", name, parameter));
		} 

		return Long.parseLong(parameter);	
	}
	
>>>>>>> PS-460-queueWithoutExcel
	/**
	 * Returns the parameter value as a String
	 * 
	 * @return
	 */
	public String getAsString() {
<<<<<<< HEAD

		checkRequired();

		return parameter;
	}

=======
		
		checkRequired();
		
		return parameter;
	}
	
>>>>>>> PS-460-queueWithoutExcel
	/**
	 * Returns the parameter value as a File
	 * 
	 * @return
	 */
	public File getAsFile() {
<<<<<<< HEAD

		checkRequired();

		return Paths.get(server.getCurrentDir(), parameter).toFile();
	}

	private void checkRequired() {

=======
		
		checkRequired();
		
		return Paths.get(server.getCurrentDir(), parameter).toFile();
	}
	
	
	private void checkRequired() {
		
>>>>>>> PS-460-queueWithoutExcel
		if (StringUtils.isBlank(parameter) && required) {
			throw new JidokaFatalException(String.format("The param %s is mandatory", name));
		}
	}
<<<<<<< HEAD

=======
	
>>>>>>> PS-460-queueWithoutExcel
}

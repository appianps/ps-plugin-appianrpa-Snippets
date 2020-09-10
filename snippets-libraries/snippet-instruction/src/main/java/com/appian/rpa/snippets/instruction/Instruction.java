package com.appian.rpa.snippets.instruction;

import java.io.File;
import java.nio.file.Paths;
import java.util.function.Predicate;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;

/**
 * The InstructionsManager Class provides multiple methods to manage
 * Instructions from your Worflow.
 * 
 * Therefore, each single instance would represent a different instruction from
 * your Appian RPA Robotic Process.
 */
public class Instruction {

	/**
	 * JidokaServer instance.
	 */
	private IJidokaServer<?> server;

	/**
	 * Instruction name
	 */
	private String name;

	/**
	 * <code>true</code> if the instruction is required (mandatory)
	 */
	private Boolean required;

	/**
	 * This variable stores the instruction value in the console
	 */
	private String parameter;
	
	/**
	 * This variable stores a predicate to validate the instruction
	 */
	private Predicate<Object> validatePredicate;

	/**
	 * Constructor for a non required instruction
	 * 
	 * @param name of the instruction
	 */
	public Instruction(String name) {

		this(name, false);
	}

	/**
	 * Constructor for a single instruction
	 * 
	 * @param name     of the instruction
	 * @param required <code>true</code> if the instruction is required (mandatory)
	 *                 for the robot
	 */
	public Instruction(String name, Boolean required) {

		this(name, required, null);
	}
	
	/**
	 * Constructor for a single instruction
	 * 
	 * @param name     of the instruction
	 * @param required <code>true</code> if the instruction is required (mandatory)
	 *                 for the robot
	 */
	public Instruction(String name, Boolean required, Predicate<Object> validatePredicate) {

		this.server = JidokaFactory.getServer();
		this.name = name;
		this.required = required;
		this.validatePredicate = validatePredicate;

		this.parameter = server.getParameters().get(name);
	}

	/**
	 * Returns the instruction parameter value as a Boolean
	 * 
	 * @return
	 */
	public Boolean getAsBoolean() {

		validate();

		return Boolean.valueOf(parameter);
	}

	/**
	 * Return the instruction parameter value as an Integer
	 * 
	 * @return
	 */
	public Integer getAsInteger() {

		validate();

		if (StringUtils.isBlank(parameter)) {

			return null;
		}

		if (!NumberUtils.isNumber(parameter)) {
			throw new JidokaFatalException(
					String.format("The param %s must be a number. The received value is %s", name, parameter));
		}

		return Integer.parseInt(parameter);
	}

	/**
	 * Return the instruction parameter value as a Long
	 * 
	 * @return
	 */
	public Long getAsLong() {

		validate();

		if (StringUtils.isBlank(parameter)) {

			return null;
		}

		if (!NumberUtils.isNumber(parameter)) {
			throw new JidokaFatalException(
					String.format("The param %s must be a number. The received value is %s", name, parameter));
		}

		return Long.parseLong(parameter);
	}

	/**
	 * Returns the instruction parameter value as a String
	 * 
	 * @return
	 */
	public String getAsString() {

		validate();

		if(StringUtils.isNotBlank(parameter)) {
			
			parameter = parameter.trim();
		}
		
		return parameter;
	}

	/**
	 * Returns the instruction parameter value as a File
	 * 
	 * @return
	 */
	public File getAsFile() {

		validate();

		return Paths.get(server.getCurrentDir(), parameter).toFile();
	}

	/**
	 * This method ensures that a required parameter is not empty. In an affirmative
	 * case, @throws a JidokaFatalException
	 */

	private void validate() throws JidokaFatalException {

		if (StringUtils.isBlank(parameter) && required) {
			
			throw new JidokaFatalException(String.format("The param %s is mandatory", name));
		}
		
		if(validatePredicate != null && !validatePredicate.test(parameter)) {
				
			throw new JidokaFatalException(String.format("The param %s is not valid with value %s", name, parameter));
		}
	}

}

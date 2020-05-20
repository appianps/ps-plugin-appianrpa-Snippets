package com.appian.rpa.snippets.examples.instruction;

import java.io.File;

import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.annotations.Robot;

/**
 * The Instruction Robot provides a usage example of all the Instruction Snippet
 * methods. The robot reads all the instructions and assigns them to an object
 * of the appropriate type, then prints the values in the server log
 */
@Robot
public class InstructionsRobot implements IRobot {

	/**
	 * Server
	 */
	private IJidokaServer<?> server;

	/**
	 * Boolean field
	 */
	private Boolean iBoolean;

	/**
	 * Integer field
	 */
	private Integer iInteger;

	/**
	 * Long field
	 */
	private Long iLong;

	/**
	 * String field
	 */
	private String iString;

	/**
	 * File field
	 */
	private File iFile;

	/**
	 * Override startup method to initialize some variables involved in our process.
	 */
	@Override
	public boolean startUp() throws Exception {
		return true;
	}

	/**
	 * Action "start"
	 * 
	 * @return
	 * @throws Exception
	 */
	public void start() throws Exception {

		server = JidokaFactory.getServer();
	}

	/**
	 * Action "Get instructions"
	 */
	public void getInstructions() {

		// Loads robot instructions
		iBoolean = EInstructions.INST_BOOLEAN.getInstruction().getAsBoolean();
		iInteger = EInstructions.INST_INTEGER.getInstruction().getAsInteger();
		iLong = EInstructions.INST_LONG.getInstruction().getAsLong();
		iString = EInstructions.INST_STRING.getInstruction().getAsString();
		iFile = EInstructions.INST_FILE.getInstruction().getAsFile();
	}

	/**
	 * Action "Log Instructions"
	 * 
	 * @return
	 * @throws Exception
	 */
	public void logInstructions() {

		server.info(String.format("Boolean instruction, value: %s", iBoolean));
		server.info(String.format("Integer instruction, value: %s", iInteger));
		server.info(String.format("Long instruction, value: %s", iLong));
		server.info(String.format("String instruction, value: %s", iString));
		server.info(String.format("File instruction, value: %s", iFile.getName()));
	}

	/**
	 * Action "end"
	 * 
	 * @return
	 * @throws Exception
	 */
	public void end() throws Exception {
	}

	/**
	 * @see com.novayre.jidoka.client.api.IRobot#cleanUp()
	 */
	@Override
	public String[] cleanUp() throws Exception {
		return new String[0];
	}
}

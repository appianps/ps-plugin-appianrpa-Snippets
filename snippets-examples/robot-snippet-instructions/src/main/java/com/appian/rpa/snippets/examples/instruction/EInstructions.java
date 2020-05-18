package com.appian.rpa.snippets.examples.instruction;

import com.appian.rpa.snippets.instruction.Instruction;

/**
 * 
 * Enum in which all the instructions of a robot are defined
 *
 */
public enum EInstructions {

	INST_BOOLEAN("booleanInstruction", false),

	INST_INTEGER("integerInstruction", true),

	INST_LONG("longInstruction", false),

	INST_STRING("stringInstruction", false),

	INST_FILE("fileInstruction", false);

	/**
	 * Instructions field
	 */
	private Instruction instruction;

	/**
	 * Constructor
	 * 
	 * @param instructionName
	 * @param instructionRequred
	 */
	EInstructions(String instructionName, Boolean instructionRequred) {
		instruction = new Instruction(instructionName, instructionRequred);
	}

	public Instruction getInstruction() {

		return instruction;
	}
}

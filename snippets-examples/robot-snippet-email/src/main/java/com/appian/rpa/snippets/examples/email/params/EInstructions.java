package com.appian.rpa.snippets.examples.email.params;

import com.appian.rpa.snippets.instruction.Instruction;

/**
 * 
 * Enum in which all the instructions of a robot are defined
 *
 */
public enum EInstructions {

	FROM("FROM", false),
	TO("TO", true),
	CC("CC", false),
	BCC("BCC", false),
	SUBJECT("SUBJECT", true),
	BODY("BODY", false);
	

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


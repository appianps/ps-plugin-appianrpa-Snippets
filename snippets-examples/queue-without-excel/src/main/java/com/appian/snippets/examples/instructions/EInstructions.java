package com.appian.snippets.examples.instructions;

import com.appian.rpa.snippets.commons.instructions.Instruction;

/**
 * 
 * Enum in which all the instructions of a robot are defined
 *
 */
public enum EInstructions {
	
	FOLDER ("folder", true);
	
	private Instruction instruction;
	
	EInstructions(String instructionName, Boolean instructionRequred){
		instruction = new Instruction(instructionName, instructionRequred);
	}
	
	public Instruction getInstruction() {
		
		return instruction;
	}
}
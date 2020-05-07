package com.appian.snippets.examples;

import java.io.Serializable;

public class FileModel implements Serializable	{

	/** Serial */
	private static final long serialVersionUID = 1L;
	
	/** File number of words */
	private Integer numOfWords;
	
	/** File content passed as a string */
	private String fileContent;
	
	/** Gets the number of words */
	public Integer getNumOfWords() {
		return numOfWords;
	}

	/** Sets the number of words */
	public void setNumOfWords(Integer numOfWords) {
		this.numOfWords = numOfWords;
	}

	/** Gets the file content as a String */
	public String getFileContent() {
		return fileContent;
	}

	/** Sets the file content */
	public void setFileContent(String fileContent) {
		this.fileContent = fileContent;
	}

}

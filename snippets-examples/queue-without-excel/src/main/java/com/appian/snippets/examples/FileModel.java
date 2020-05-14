package com.appian.snippets.examples;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;

import org.apache.commons.io.FilenameUtils;

import com.appian.rpa.snippets.queuemanager.annotations.AItemField;
import com.appian.rpa.snippets.queuemanager.annotations.AItemKey;
import com.novayre.jidoka.client.api.exceptions.JidokaItemException;

public class FileModel implements Serializable {

	public FileModel() {
	}

	public FileModel(File file) {
		this.fileName = FilenameUtils.getBaseName(file.getName());
		this.fileContent = getContent(file);
		this.numOfWords = 0;
	}

	private String getContent(File file) {

		String content = "";
		try {
			content = new String(Files.readAllBytes(file.toPath()));
		} catch (IOException e) {
			throw new JidokaItemException(
					"Error getting the file " + FilenameUtils.getBaseName(file.getName()) + " content");
		}
		return content;

	}

	/** Serial */
	private static final long serialVersionUID = 1L;

	/** File name */
	@AItemKey
	@AItemField(fieldName = "FILE_NAME")
	private String fileName;

	/** File number of words */
	@AItemField(fieldName = "NUM_OF_WORDS")
	private Integer numOfWords;

	/** File content passed as a string */
	@AItemField(fieldName = "FILE_CONTENT")
	private String fileContent;

	/** Gets the file name */
	public String getFileName() {
		return fileName;
	}

	/** Sets the file name */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

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

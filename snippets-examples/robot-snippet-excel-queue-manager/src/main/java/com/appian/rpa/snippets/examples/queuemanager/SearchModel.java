package com.appian.rpa.snippets.examples.queuemanager;

import java.io.Serializable;

import com.appian.rpa.snippets.queuemanager.annotations.AItemField;
import com.appian.rpa.snippets.queuemanager.annotations.AItemKey;

public class SearchModel implements Serializable {

	/** Serial version */
	private static final long serialVersionUID = 1L;

	@AItemKey
	@AItemField(fieldName = "SEARCH TERM")
	private String searchTerm;

	@AItemField(fieldName = "NUMBER OF RESULTS")
	private String numberOfResults;

	@AItemField(fieldName = "TIME REQUIRED")
	private String timeRequired;

	/* GETTERS AND SETTERS */

	public String getSearchTerm() {
		return searchTerm;
	}

	public void setSearchTerm(String searchTerm) {
		this.searchTerm = searchTerm;
	}

	public String getNumberOfResults() {
		return numberOfResults;
	}

	public void setNumberOfResults(String numberOfResults) {
		this.numberOfResults = numberOfResults;
	}

	public String getTimeRequired() {
		return timeRequired;
	}

	public void setTimeRequired(String timeRequired) {
		this.timeRequired = timeRequired;
	}

}

package com.appian.rpa.snippets.examples.email.model;

public class CovidModel {
	
	/** Area to search */
	private String areaToSearch;
	
	/** Active cases */
	private Integer activeCases;
	
	/** Fatal cases */
	private Integer fatalCases;
	
	/** Recovered cases */
	private Integer recoveredCases;

	public String getAreaToSearch() {
		return areaToSearch;
	}

	public void setAreaToSearch(String areaToSearch) {
		this.areaToSearch = areaToSearch;
	}

	public Integer getActiveCases() {
		return activeCases;
	}

	public void setActiveCases(Integer activeCases) {
		this.activeCases = activeCases;
	}

	public Integer getFatalCases() {
		return fatalCases;
	}

	public void setFatalCases(Integer fatalCases) {
		this.fatalCases = fatalCases;
	}

	public Integer getRecoveredCases() {
		return recoveredCases;
	}

	public void setRecoveredCases(Integer recoveredCases) {
		this.recoveredCases = recoveredCases;
	}
	
}

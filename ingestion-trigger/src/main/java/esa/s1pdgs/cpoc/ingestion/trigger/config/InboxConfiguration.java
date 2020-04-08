package esa.s1pdgs.cpoc.ingestion.trigger.config;

import esa.s1pdgs.cpoc.common.ProductFamily;

public class InboxConfiguration {
	private String directory;
	private String matchRegex;
	private String ignoreRegex;
	private String topic;
	private String stationName;
	private String sessionNamePattern = "^([a-z_]{4}/)?"
			+ "([0-9a-z_]{2})([0-9a-z_]{1})/([0-9a-z_]+)/(ch[0|_]?[1-2]/)?"
			+ "(DCS_[0-9]{2}_([a-zA-Z0-9_]*)_ch([12])_(DSDB|DSIB).*\\.(raw|aisp|xml))$";
	private int sessionNameGroupIndex = 4;
	
	private ProductFamily family = ProductFamily.BLANK;

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(final String directory) {
		this.directory = directory;
	}

	public String getMatchRegex() {
		return matchRegex;
	}

	public void setMatchRegex(final String matchRegex) {
		this.matchRegex = matchRegex;
	}

	public String getIgnoreRegex() {
		return ignoreRegex;
	}

	public void setIgnoreRegex(final String ignoreRegex) {
		this.ignoreRegex = ignoreRegex;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(final String topic) {
		this.topic = topic;
	}
	
	public ProductFamily getFamily() {
		return family;
	}

	public void setFamily(final ProductFamily family) {
		this.family = family;
	}
	
	public String getStationName() {
		return stationName;
	}

	public void setStationName(final String stationName) {
		this.stationName = stationName;
	}

	public String getSessionNamePattern() {
		return sessionNamePattern;
	}

	public void setSessionNamePattern(final String sessionNamePattern) {
		this.sessionNamePattern = sessionNamePattern;
	}

	public int getSessionNameGroupIndex() {
		return sessionNameGroupIndex;
	}

	public void setSessionNameGroupIndex(final int sessionNameGroupIndex) {
		this.sessionNameGroupIndex = sessionNameGroupIndex;
	}

	@Override
	public String toString() {
		return "InboxConfiguration [directory=" + directory + ", matchRegex=" + matchRegex + ", ignoreRegex="
				+ ignoreRegex + ", topic=" + topic + ", stationName=" + stationName + ", sessionNamePattern="
				+ sessionNamePattern + ", sessionNameGroupIndex=" + sessionNameGroupIndex + ", family=" + family + "]";
	}	
}
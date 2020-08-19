package esa.s1pdgs.cpoc.ingestion.trigger.config;

import java.util.Date;

import esa.s1pdgs.cpoc.common.ProductFamily;

public class InboxConfiguration {	
	private String directory;
	private String matchRegex;
	private String ignoreRegex;
	private String topic;
	private String stationName;
	private String mode;
	private String timeliness;
	private String sessionNamePattern = "^([a-z_]{4}/)?"
			+ "([0-9a-z_]{2})([0-9a-z_]{1})/(([0-9a-z_]+)/(ch[0|_]?[1-2]/)?"
			+ "(DCS_[0-9]{2}_([a-zA-Z0-9_]*)_ch([12])_(DSDB|DSIB).*\\.(raw|aisp|xml)))$";
	private int sessionNameGroupIndex = 4;
	
	private Date ignoreFilesBeforeDate = ConfigDateConverter.DEFAULT_START_DATE;
	
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
	
	public String getMode() {
		return mode;
	}

	public void setMode(final String mode) {
		this.mode = mode;
	}

	public String getTimeliness() {
		return timeliness;
	}

	public void setTimeliness(final String timeliness) {
		this.timeliness = timeliness;
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

	public Date getIgnoreFilesBeforeDate() {
		return ignoreFilesBeforeDate;
	}

	public void setIgnoreFilesBeforeDate(final Date ignoreFilesBeforeDate) {
		this.ignoreFilesBeforeDate = ignoreFilesBeforeDate;
	}

	@Override
	public String toString() {
		return "InboxConfiguration [directory=" + directory + ", matchRegex=" + matchRegex + ", ignoreRegex="
				+ ignoreRegex + ", topic=" + topic + ", stationName=" + stationName + ", mode=" + mode + ", timeliness="
				+ timeliness + ", sessionNamePattern=" + sessionNamePattern + ", sessionNameGroupIndex="
				+ sessionNameGroupIndex + ", ignoreFilesBeforeDate=" + ignoreFilesBeforeDate + ", family=" + family
				+ "]";
	}
}
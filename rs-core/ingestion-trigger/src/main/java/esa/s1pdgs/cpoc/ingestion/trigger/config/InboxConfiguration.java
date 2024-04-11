/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package esa.s1pdgs.cpoc.ingestion.trigger.config;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import esa.s1pdgs.cpoc.common.ProductFamily;

public class InboxConfiguration {
	private String type;
	private String directory;
	private String matchRegex;
	private String ignoreRegex;
	private String missionId;
	private String stationName;
	private String mode;
	private String timeliness = "";
	private String sessionNamePattern = "^([a-z_]{4}/)?"
			+ "([0-9a-z_]{2})([0-9a-z_]{1})/(([0-9a-z_]+)/(ch[0|_]?[1-2]/)?"
			+ "(DCS_[0-9]{2}_([a-zA-Z0-9_]*)_ch([12])_(DSDB|DSIB).*\\.(raw|aisp|xml)))$";
	private int sessionNameGroupIndex = 4;
	
	private Date ignoreFilesBeforeDate = ConfigDateConverter.DEFAULT_START_DATE;
	
	private ProductFamily family = ProductFamily.BLANK;
	
	private int stationRetentionTime = 0; // how many days to keep persisted data about inbox files at a minimum
	
	private boolean ftpDirectoryListing = false;
	
	private String pathPattern = null;
	private Map<String,Integer> pathMetadataElements = new HashMap<>();
	
	private String satelliteId = "";

	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
	}

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
	
	public ProductFamily getFamily() {
		return family;
	}

	public void setFamily(final ProductFamily family) {
		this.family = family;
	}
	
	public String getMissionId() {
		return missionId;
	}

	public void setMissionId(String missionId) {
		this.missionId = missionId;
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
	
	public int getStationRetentionTime() {
		return stationRetentionTime;
	}

	public void setStationRetentionTime(final int stationRetentionTime) {
		this.stationRetentionTime = stationRetentionTime;
	}
	
	public boolean isFtpDirectoryListing() {
		return ftpDirectoryListing;
	}

	public void setFtpDirectoryListing(final boolean ftpDirectoryListing) {
		this.ftpDirectoryListing = ftpDirectoryListing;
	}		

	public String getPathPattern() {
		return pathPattern;
	}

	public void setPathPattern(final String pathPattern) {
		this.pathPattern = pathPattern;
	}

	public Map<String, Integer> getPathMetadataElements() {
		return pathMetadataElements;
	}

	public void setPathMetadataElements(final Map<String, Integer> pathMetadataElements) {
		this.pathMetadataElements = pathMetadataElements;
	}

	public String getSatelliteId() {
		return satelliteId;
	}

	public void setSatelliteId(String satelliteId) {
		this.satelliteId = satelliteId;
	}
	
	@Override
	public String toString() {
		return "InboxConfiguration [type=" + type + ", directory=" + directory + ", matchRegex=" + matchRegex
				+ ", ignoreRegex=" + ignoreRegex + ", missionId=" + missionId + ", stationName=" + stationName
				+ ", mode=" + mode + ", timeliness=" + timeliness + ", sessionNamePattern=" + sessionNamePattern
				+ ", sessionNameGroupIndex=" + sessionNameGroupIndex + ", ignoreFilesBeforeDate="
				+ ignoreFilesBeforeDate + ", family=" + family + ", stationRetentionTime=" + stationRetentionTime
				+ ", ftpDirectoryListing=" + ftpDirectoryListing + ", pathPattern=" + pathPattern
				+ ", pathMetadataElements=" + pathMetadataElements + ", satelliteId=" + satelliteId + "]";
	}
}
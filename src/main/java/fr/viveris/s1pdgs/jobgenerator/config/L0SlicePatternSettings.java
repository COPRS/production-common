package fr.viveris.s1pdgs.jobgenerator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "pattern-l0-slices")
public class L0SlicePatternSettings {
	
	private String regexp;
	
	private int placeMatchSatelliteId;
	
	private int placeMatchMissionId;
	
	private int placeMatchAcquisition;
	
	private int placeMatchStartTime;
	
	private int placeMatchStopTime;

	public L0SlicePatternSettings() {
		
	}

	/**
	 * @return the regexp
	 */
	public String getRegexp() {
		return regexp;
	}

	/**
	 * @param regexp the regexp to set
	 */
	public void setRegexp(String regexp) {
		this.regexp = regexp;
	}

	/**
	 * @return the placeMatchSatelliteId
	 */
	public int getPlaceMatchSatelliteId() {
		return placeMatchSatelliteId;
	}

	/**
	 * @param placeMatchSatelliteId the placeMatchSatelliteId to set
	 */
	public void setPlaceMatchSatelliteId(int placeMatchSatelliteId) {
		this.placeMatchSatelliteId = placeMatchSatelliteId;
	}

	/**
	 * @return the placeMatchMissionId
	 */
	public int getPlaceMatchMissionId() {
		return placeMatchMissionId;
	}

	/**
	 * @param placeMatchMissionId the placeMatchMissionId to set
	 */
	public void setPlaceMatchMissionId(int placeMatchMissionId) {
		this.placeMatchMissionId = placeMatchMissionId;
	}

	/**
	 * @return the placeMatchAcquisition
	 */
	public int getPlaceMatchAcquisition() {
		return placeMatchAcquisition;
	}

	/**
	 * @param placeMatchAcquisition the placeMatchAcquisition to set
	 */
	public void setPlaceMatchAcquisition(int placeMatchAcquisition) {
		this.placeMatchAcquisition = placeMatchAcquisition;
	}

	/**
	 * @return the placeMatchStartTime
	 */
	public int getPlaceMatchStartTime() {
		return placeMatchStartTime;
	}

	/**
	 * @param placeMatchStartTime the placeMatchStartTime to set
	 */
	public void setPlaceMatchStartTime(int placeMatchStartTime) {
		this.placeMatchStartTime = placeMatchStartTime;
	}

	/**
	 * @return the placeMatchStopTime
	 */
	public int getPlaceMatchStopTime() {
		return placeMatchStopTime;
	}

	/**
	 * @param placeMatchStopTime the placeMatchStopTime to set
	 */
	public void setPlaceMatchStopTime(int placeMatchStopTime) {
		this.placeMatchStopTime = placeMatchStopTime;
	}

}

package esa.s1pdgs.cpoc.jobgenerator.config;

import java.time.format.DateTimeFormatter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Properties around the extraction of the L0 slices information
 * 
 * @author Cyrielle Gailliard
 *
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "pattern-l0-slices")
public class L0SlicePatternSettings {
    
    public final static DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

	/**
	 * The regular expression in Java format
	 */
	private String regexp;

	/**
	 * The id of the matcher.group for the satellite identifier according the
	 * regexp.
	 */
	private int mGroupSatId;

	/**
	 * The id of the matcher.group for the mission identifier according the regexp.
	 */
	private int mGroupMissionId;

	/**
	 * The id of the matcher.group for the acquisition according the regexp.
	 */
	private int mGroupAcquisition;

	/**
	 * The id of the matcher.group for the start time according the regexp.
	 */
	private int mGroupStartTime;

	/**
	 * The id of the matcher.group for the stop time according the regexp.
	 */
	private int mGroupStopTime;

	/**
	 * Default constructor
	 */
	public L0SlicePatternSettings() {
		mGroupSatId = 0;
		mGroupMissionId = 0;
		mGroupAcquisition = 0;
		mGroupStartTime = 0;
		mGroupStopTime = 0;
	}

	/**
	 * @return the regexp
	 */
	public String getRegexp() {
		return regexp;
	}

	/**
	 * @param regexp
	 *            the regexp to set
	 */
	public void setRegexp(final String regexp) {
		this.regexp = regexp;
	}

	/**
	 * @return the mGroupSatId
	 */
	public int getMGroupSatId() {
		return mGroupSatId;
	}

	/**
	 * @param mGroupSatId
	 *            the mGroupSatId to set
	 */
	public void setMGroupSatId(final int mGroupSatId) {
		this.mGroupSatId = mGroupSatId;
	}

	/**
	 * @return the mGroupMissionId
	 */
	public int getMGroupMissionId() {
		return mGroupMissionId;
	}

	/**
	 * @param mGroupMissionId
	 *            the mGroupMissionId to set
	 */
	public void setMGroupMissionId(final int mGroupMissionId) {
		this.mGroupMissionId = mGroupMissionId;
	}

	/**
	 * @return the getMGroupAcquisition
	 */
	public int getMGroupAcquisition() {
		return mGroupAcquisition;
	}

	/**
	 * @param mGroupAcquisition
	 *            the mGroupAcquisition to set
	 */
	public void setMGroupAcquisition(final int mGroupAcquisition) {
		this.mGroupAcquisition = mGroupAcquisition;
	}

	/**
	 * @return the mGroupStartTime
	 */
	public int getMGroupStartTime() {
		return mGroupStartTime;
	}

	/**
	 * @param mGroupStartTime
	 *            the mGroupStartTime to set
	 */
	public void setMGroupStartTime(final int mGroupStartTime) {
		this.mGroupStartTime = mGroupStartTime;
	}

	/**
	 * @return the mGroupStopTime
	 */
	public int getMGroupStopTime() {
		return mGroupStopTime;
	}

	/**
	 * @param mGroupStopTime
	 *            the mGroupStopTime to set
	 */
	public void setMGroupStopTime(final int mGroupStopTime) {
		this.mGroupStopTime = mGroupStopTime;
	}

}

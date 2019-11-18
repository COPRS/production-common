package esa.s1pdgs.cpoc.inbox.config;

import java.nio.file.Path;
import java.nio.file.Paths;

public class InboxPathInformation {

	private String missionId;
	private String satelliteId;
	private String stationCode;

	public String getMissionId() {
		return missionId;
	}

	public void setMissionId(String missionId) {
		this.missionId = missionId;
	}

	public String getSatelliteId() {
		return satelliteId;
	}

	public void setSatelliteId(String satelliteId) {
		this.satelliteId = satelliteId;
	}

	public String getStationCode() {
		return stationCode;
	}

	public void setStationCode(String stationCode) {
		this.stationCode = stationCode;
	}

	/**
	 * Returns empty String, if the directory structure is not like following
	 * convention!
	 * 
	 * Expected directory structure for CGS/EDRS sessions is:
	 * 
	 * {...}/inboxpath/[4-letter Station Code]/[3-letter Mission & Satellite - ID]
	 * 
	 * Examples: /data/local-ingestor/WILE/S1A, /data/local-ingestor/WILE/S1B,
	 * /data/local-ingestor/MTI_/S1A, /data/local-ingestor/MTI_/S1B
	 * 
	 * @param directory
	 * @return missionId or empty String
	 */
	public static String extractMissionIdFromInboxDirectoryName(String directory) {
		Path inboxPath = Paths.get(directory);
		if (hasSessionDirectoryStructure(directory)) {
			return inboxPath.getName(inboxPath.getNameCount() - 1).toString().substring(0, 2);
		} else {
			return "";
		}
	}

	/**
	 * Returns empty String, if the directory structure is not like following
	 * convention!
	 * 
	 * Expected directory structure for CGS/EDRS sessions is:
	 * 
	 * {...}/inboxpath/[4-letter Station Code]/[3-letter Mission & Satellite - ID]
	 * 
	 * Examples: /data/local-ingestor/WILE/S1A, /data/local-ingestor/WILE/S1B,
	 * /data/local-ingestor/MTI_/S1A, /data/local-ingestor/MTI_/S1B
	 * 
	 * @param directory
	 * @return satelliteId or empty String
	 */
	public static String extractSatelliteIdFromInboxDirectoryName(String directory) {
		Path inboxPath = Paths.get(directory);
		if (hasSessionDirectoryStructure(directory)) {
			return inboxPath.getName(inboxPath.getNameCount() - 1).toString().substring(2, 3);
		} else {
			return "";
		}

	}

	/**
	 * Returns empty String, if the directory structure is not like following
	 * convention!
	 * 
	 * Expected directory structure for CGS/EDRS sessions is:
	 * 
	 * {...}/inboxpath/[4-letter Station Code]/[3-letter Mission & Satellite - ID]
	 * 
	 * Examples: /data/local-ingestor/WILE/S1A, /data/local-ingestor/WILE/S1B,
	 * /data/local-ingestor/MTI_/S1A, /data/local-ingestor/MTI_/S1B
	 * 
	 * @param directory
	 * @return stationCode or empty String
	 */
	public static String extractStationCodeFromInboxDirectoryName(String directory) {
		Path inboxPath = Paths.get(directory);
		if (hasSessionDirectoryStructure(directory)) {
			return inboxPath.getName(inboxPath.getNameCount() - 2).toString();
		} else {
			return "";
		}
	}

	static boolean hasSessionDirectoryStructure(String directory) {

		Path inboxPath = Paths.get(directory);
		if (inboxPath.getNameCount() < 2 || (inboxPath.getName(inboxPath.getNameCount() - 1).toString().length() != 3)
				|| (inboxPath.getName(inboxPath.getNameCount() - 2).toString().length() != 4)) {
			return false;
		} else {
			return true;
		}
	}

}

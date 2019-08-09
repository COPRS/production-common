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

	public static String extractMissionIdFromInboxDirectoryName(String directory) {
		Path inboxPath = Paths.get(directory);
		assertInboxDirectoryHasExpectedStructure(directory);
		return inboxPath.getName(inboxPath.getNameCount() - 1).toString().substring(0, 2);
	}

	public static String extractSatelliteIdFromInboxDirectoryName(String directory) {
		Path inboxPath = Paths.get(directory);
		assertInboxDirectoryHasExpectedStructure(directory);
		return inboxPath.getName(inboxPath.getNameCount() - 1).toString().substring(2, 3);
	}

	public static String extractStationCodeFromInboxDirectoryName(String directory) {
		Path inboxPath = Paths.get(directory);
		assertInboxDirectoryHasExpectedStructure(directory);
		return inboxPath.getName(inboxPath.getNameCount() - 2).toString();
	}

	/**
	 * Expected directory structure is:
	 * 
	 * {...}/inboxpath/[4-letter Station Code]/[3-letter Mission & Satellite - ID]
	 * 
	 * Examples: /data/local-ingestor/WILE/S1A, /data/local-ingestor/WILE/S1B,
	 * /data/local-ingestor/MTI_/S1A, /data/local-ingestor/MTI_/S1B
	 * 
	 * @throws IllegalArgumentException if the inbox directory structure is not
	 *                                  valid
	 */
	public static void assertInboxDirectoryHasExpectedStructure(String directory) {

		Path inboxPath = Paths.get(directory);
		if (inboxPath.getNameCount() < 2 || (inboxPath.getName(inboxPath.getNameCount() - 1).toString().length() != 3)
				|| (inboxPath.getName(inboxPath.getNameCount() - 2).toString().length() != 4)) {
			throw new IllegalArgumentException(String.format(
					"expected inbox directory structure is  {...}/inboxpath/[4-letter Station Code]/[3-letter Mission & Satellite - ID] but was %s",
					directory));
		}
	}

}

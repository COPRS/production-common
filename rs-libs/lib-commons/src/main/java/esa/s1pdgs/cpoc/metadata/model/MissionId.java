package esa.s1pdgs.cpoc.metadata.model;

public enum MissionId {
	S1,
	S2,
	S3,
	UNDEFINED;
	
	public static final String FIELD_NAME = "missionId";
	
	public static MissionId fromFileName(String fileName) {

		if (fileName == null) {
			throw new IllegalArgumentException("filename is null"); 
		}
		if (fileName.isEmpty()) {
			throw new IllegalArgumentException("filename is empty");
		}
		if (fileName.startsWith("S1")) {
			return S1;
		} else if (fileName.startsWith("S2")) {
			return S2;
		} else if (fileName.startsWith("S3")) {
			return S3;
		} else {
			throw new IllegalArgumentException("cannot extract mission from filename: " + fileName);
		}
	}
	
	public static String toPlatformShortName(MissionId missionId) {
		if (missionId == S1) {
			return "SENTINEL-1";
		} else if (missionId == S2) {
			return "SENTINEL-2";
		} else if (missionId == S3) {
			return "SENTINEL-3";
		} else {
			return "UNDEFINED";
		}
	}

}

package esa.s1pdgs.cpoc.metadata.model;

import esa.s1pdgs.cpoc.common.ProductFamily;

public enum MissionId {
	S1,
	S2,
	S3,
	UNDEFINED;
	
	public static final String FIELD_NAME = "missionId";
	
	public static MissionId fromFamilyOrFileName(ProductFamily productFamily, String filename) {
		
		if (productFamily == null) {
			throw new IllegalArgumentException("productFamily is null");
		}
		if (productFamily == ProductFamily.EDRS_SESSION) {
			return S1;
		} else {
			return fromFileName(filename);
		}
	}
	
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

}

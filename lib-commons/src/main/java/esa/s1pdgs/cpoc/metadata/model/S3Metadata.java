package esa.s1pdgs.cpoc.metadata.model;

import java.util.Objects;

/**
 * Describes metadata returned by queries for the S3 mission
 * 
 * @author Julian Kaping
 *
 */
public class S3Metadata extends AbstractMetadata {

	private int granuleNumber;
	private String granulePosition;

	public S3Metadata(final String productName, final String productType, final String keyObjectStorage,
			final String validityStart, final String validityStop, final String missionId, final String satelliteId,
			final String stationCode, final int granuleNumber, final String granulePosition) {
		super(productName, productType, keyObjectStorage, validityStart, validityStop, missionId, satelliteId,
				stationCode);
		this.granuleNumber = granuleNumber;
		this.granulePosition = granulePosition;
	}
	
	public S3Metadata() {
		
	}

	public int getGranuleNumber() {
		return granuleNumber;
	}

	public void setGranuleNumber(int granuleNumber) {
		this.granuleNumber = granuleNumber;
	}

	public String getGranulePosition() {
		return granulePosition;
	}

	public void setGranulePosition(String granulePosition) {
		this.granulePosition = granulePosition;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(granuleNumber, granulePosition);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof S3Metadata)) {
			return false;
		}
		S3Metadata other = (S3Metadata) obj;
		return granuleNumber == other.granuleNumber && Objects.equals(granulePosition, other.granulePosition);
	}

	@Override
	public String toString() {
		return "S3Metadata [granuleNumber=" + granuleNumber + ", granulePosition=" + granulePosition + ", productName="
				+ productName + ", productType=" + productType + ", keyObjectStorage=" + keyObjectStorage
				+ ", validityStart=" + validityStart + ", validityStop=" + validityStop + ", missionId=" + missionId
				+ ", satelliteId=" + satelliteId + ", stationCode=" + stationCode + "]";
	}
}

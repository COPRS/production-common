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
	private String creationTime;

	public S3Metadata(final String productName, final String productType, final String keyObjectStorage,
			final String validityStart, final String validityStop, final String missionId, final String satelliteId,
			final String stationCode, final int granuleNumber, final String granulePosition,
			final String creationTime) {
		super(productName, productType, keyObjectStorage, validityStart, validityStop, missionId, satelliteId,
				stationCode);
		this.granuleNumber = granuleNumber;
		this.granulePosition = granulePosition;
		this.setCreationTime(creationTime);
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

	public String getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(String creationTime) {
		this.creationTime = creationTime;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(granuleNumber, granulePosition, creationTime);
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
		return granuleNumber == other.granuleNumber && Objects.equals(granulePosition, other.granulePosition)
				&& Objects.equals(creationTime, other.creationTime);
	}

	@Override
	public String toString() {
		return "S3Metadata [granuleNumber=" + granuleNumber + ", granulePosition=" + granulePosition + ", creationTime="
				+ creationTime + ", productName=" + productName + ", productType=" + productType + ", keyObjectStorage="
				+ keyObjectStorage + ", validityStart=" + validityStart + ", validityStop=" + validityStop
				+ ", missionId=" + missionId + ", satelliteId=" + satelliteId + ", stationCode=" + stationCode + "]";
	}
}

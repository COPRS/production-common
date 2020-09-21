package esa.s1pdgs.cpoc.metadata.model;

import java.util.Objects;

/**
 * Describes metadata returned by queries for the S3 mission
 * 
 * @author Julian Kaping
 *
 */
public class S3Metadata extends AbstractMetadata {

	private String absoluteStartOrbit;
	private String anxTime;
	private String anx1Time;
	private String creationTime;
	private int granuleNumber;
	private String granulePosition;
	private String insertionTime;

	public S3Metadata(final String productName, final String productType, final String keyObjectStorage,
			final String validityStart, final String validityStop, final String missionId, final String satelliteId,
			final String stationCode, final int granuleNumber, final String granulePosition, final String creationTime,
			final String anxTime, final String anx1Time) {
		super(productName, productType, keyObjectStorage, validityStart, validityStop, missionId, satelliteId,
				stationCode);
		this.granuleNumber = granuleNumber;
		this.granulePosition = granulePosition;
		this.creationTime = creationTime;
		this.anxTime = anxTime;
		this.anx1Time = anx1Time;
	}

	public S3Metadata() {
	}

	public String getAbsoluteStartOrbit() {
		return absoluteStartOrbit;
	}

	public void setAbsoluteStartOrbit(String absoluteStartOrbit) {
		this.absoluteStartOrbit = absoluteStartOrbit;
	}
	
	public String getAnxTime() {
		return anxTime;
	}

	public void setAnxTime(String anxTime) {
		this.anxTime = anxTime;
	}

	public String getAnx1Time() {
		return anx1Time;
	}

	public void setAnx1Time(String anx1Time) {
		this.anx1Time = anx1Time;
	}
	
	public String getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(String creationTime) {
		this.creationTime = creationTime;
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
	
	public String getInsertionTime() {
		return insertionTime;
	}

	public void setInsertionTime(String insertionTime) {
		this.insertionTime = insertionTime;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(absoluteStartOrbit, anx1Time, anxTime, creationTime, granuleNumber,
				granulePosition, insertionTime);
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
		return Objects.equals(absoluteStartOrbit, other.absoluteStartOrbit) && Objects.equals(anx1Time, other.anx1Time)
				&& Objects.equals(anxTime, other.anxTime) && Objects.equals(creationTime, other.creationTime)
				&& granuleNumber == other.granuleNumber && Objects.equals(granulePosition, other.granulePosition)
				&& Objects.equals(insertionTime, other.insertionTime);
	}

	@Override
	public String toString() {
		return "S3Metadata [absoluteStartOrbit=" + absoluteStartOrbit + ", anxTime=" + anxTime + ", anx1Time="
				+ anx1Time + ", creationTime=" + creationTime + ", granuleNumber=" + granuleNumber
				+ ", granulePosition=" + granulePosition + ", insertionTime=" + insertionTime + ", productName="
				+ productName + ", productType=" + productType + ", keyObjectStorage=" + keyObjectStorage
				+ ", validityStart=" + validityStart + ", validityStop=" + validityStop + ", missionId=" + missionId
				+ ", satelliteId=" + satelliteId + ", stationCode=" + stationCode + "]";
	}
}

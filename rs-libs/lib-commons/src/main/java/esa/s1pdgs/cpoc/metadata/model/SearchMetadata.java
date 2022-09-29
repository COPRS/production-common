package esa.s1pdgs.cpoc.metadata.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Class describing the metdata of a file
 * 
 * @author Cyrielle Gailliard
 *
 */
public class SearchMetadata extends AbstractMetadata {

	private List<List<Double>> footprint;
	
	private String insertionTime;
	
	/**
	 * Constructor using fields
	 * 
	 * @param productName
	 * @param productType
	 * @param keyObjectStorage
	 * @param validityStart
	 * @param validityStop
	 * @param missionId
     * @param satelliteId
     * @param stationCode
	 */
	public SearchMetadata(final String productName, final String productType, final String keyObjectStorage,
			final String validityStart, final String validityStop, final String missionId, final String satelliteId,
			final String stationCode) {
		super(productName, productType, keyObjectStorage, validityStart, validityStop, missionId, satelliteId, stationCode);
		this.footprint = new ArrayList<>(); // FIXME: Make footprint available as constructor argument
	}
	
	public SearchMetadata() {
		
	}
	
	public List<List<Double>> getFootprint() {
		return footprint;
	}

	public void setFootprint(final List<List<Double>> footprint) {
		this.footprint = footprint;
	}

	public String getInsertionTime() {
		return insertionTime;
	}

	public void setInsertionTime(final String insertionTime) {
		this.insertionTime = insertionTime;
	}

	public String toJsonString() {
		return String.format("{%s}", super.toAbstractString());
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(
				additionalProperties,
				footprint,
				insertionTime,
				keyObjectStorage,
				missionId,
				productName,
				productType,
				satelliteId,
				stationCode,
				swathtype,
				validityStart,
				validityStop);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final SearchMetadata other = (SearchMetadata) obj;
		return Objects.equals(additionalProperties, other.additionalProperties)
				&& Objects.equals(footprint, other.footprint) && Objects.equals(insertionTime, other.insertionTime)
				&& Objects.equals(keyObjectStorage, other.keyObjectStorage)
				&& Objects.equals(missionId, other.missionId) && Objects.equals(productName, other.productName)
				&& Objects.equals(productType, other.productType) && Objects.equals(satelliteId, other.satelliteId)
				&& Objects.equals(stationCode, other.stationCode) && Objects.equals(swathtype, other.swathtype)
				&& Objects.equals(validityStart, other.validityStart)
				&& Objects.equals(validityStop, other.validityStop);
	}

	@Override
	public String toString() {
		return "SearchMetadata [footprint=" + footprint + ", insertionTime=" + insertionTime + ", productName="
				+ productName + ", productType=" + productType + ", keyObjectStorage=" + keyObjectStorage
				+ ", validityStart=" + validityStart + ", validityStop=" + validityStop + ", missionId=" + missionId
				+ ", satelliteId=" + satelliteId + ", stationCode=" + stationCode + ", swathtype=" + swathtype
				+ ", additionalProperties=" + additionalProperties + "]";
	}

	
}

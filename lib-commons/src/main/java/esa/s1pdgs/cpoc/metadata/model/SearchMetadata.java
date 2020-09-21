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

	public void setFootprint(List<List<Double>> footprint) {
		this.footprint = footprint;
	}

	public String toJsonString() {
		return String.format("{%s}", super.toAbstractString());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		SearchMetadata metadata = (SearchMetadata) o;
		return Objects.equals(footprint, metadata.footprint);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), footprint);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */



	@Override
	public String toString() {
		return "SearchMetadata [productName=" + productName + ", productType=" + productType + ", keyObjectStorage="
				+ keyObjectStorage + ", validityStart=" + validityStart + ", validityStop=" + validityStop
				+ ", missionId=" + missionId + ", satelliteId=" + satelliteId + ", stationCode=" + stationCode + ", footprint=" + footprint + "]";
	}
}

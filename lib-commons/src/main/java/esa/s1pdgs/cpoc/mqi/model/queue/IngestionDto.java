package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Objects;

import esa.s1pdgs.cpoc.common.ProductFamily;

public class IngestionDto extends AbstractDto {

	private String productUrl;
	private String missionId;
	private String satelliteId;
	private String stationCode;

	public IngestionDto() {
		super();
	}

	public IngestionDto(String productName, String productUrl) {
		super(productName, ProductFamily.BLANK);
		this.productUrl = productUrl;
	}

	public String getProductUrl() {
		return productUrl;
	}

	public void setProductUrl(String productUrl) {
		this.productUrl = productUrl;
	}

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
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(getProductName(), getFamily(), productUrl, missionId, satelliteId, stationCode);
	}

	/**
	 * @see java.lang.Object#equals()
	 */
	@Override
	public boolean equals(final Object obj) {
		boolean ret;
		if (this == obj) {
			ret = true;
		} else if (obj == null || getClass() != obj.getClass()) {
			ret = false;
		} else {
			final IngestionDto other = (IngestionDto) obj;
			// field comparison
			ret = Objects.equals(getProductName(), other.getProductName())
					&& Objects.equals(productUrl, other.productUrl) && Objects.equals(getFamily(), other.getFamily())
					&& Objects.equals(missionId, other.missionId) && Objects.equals(satelliteId, other.satelliteId)
					&& Objects.equals(stationCode, other.stationCode);
		}
		return ret;
	}

	@Override
	public String toString() {
		return String.format("IngestionDto [productUrl=%s, missionId=%s, satelliteId=%s, stationCode=%s]", productUrl,
				missionId, satelliteId, stationCode);
	}
}

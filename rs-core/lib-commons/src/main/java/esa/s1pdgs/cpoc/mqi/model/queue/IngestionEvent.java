package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mqi.model.control.AllowedAction;

/**
 * DTO object used to transfer EDRS session files between MQI and application
 * 
 * @author Viveris technologies
 */
public class IngestionEvent extends AbstractMessage {
	private String productName = NOT_DEFINED;
	private String relativePath = NOT_DEFINED;
	private String missionId = NOT_DEFINED;
	private long productSizeByte = 0L;
	private String stationName;
	private String mode;
	private String timeliness;
	private Map<String,String> additionalMetadata = new HashMap<>();
		
	public IngestionEvent() {
		super();
		setAllowedActions(Arrays.asList(AllowedAction.RESUBMIT));
	}

	public IngestionEvent(
			final ProductFamily productFamily, 
			final String productName, 
			final String relativePath, 
			final long productSizeByte,
			final String missionId,
			final String stationName,
			final String mode,
			final String timeliness
	) {
		super(productFamily, productName);
		this.productName = productName;
		this.relativePath = relativePath;
		this.productSizeByte = productSizeByte;
		this.missionId = missionId;
		this.stationName = stationName;
		this.mode = mode;
		this.timeliness = timeliness;
		setAllowedActions(Arrays.asList(AllowedAction.RESUBMIT));
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(final String productName) {
		this.productName = productName;
	}

	public String getRelativePath() {
		return relativePath;
	}

	public void setRelativePath(final String relativePath) {
		this.relativePath = relativePath;
	}
	
	public String getMissionId() {
		return missionId;
	}

	public void setMissionId(String missionId) {
		this.missionId = missionId;
	}

	public long getProductSizeByte() {
		return productSizeByte;
	}

	public void setProductSizeByte(final long productSizeByte) {
		this.productSizeByte = productSizeByte;
	}	

	public String getStationName() {
		return stationName;
	}

	public void setStationName(final String stationName) {
		this.stationName = stationName;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(final String mode) {
		this.mode = mode;
	}

	public String getTimeliness() {
		return timeliness;
	}

	public void setTimeliness(final String timeliness) {
		this.timeliness = timeliness;
	}
	
	public Map<String, String> getAdditionalMetadata() {
		return additionalMetadata;
	}

	public void setAdditionalMetadata(final Map<String, String> additionalMetadata) {
		this.additionalMetadata = additionalMetadata;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects
				.hash(additionalMetadata, mode, productName, productSizeByte, relativePath, missionId, stationName, timeliness);
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		final IngestionEvent other = (IngestionEvent) obj;
		return Objects.equals(additionalMetadata, other.additionalMetadata) && Objects.equals(mode, other.mode)
				&& Objects.equals(productName, other.productName) && productSizeByte == other.productSizeByte
				&& Objects.equals(relativePath, other.relativePath) && Objects.equals(missionId, other.missionId)
				&& Objects.equals(stationName, other.stationName) && Objects.equals(timeliness, other.timeliness);
	}

	@Override
	public String toString() {
		return "IngestionEvent [productName=" + productName + ", productFamily=" + productFamily + ", keyObjectStorage=" 
				+ keyObjectStorage + ", creationDate=" + creationDate + ", hostname=" + hostname + ", relativePath=" + 
				relativePath + ", missionId=" + missionId + ", mode=" + mode +", uid=" + uid +", productSizeByte=" + productSizeByte +
				", stationName=" + stationName +", timeliness=" + timeliness + ", additionalMetadata=" + additionalMetadata 
				+ "]";
	}
}


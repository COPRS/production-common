package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mqi.model.control.AllowedAction;

public class IngestionJob extends AbstractMessage {	
	
	/**
	 * either file:// or https://, base-URL of XBIP or inbox
	 */
	private String pickupBaseURL;

	/**
	 * type of inbox, e.g. prip, xbip, file etc.
	 */
	private String inboxType;

	/**
	 * path to file/folder relative to pickupBaseURL
	 */
	private String relativePath;
	
	/**
	 * name of file/folder to be ingested (to be used by IngestionWorker instead of keyObjectStorage
	 */
	private String productName;
	
	private long productSizeByte = 0L;
	
	private String missionId;
	
	private String stationName;
	
	private String mode;
	
	private String timeliness;
	
	private Map<String,String> additionalMetadata = new HashMap<>();
		
	public IngestionJob() {
		super();
		setAllowedActions(Arrays.asList(AllowedAction.RESTART));
	}

	public IngestionJob(
			final ProductFamily family, 
			final String productName, 
			final String pickupBaseURL, 
			final String relativePath, 	
			final long productSizeByte,
			final UUID uuid,
			final String missionId,
			final String stationName,
			final String mode,
			final String timeliness,
			final String inboxType,
			final Map<String,String> additionalMetadata
	) {
		super(family, productName);
		this.pickupBaseURL 		= pickupBaseURL;
		this.relativePath 		= relativePath;
		this.productName 		= productName;
		this.productSizeByte 	= productSizeByte;
		this.uid				= uuid;
		this.missionId          = missionId;
		this.stationName		= stationName;
		this.mode               = mode;
		this.timeliness         = timeliness;
		this.inboxType          = inboxType;
		this.additionalMetadata	= additionalMetadata;
		setAllowedActions(Arrays.asList(AllowedAction.RESTART));
	}

	public String getPickupBaseURL() {
		return pickupBaseURL;
	}

	public void setPickupBaseURL(final String pickupBaseURL) {
		this.pickupBaseURL = pickupBaseURL;
	}
	
	public String getRelativePath() {
		return relativePath;
	}

	public void setRelativePath(final String relativePath) {
		this.relativePath = relativePath;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(final String productName) {
		this.productName = productName;
	}
	
	public long getProductSizeByte() {
		return productSizeByte;
	}

	public void setProductSizeByte(final long productSizeByte) {
		this.productSizeByte = productSizeByte;
	}
	
	public String getMissionId() {
		return missionId;
	}
	
	public void setMissionId(String missionId) {
		this.missionId = missionId;
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

	public String getInboxType() {
		return inboxType;
	}

	public void setInboxType(final String inboxType) {
		this.inboxType = inboxType;
	}
	
	public Map<String, String> getAdditionalMetadata() {
		return additionalMetadata;
	}

	public void setAdditionalMetadata(final Map<String, String> additionalMetadata) {
		this.additionalMetadata = additionalMetadata;
	}

	@Override
	public final int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(
				additionalMetadata,
				inboxType,
				mode,
				pickupBaseURL,
				productName,
				productSizeByte,
				relativePath,
				missionId,
				stationName,
				timeliness
	    );
		return result;
	}

	@Override
	public final boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		final IngestionJob other = (IngestionJob) obj;
		return Objects.equals(additionalMetadata, other.additionalMetadata)
				&& Objects.equals(inboxType, other.inboxType) 
				&& Objects.equals(mode, other.mode)
				&& Objects.equals(pickupBaseURL, other.pickupBaseURL) 
				&& Objects.equals(productName, other.productName)
				&& productSizeByte == other.productSizeByte 
				&& Objects.equals(relativePath, other.relativePath)
				&& Objects.equals(missionId, other.missionId)
				&& Objects.equals(stationName, other.stationName) 
				&& Objects.equals(timeliness, other.timeliness);
	}

	@Override
	public String toString() {
		return "IngestionJob [productFamily=" + productFamily + ", keyObjectStorage=" + keyObjectStorage
				+ ", creationDate=" + creationDate + ", hostname=" + hostname + ", relativePath=" + relativePath
				+ ", pickupBaseURL=" + pickupBaseURL + ", productName=" + productName + ", uid=" + uid +
				", productSizeByte=" + productSizeByte + ", missionId=" + missionId + ", stationName=" + stationName + ", mode=" + mode +
				", timeliness=" + timeliness + ", inboxType=" + inboxType + ", additionalMetadata=" + 
				additionalMetadata + "]";
	}

}

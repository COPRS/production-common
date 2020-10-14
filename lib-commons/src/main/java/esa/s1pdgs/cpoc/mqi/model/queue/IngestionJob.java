package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Arrays;
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
	
	private String stationName;
	
	private String mode;
	
	private String timeliness;
		
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
			final String stationName,
			final String mode,
			final String timeliness,
			final String inboxType
	) {
		super(family, productName);
		this.pickupBaseURL 		= pickupBaseURL;
		this.relativePath 		= relativePath;
		this.productName 		= productName;
		this.productSizeByte 	= productSizeByte;
		this.uid				= uuid;
		this.stationName		= stationName;
		this.mode               = mode;
		this.timeliness         = timeliness;
		this.inboxType          = inboxType;
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
	
	public String getStationName() {
		return stationName;
	}

	public void setStationName(final String stationName) {
		this.stationName = stationName;
	}
	
	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getTimeliness() {
		return timeliness;
	}

	public void setTimeliness(String timeliness) {
		this.timeliness = timeliness;
	}

	public String getInboxType() {
		return inboxType;
	}

	public void setInboxType(String inboxType) {
		this.inboxType = inboxType;
	}

	@Override
	public int hashCode() {
		return Objects.hash(creationDate, hostname, keyObjectStorage, pickupBaseURL, productFamily,
				relativePath, productName, uid, productSizeByte, stationName, mode, timeliness,
				allowedActions, demandType, debug, retryCounter, inboxType);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final IngestionJob other = (IngestionJob) obj;
		return Objects.equals(creationDate, other.creationDate) 
				&& Objects.equals(hostname, other.hostname)
				&& Objects.equals(keyObjectStorage, other.keyObjectStorage)
				&& Objects.equals(pickupBaseURL, other.pickupBaseURL)
				&& productFamily == other.productFamily 
				&& Objects.equals(uid, other.uid)
				&& Objects.equals(relativePath, other.relativePath)
				&& Objects.equals(stationName, other.stationName)
				&& Objects.equals(mode, other.mode)
				&& Objects.equals(timeliness, other.timeliness)
				&& productSizeByte == other.productSizeByte 
				&& Objects.equals(productName, other.productName)
				&& Objects.equals(allowedActions, other.getAllowedActions())
		        && demandType == other.demandType
		        && debug == other.debug
		        && retryCounter == other.retryCounter
				&& Objects.equals(inboxType, other.inboxType);
	}

	@Override
	public String toString() {
		return "IngestionJob [productFamily=" + productFamily + ", keyObjectStorage=" + keyObjectStorage
				+ ", creationDate=" + creationDate + ", hostname=" + hostname + ", relativePath=" + relativePath
				+ ", pickupBaseURL=" + pickupBaseURL + ", productName=" + productName + ", uid=" + uid +
				", productSizeByte=" + productSizeByte + ", stationName=" + stationName + ", mode=" + mode +
				", timeliness=" + timeliness + ", inboxType=" + inboxType + "]";
	}	
}

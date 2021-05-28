package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Arrays;
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
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((inboxType == null) ? 0 : inboxType.hashCode());
		result = prime * result + ((mode == null) ? 0 : mode.hashCode());
		result = prime * result + ((pickupBaseURL == null) ? 0 : pickupBaseURL.hashCode());
		result = prime * result + ((productName == null) ? 0 : productName.hashCode());
		result = prime * result + (int) (productSizeByte ^ (productSizeByte >>> 32));
		result = prime * result + ((relativePath == null) ? 0 : relativePath.hashCode());
		result = prime * result + ((stationName == null) ? 0 : stationName.hashCode());
		result = prime * result + ((timeliness == null) ? 0 : timeliness.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		IngestionJob other = (IngestionJob) obj;
		if (inboxType == null) {
			if (other.inboxType != null)
				return false;
		} else if (!inboxType.equals(other.inboxType))
			return false;
		if (mode == null) {
			if (other.mode != null)
				return false;
		} else if (!mode.equals(other.mode))
			return false;
		if (pickupBaseURL == null) {
			if (other.pickupBaseURL != null)
				return false;
		} else if (!pickupBaseURL.equals(other.pickupBaseURL))
			return false;
		if (productName == null) {
			if (other.productName != null)
				return false;
		} else if (!productName.equals(other.productName))
			return false;
		if (productSizeByte != other.productSizeByte)
			return false;
		if (relativePath == null) {
			if (other.relativePath != null)
				return false;
		} else if (!relativePath.equals(other.relativePath))
			return false;
		if (stationName == null) {
			if (other.stationName != null)
				return false;
		} else if (!stationName.equals(other.stationName))
			return false;
		if (timeliness == null) {
			if (other.timeliness != null)
				return false;
		} else if (!timeliness.equals(other.timeliness))
			return false;
		return true;
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

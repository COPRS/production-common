package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Arrays;

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
	private long productSizeByte = 0L;
	private String stationName;
	private String mode;
	private String timeliness;
	
	public IngestionEvent() {
		super();
		setAllowedActions(Arrays.asList(AllowedAction.RESUBMIT));
	}

	public IngestionEvent(
			final ProductFamily productFamily, 
			final String productName, 
			final String relativePath, 
			final long productSizeByte,
			final String stationName,
			final String mode,
			final String timeliness
	) {
		super(productFamily, productName);
		this.productName = productName;
		this.relativePath = relativePath;
		this.productSizeByte = productSizeByte;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((mode == null) ? 0 : mode.hashCode());
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
		IngestionEvent other = (IngestionEvent) obj;
		if (mode == null) {
			if (other.mode != null)
				return false;
		} else if (!mode.equals(other.mode))
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
		return "IngestionEvent [productName=" + productName + ", productFamily=" + productFamily + ", keyObjectStorage=" 
				+ keyObjectStorage + ", creationDate=" + creationDate + ", hostname=" + hostname + ", relativePath=" + 
				relativePath + ", mode=" + mode +", uid=" + uid +", productSizeByte=" + productSizeByte + ", stationName=" + stationName +", timeliness=" + timeliness + "]";
	}
}


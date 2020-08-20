package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Arrays;
import java.util.Objects;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mqi.model.control.ControlAction;

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
		setAllowedControlActions(Arrays.asList(ControlAction.RESUBMIT));
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
		setAllowedControlActions(Arrays.asList(ControlAction.RESUBMIT));
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
		return Objects.hash(
				creationDate, 
				hostname, 
				keyObjectStorage, 
				productFamily, 
				productName,
				mode,
				timeliness,
				relativePath, 
				uid,
				productSizeByte,
				stationName
		);
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
		final IngestionEvent other = (IngestionEvent) obj;
		return Objects.equals(creationDate, other.creationDate) 
				&& Objects.equals(hostname, other.hostname)
				&& Objects.equals(keyObjectStorage, other.keyObjectStorage) 
				&& productFamily == other.productFamily
				&& Objects.equals(productName, other.productName)
				&& Objects.equals(stationName, other.stationName)
				&& Objects.equals(mode, other.mode)
				&& Objects.equals(timeliness, other.timeliness)
				&& Objects.equals(uid, other.uid)
				&& productSizeByte == other.productSizeByte
				&& Objects.equals(relativePath, other.relativePath);
	}
	
	

	@Override
	public String toString() {
		return "IngestionEvent [productName=" + productName + ", productFamily=" + productFamily + ", keyObjectStorage=" 
				+ keyObjectStorage + ", creationDate=" + creationDate + ", hostname=" + hostname + ", relativePath=" + 
				relativePath + ", mode=" + mode +", uid=" + uid +", productSizeByte=" + productSizeByte + ", stationName=" + stationName +", timeliness=" + timeliness + "]";
	}
}


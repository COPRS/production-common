package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Objects;

import esa.s1pdgs.cpoc.common.ProductFamily;

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
	
	public IngestionEvent() {
		super();
	}

	public IngestionEvent(
			final ProductFamily productFamily, 
			final String productName, 
			final String relativePath, 
			final long productSizeByte,
			final String stationName
	) {
		super(productFamily, productName);
		this.productName = productName;
		this.relativePath = relativePath;
		this.productSizeByte = productSizeByte;
		this.stationName = stationName;
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

	@Override
	public int hashCode() {
		return Objects.hash(
				creationDate, 
				hostname, 
				keyObjectStorage, 
				productFamily, 
				productName, 
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
				&& Objects.equals(uid, other.uid)
				&& productSizeByte == other.productSizeByte
				&& Objects.equals(relativePath, other.relativePath);
	}

	@Override
	public String toString() {
		return "IngestionEvent [productName=" + productName + ", productFamily=" + productFamily + ", keyObjectStorage=" 
				+ keyObjectStorage + ", creationDate=" + creationDate + ", hostname=" + hostname + ", relativePath=" + 
				relativePath + ", uid=" + uid +", productSizeByte=" + productSizeByte + ", stationName=" + stationName + "]";
	}
}

package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import esa.s1pdgs.cpoc.mqi.model.control.AllowedAction;

public class CatalogEvent extends AbstractMessage {	
	private String productName;
	private String productType;
	private Map<String,Object> metadata;
	
	public CatalogEvent() {
		super();
		setAllowedActions(Arrays.asList(AllowedAction.RESUBMIT));
	}
	
	public String getProductName() {
		return productName;
	}
	
	public void setProductName(final String productName) {
		this.productName = productName;
	}
	
	public String getProductType() {
		return productType;
	}
	
	public void setProductType(final String productType) {
		this.productType = productType;
	}
	
	public Map<String,Object> getMetadata() {
		return metadata;
	}
	
	public void setMetadata(final Map<String,Object> metadata) {
		this.metadata = metadata;
	}

	@Override
	public int hashCode() {
		return Objects.hash(creationDate, hostname, productName, productType,
				keyObjectStorage, metadata, productFamily, uid,
				allowedActions, demandType, debug, retryCounter);
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
		final CatalogEvent other = (CatalogEvent) obj;
		return Objects.equals(creationDate, other.creationDate) 
				&& Objects.equals(productName, other.productName)
				&& Objects.equals(hostname, other.hostname) 
				&& Objects.equals(productType, other.productType)
				&& Objects.equals(metadata, other.metadata)
				&& Objects.equals(keyObjectStorage, other.keyObjectStorage)
				&& Objects.equals(uid, other.uid)
				&& productFamily == other.productFamily
				&& Objects.equals(allowedActions, other.getAllowedActions())
		        && demandType == other.demandType
		        && debug == other.debug
		        && retryCounter == other.retryCounter;
	}

	@Override
	public String toString() {
		return "CatalogEvent [productName=" + productName + ", productType=" + productType + 
				", metadata=" + metadata + ", productFamily=" + productFamily + 
				", keyObjectStorage=" + keyObjectStorage + ", creationDate=" + creationDate + 
				", hostname=" + hostname + ", uid=" + uid + "]";
	}
}
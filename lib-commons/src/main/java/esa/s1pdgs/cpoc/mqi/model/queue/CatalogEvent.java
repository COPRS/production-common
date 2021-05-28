package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Arrays;
import java.util.Map;

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
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((metadata == null) ? 0 : metadata.hashCode());
		result = prime * result + ((productName == null) ? 0 : productName.hashCode());
		result = prime * result + ((productType == null) ? 0 : productType.hashCode());
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
		CatalogEvent other = (CatalogEvent) obj;
		if (metadata == null) {
			if (other.metadata != null)
				return false;
		} else if (!metadata.equals(other.metadata))
			return false;
		if (productName == null) {
			if (other.productName != null)
				return false;
		} else if (!productName.equals(other.productName))
			return false;
		if (productType == null) {
			if (other.productType != null)
				return false;
		} else if (!productType.equals(other.productType))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CatalogEvent [productName=" + productName + ", productType=" + productType + 
				", metadata=" + metadata + ", productFamily=" + productFamily + 
				", keyObjectStorage=" + keyObjectStorage + ", creationDate=" + creationDate + 
				", hostname=" + hostname + ", uid=" + uid + "]";
	}
}
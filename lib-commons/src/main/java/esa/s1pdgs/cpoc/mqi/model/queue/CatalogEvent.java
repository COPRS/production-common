package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;

public class CatalogEvent extends AbstractMessage {	
	private String productName;
	private String productType;
	private JsonNode metadata;
	
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
	
	public JsonNode getMetadata() {
		return metadata;
	}
	
	public void setMetadata(final JsonNode metadata) {
		this.metadata = metadata;
	}

	@Override
	public int hashCode() {
		return Objects.hash(creationDate, hostname, productName, productType,
				keyObjectStorage, metadata, productFamily);
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
				&& productFamily == other.productFamily;
	}

	@Override
	public String toString() {
		return "CatalogEvent [productName=" + productName + ", productType=" + productType + 
				", metadata=" + metadata + ", productFamily=" + productFamily + 
				", keyObjectStorage=" + keyObjectStorage + ", creationDate=" + creationDate + 
				", hostname=" + hostname + "]";
	}
}
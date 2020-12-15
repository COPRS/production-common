package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Objects;

import esa.s1pdgs.cpoc.common.ProductFamily;

public class DisseminationSource {
	
	protected ProductFamily productFamily;
	protected String keyObjectStorage;
	
	public DisseminationSource(ProductFamily productFamily, String keyObjectStorage) {
		this.productFamily = productFamily;
		this.keyObjectStorage = keyObjectStorage;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Objects.hash(keyObjectStorage, productFamily);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof DisseminationSource))
			return false;
		DisseminationSource other = (DisseminationSource) obj;
		return Objects.equals(keyObjectStorage, other.keyObjectStorage) && productFamily == other.productFamily;
	}

	@Override
	public String toString() {
		return "DisseminationSource [productFamily=" + productFamily + ", keyObjectStorage=" + keyObjectStorage
				+ "]";
	}

	public ProductFamily getProductFamily() {
		return productFamily;
	}

	public String getKeyObjectStorage() {
		return keyObjectStorage;
	}

	public void setProductFamily(ProductFamily productFamily) {
		this.productFamily = productFamily;
	}

	public void setKeyObjectStorage(String keyObjectStorage) {
		this.keyObjectStorage = keyObjectStorage;
	}
}

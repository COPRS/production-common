package esa.s1pdgs.cpoc.mqi.model.queue;

import esa.s1pdgs.cpoc.common.ProductFamily;

public class DisseminationSource {
	
	protected ProductFamily productFamily;
	protected String keyObjectStorage;
	
	public DisseminationSource() {
		super();
	}
	
	public DisseminationSource(ProductFamily productFamily, String keyObjectStorage) {
		this.productFamily = productFamily;
		this.keyObjectStorage = keyObjectStorage;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((keyObjectStorage == null) ? 0 : keyObjectStorage.hashCode());
		result = prime * result + ((productFamily == null) ? 0 : productFamily.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DisseminationSource other = (DisseminationSource) obj;
		if (keyObjectStorage == null) {
			if (other.keyObjectStorage != null)
				return false;
		} else if (!keyObjectStorage.equals(other.keyObjectStorage))
			return false;
		if (productFamily != other.productFamily)
			return false;
		return true;
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

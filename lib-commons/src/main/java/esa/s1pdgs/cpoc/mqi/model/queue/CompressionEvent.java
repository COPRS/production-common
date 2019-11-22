package esa.s1pdgs.cpoc.mqi.model.queue;

import esa.s1pdgs.cpoc.common.ProductFamily;

public class CompressionEvent extends AbstractMessage {

	private ProductFamily productFamily;
	private String keyObjectStorage;
	
	public ProductFamily getProductFamily() {
		return productFamily;
	}
	public void setProductFamily(ProductFamily productFamily) {
		this.productFamily = productFamily;
	}
	public String getKeyObjectStorage() {
		return keyObjectStorage;
	}
	public void setKeyObjectStorage(String keyObjectStorage) {
		this.keyObjectStorage = keyObjectStorage;
	}

	

}

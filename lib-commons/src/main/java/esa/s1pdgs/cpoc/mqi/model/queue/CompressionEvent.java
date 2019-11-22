package esa.s1pdgs.cpoc.mqi.model.queue;

import esa.s1pdgs.cpoc.common.ProductFamily;

public class CompressionEvent extends AbstractMessage {
	private String keyObjectStorage;
	
	public CompressionEvent(String keyObjectStoragge, ProductFamily productFamily) {
		setKeyObjectStorage(keyObjectStoragge);
		setProductFamily(productFamily);
	}
	
	public String getKeyObjectStorage() {
		return keyObjectStorage;
	}
	public void setKeyObjectStorage(String keyObjectStorage) {
		this.keyObjectStorage = keyObjectStorage;
	}

	

}

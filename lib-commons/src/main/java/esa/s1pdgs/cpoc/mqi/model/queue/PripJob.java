package esa.s1pdgs.cpoc.mqi.model.queue;

import java.time.LocalDateTime;

import esa.s1pdgs.cpoc.common.ProductFamily;

public class PripJob extends AbstractMessage {

	private ProductFamily productFamily;
	private String keyObjectStorage;
	private LocalDateTime evictionDate;

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

	public LocalDateTime getEvictionDate() {
		return evictionDate;
	}

	public void setEvictionDate(LocalDateTime evictionDate) {
		this.evictionDate = evictionDate;
	}

}

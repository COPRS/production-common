package esa.s1pdgs.cpoc.mqi.model.queue;

import esa.s1pdgs.cpoc.common.ProductFamily;

public class CompressionEvent extends AbstractMessage {	

	public CompressionEvent(ProductFamily productFamily, String keyObjectStorage) {
		super(productFamily, keyObjectStorage);
	}
}

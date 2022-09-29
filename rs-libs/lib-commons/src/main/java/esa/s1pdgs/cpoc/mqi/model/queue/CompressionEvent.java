package esa.s1pdgs.cpoc.mqi.model.queue;

import static esa.s1pdgs.cpoc.mqi.model.queue.util.CompressionEventUtil.composeCompressedKeyObjectStorage;
import static esa.s1pdgs.cpoc.mqi.model.queue.util.CompressionEventUtil.composeCompressedProductFamily;

import java.util.Arrays;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mqi.model.control.AllowedAction;

public class CompressionEvent extends AbstractMessage {

	public CompressionEvent() {
		super();
		setAllowedActions(Arrays.asList(AllowedAction.RESUBMIT));
	}

	public CompressionEvent(final ProductFamily productFamily, final String keyObjectStorage) {
		super(productFamily, keyObjectStorage);
		setAllowedActions(Arrays.asList(AllowedAction.RESUBMIT));
	}

	@Override
	public String toString() {
		return "CompressionEvent [productFamily=" + productFamily + ", keyObjectStorage=" + keyObjectStorage
				+ ", storagePath=" + storagePath + ", creationDate=" + creationDate + ", podName=" + podName + ", uid="
				+ uid + "]";
	}

	public void convertForPublishingCompressed() {
		this.setKeyObjectStorage(composeCompressedKeyObjectStorage(this.getKeyObjectStorage()));
		this.setProductFamily(composeCompressedProductFamily(this.getProductFamily()));
	}
}

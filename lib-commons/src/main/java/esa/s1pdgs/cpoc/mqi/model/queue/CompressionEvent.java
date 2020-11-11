package esa.s1pdgs.cpoc.mqi.model.queue;

import static esa.s1pdgs.cpoc.mqi.model.queue.util.CompressionEventUtil.composeCompressedKeyObjectStorage;
import static esa.s1pdgs.cpoc.mqi.model.queue.util.CompressionEventUtil.composeCompressedProductFamily;

import java.util.Arrays;
import java.util.Objects;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mqi.model.control.AllowedAction;

public class CompressionEvent extends AbstractMessage {	
	
	private CompressionDirection compressionDirection = CompressionDirection.UNDEFINED;
	
	public CompressionEvent() {
		super();
		setAllowedActions(Arrays.asList(AllowedAction.RESUBMIT));
	}
	
	public CompressionEvent(final ProductFamily productFamily, final String keyObjectStorage, final CompressionDirection compressionDirection) {
		super(productFamily, keyObjectStorage);
		this.compressionDirection = compressionDirection;
		setAllowedActions(Arrays.asList(AllowedAction.RESUBMIT));
	}
	
	public CompressionDirection getCompressionDirection() {
		return this.compressionDirection;
	}

	@Override
	public int hashCode() {
		return Objects.hash(creationDate, hostname, keyObjectStorage, productFamily, uid, compressionDirection,
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
		final CompressionEvent other = (CompressionEvent) obj;
		return Objects.equals(creationDate, other.creationDate) 
				&& Objects.equals(hostname, other.hostname)
				&& Objects.equals(keyObjectStorage, other.keyObjectStorage) 
				&& Objects.equals(uid, other.uid)
				&& productFamily == other.productFamily
		        && compressionDirection == other.compressionDirection
		        && Objects.equals(allowedActions, other.getAllowedActions())
		        && demandType == other.demandType
		        && debug == other.debug
		        && retryCounter == other.retryCounter;
	}

	@Override
	public String toString() {
		return "CompressionEvent [productFamily=" + productFamily + ", keyObjectStorage=" + keyObjectStorage
				+ ", creationDate=" + creationDate + ", hostname=" + hostname + ", uid=" + uid + ", compressionDirection="
						+ compressionDirection +"]";
	}

	public void convertForPublishingCompressed() {
		this.setKeyObjectStorage(composeCompressedKeyObjectStorage(this.getKeyObjectStorage()));
		this.setProductFamily(composeCompressedProductFamily(this.getProductFamily()));
	}
}

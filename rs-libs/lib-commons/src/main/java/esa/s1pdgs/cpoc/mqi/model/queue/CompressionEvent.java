package esa.s1pdgs.cpoc.mqi.model.queue;

import static esa.s1pdgs.cpoc.mqi.model.queue.util.CompressionEventUtil.composeCompressedKeyObjectStorage;
import static esa.s1pdgs.cpoc.mqi.model.queue.util.CompressionEventUtil.composeCompressedProductFamily;

import java.util.Arrays;

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
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((compressionDirection == null) ? 0 : compressionDirection.hashCode());
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
		CompressionEvent other = (CompressionEvent) obj;
		if (compressionDirection != other.compressionDirection)
			return false;
		return true;
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

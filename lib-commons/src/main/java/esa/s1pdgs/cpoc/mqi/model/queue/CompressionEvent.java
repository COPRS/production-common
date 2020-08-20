package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Arrays;
import java.util.Objects;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mqi.model.control.ControlAction;

public class CompressionEvent extends AbstractMessage {	
	
	private CompressionDirection compressionDirection = CompressionDirection.UNDEFINED;
	
	public CompressionEvent() {
		super();
		setAllowedControlActions(Arrays.asList(ControlAction.RESUBMIT));
	}
	
	public CompressionEvent(final ProductFamily productFamily, final String keyObjectStorage, final CompressionDirection compressionDirection) {
		super(productFamily, keyObjectStorage);
		this.compressionDirection = compressionDirection;
		setAllowedControlActions(Arrays.asList(ControlAction.RESUBMIT));
	}

	@Override
	public int hashCode() {
		return Objects.hash(creationDate, hostname, keyObjectStorage, productFamily, uid, compressionDirection);
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
		        && compressionDirection == other.compressionDirection;
	}

	@Override
	public String toString() {
		return "CompressionEvent [productFamily=" + productFamily + ", keyObjectStorage=" + keyObjectStorage
				+ ", creationDate=" + creationDate + ", hostname=" + hostname + ", uid=" + uid + ", compressionDirection="
						+ compressionDirection +"]";
	}
}

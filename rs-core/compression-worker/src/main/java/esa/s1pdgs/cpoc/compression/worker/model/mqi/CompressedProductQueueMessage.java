package esa.s1pdgs.cpoc.compression.worker.model.mqi;

import java.util.Objects;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionDirection;

public class CompressedProductQueueMessage extends QueueMessage {
    
	private String objectStorageKey;
	
	private CompressionDirection compressionDirection = CompressionDirection.UNDEFINED;
    
	public CompressedProductQueueMessage(ProductFamily family, String productName, String objectStorageKey, CompressionDirection compressionDirection) {
		super(family, productName);
		this.objectStorageKey = objectStorageKey;
		this.compressionDirection = compressionDirection;
	}

	public String getObjectStorageKey() {
		return objectStorageKey;
	}

	public void setObjectStorageKey(String objectStorageKey) {
		this.objectStorageKey = objectStorageKey;
	}
	
    public CompressionDirection getCompressionDirection() {
		return compressionDirection;
	}

	public void setCompressionDirection(CompressionDirection compressionDirection) {
		this.compressionDirection = compressionDirection;
	}

	/**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String superStr = super.toStringForExtendedClasses();
        return String.format("{%s, keyObs: %s, compressionDirection: %s}", superStr,
                objectStorageKey, compressionDirection);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int superHash = super.hashCode();
        return Objects.hash(superHash, objectStorageKey, compressionDirection);
    }

    /**
     * @see java.lang.Object#equals()
     */
    @Override
    public boolean equals(final Object obj) {
        boolean ret;
        if (this == obj) {
            ret = true;
        } else if (obj == null || getClass() != obj.getClass()) {
            ret = false;
        } else {
        	CompressedProductQueueMessage other = (CompressedProductQueueMessage) obj;
            // field comparison
            ret = super.equals(other) && Objects.equals(objectStorageKey, other.objectStorageKey) && compressionDirection == other.compressionDirection;
        }
        return ret;
    }

}

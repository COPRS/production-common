package esa.s1pdgs.cpoc.compression.model.mqi;

import java.util.Objects;

import esa.s1pdgs.cpoc.common.ProductFamily;

public class CompressedProductQueueMessage extends QueueMessage {
    
	private String objectStorageKey;;
    
	public CompressedProductQueueMessage(ProductFamily family, String productName, String objectStorageKey) {
		super(family, productName);
		setObjectStorageKey(objectStorageKey);
	}

	public String getObjectStorageKey() {
		return objectStorageKey;
	}

	public void setObjectStorageKey(String objectStorageKey) {
		this.objectStorageKey = objectStorageKey;
	}
	
    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String superStr = super.toStringForExtendedClasses();
        return String.format("{%s, keyObs: %s}", superStr,
                objectStorageKey);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int superHash = super.hashCode();
        return Objects.hash(superHash, objectStorageKey);
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
            ret = super.equals(other) && Objects.equals(objectStorageKey, other.objectStorageKey);
        }
        return ret;
    }

}

package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Objects;

import esa.s1pdgs.cpoc.common.ProductFamily;

public class CompressionJobDto {

	private String productName;
	private ProductFamily family = ProductFamily.AUXILIARY_FILE;
	private String objectStorageKey;

    /**
     * Default constructor
     */
    public CompressionJobDto() {
        super();
    }
    
	public CompressionJobDto(final String productName, final ProductFamily family, final String objectStorageKey) {
		this.productName = productName;
		this.family = family;
		this.objectStorageKey = objectStorageKey;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public ProductFamily getFamily() {
		return family;
	}

	public void setFamily(ProductFamily family) {
		this.family = family;
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
        return String.format(
                "{objectStorageKey: %s, productName: %s, family: %s}",
                objectStorageKey, productName, family);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(objectStorageKey, productName, family);
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
        	CompressionJobDto other = (CompressionJobDto) obj;
            // field comparison
            ret = Objects.equals(objectStorageKey, other.objectStorageKey)
                    && productName == other.productName
                    && Objects.equals(family, other.family);
        }
        return ret;
    }

}

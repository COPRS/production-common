package esa.s1pdgs.cpoc.mdcatalog.rest.dto;

/**
 * Class describing the metadata of a file
 * 
 * @author Viveris Technologies
 *
 */
public class SearchMetadataDto extends MetadataDto {
    
	public SearchMetadataDto(String productName, String productType, String keyObjectStorage,
			String validityStart, String validityStop) {
		super(productName, productType, keyObjectStorage, validityStart, validityStop);
	}

	public SearchMetadataDto(SearchMetadataDto obj) {
		this(obj.getProductName(), obj.getProductType(), obj.getKeyObjectStorage(), obj.getValidityStart(),
				obj.getValidityStop());
	}

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "SearchMetadataDto [productName= " + productName + ", productType= "
                + productType + ", keyObjectStorage= " + keyObjectStorage
                + ", validityStart= " + validityStart + ", validityStop= "
                + validityStop + "]";
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        return super.equals(obj);
    }
	
}

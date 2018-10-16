package esa.s1pdgs.cpoc.mdcatalog.rest.dto;

/**
 * Class describing the metadata of a file
 * 
 * @author Viveris Technologies
 *
 */
public class SearchMetadataDto extends MetadataDto {

    private String polarisation;
    
    private String productConsolidation;
    
	public SearchMetadataDto(String productName, String productType, String keyObjectStorage,
			String validityStart, String validityStop) {
		super(productName, productType, keyObjectStorage, validityStart, validityStop);
	}

	public SearchMetadataDto(SearchMetadataDto obj) {
		this(obj.getProductName(), obj.getProductType(), obj.getKeyObjectStorage(), obj.getValidityStart(),
				obj.getValidityStop());
		this.polarisation = obj.getPolarisation();
		this.productConsolidation = obj.getProductConsolidation();
	}
	
	public SearchMetadataDto(String productName, String productType, String keyObjectStorage,
            String validityStart, String validityStop, String polarisation, String productConsolidation) {
        super(productName, productType, keyObjectStorage, validityStart, validityStop);
        this.polarisation = polarisation;
        this.productConsolidation = productConsolidation;
    }

    /**
     * @return the polarisation
     */
    public String getPolarisation() {
        return polarisation;
    }

    /**
     * @param polarisation the polarisation to set
     */
    public void setPolarisation(String polarisation) {
        this.polarisation = polarisation;
    }

    /**
     * @return the productConsolidation
     */
    public String getProductConsolidation() {
        return productConsolidation;
    }

    /**
     * @param productConsolidation the productConsolidation to set
     */
    public void setProductConsolidation(String productConsolidation) {
        this.productConsolidation = productConsolidation;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "SearchMetadataDto [polarisation= " + polarisation
                + ", productConsolidation= " + productConsolidation
                + ", productName= " + productName + ", productType= "
                + productType + ", keyObjectStorage= " + keyObjectStorage
                + ", validityStart= " + validityStart + ", validityStop= "
                + validityStop + "]";
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + ((polarisation == null) ? 0 : polarisation.hashCode());
        result = prime * result + ((productConsolidation == null) ? 0
                : productConsolidation.hashCode());
        return result;
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
        SearchMetadataDto other = (SearchMetadataDto) obj;
        if (polarisation == null) {
            if (other.polarisation != null)
                return false;
        } else if (!polarisation.equals(other.polarisation))
            return false;
        if (productConsolidation == null) {
            if (other.productConsolidation != null)
                return false;
        } else if (!productConsolidation.equals(other.productConsolidation))
            return false;
        return true;
    }
	
}

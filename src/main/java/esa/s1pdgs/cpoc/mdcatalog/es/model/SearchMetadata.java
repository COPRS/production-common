package esa.s1pdgs.cpoc.mdcatalog.es.model;

/**
 * Object containing the metadata from ES
 *
 * @author Viveris Technologies
 */
public class SearchMetadata extends AbstractMetadata {

    private String polarisation;
    
    private String productConsolidation;
        
	public SearchMetadata() {
		super();
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
        return "SearchMetadata [polarisation= " + polarisation
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
        SearchMetadata other = (SearchMetadata) obj;
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

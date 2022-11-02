package esa.s1pdgs.cpoc.metadata.client;

import java.util.Objects;

import esa.s1pdgs.cpoc.common.ProductFamily;

/**
 * Class describing a search query for metadata
 * 
 * @author Cyrielle Gailliard
 */
public class SearchMetadataQuery {

    /**
     * Identify the query
     */
    private int identifier;

    /**
     * Retrieval mode
     */
    private String retrievalMode;

    /**
     * Delta time to apply to the validity start time
     */
    private double deltaTime0;

    /**
     * Delta time to apply to the validity stop time
     */
    private double deltaTime1;
    
    private int minResults;
    private int maxResults;

    /**
     * Wanted product type
     */
    private String productType;

    /**
     * Product Family
     */
    private ProductFamily productFamily;

    /**
     * Default constructor
     */
    public SearchMetadataQuery() {
        super();
    }

    /**
     * Constructor using fields
     * 
     * @param identifier
     * @param retrievalMode
     * @param deltaTime0
     * @param deltaTime1
     * @param fileType
     */
    public SearchMetadataQuery(final int identifier, final String retrievalMode,
            final double deltaTime0, final double deltaTime1,
            final int minResults, final int maxResults,
            final String productType, final ProductFamily productFamily) {
        this(identifier, retrievalMode, deltaTime0, deltaTime1, productType, productFamily);
        this.minResults = minResults;
        this.maxResults = maxResults;
    }
    
    public SearchMetadataQuery(final int identifier, final String retrievalMode,
            final double deltaTime0, final double deltaTime1,
            final String productType, final ProductFamily productFamily) {
        this();
        this.identifier = identifier;
        this.retrievalMode = retrievalMode;
        this.deltaTime0 = deltaTime0;
        this.deltaTime1 = deltaTime1;
        this.productType = productType;
        this.productFamily = productFamily;
    }

    /**
     * Clone
     * 
     * @param obj
     */
    public SearchMetadataQuery(final SearchMetadataQuery obj) {
        this(obj.getIdentifier(), obj.getRetrievalMode(), 
        		obj.getDeltaTime0(), obj.getDeltaTime1(), 
        		obj.getMinResults(), obj.getMaxResults(),
        		obj.getProductType(),
                obj.getProductFamily());
    }

    /**
     * @return the identifier
     */
    public int getIdentifier() {
        return identifier;
    }

    /**
     * @param identifier
     *            the identifier to set
     */
    public void setIdentifier(final int identifier) {
        this.identifier = identifier;
    }

    /**
     * @return the retrievalMode
     */
    public String getRetrievalMode() {
        return retrievalMode;
    }

    /**
     * @param retrievalMode
     *            the retrievalMode to set
     */
    public void setRetrievalMode(final String retrievalMode) {
        this.retrievalMode = retrievalMode;
    }

    /**
     * @return the deltaTime0
     */
    public double getDeltaTime0() {
        return deltaTime0;
    }

    /**
     * @param deltaTime0
     *            the deltaTime0 to set
     */
    public void setDeltaTime0(final double deltaTime0) {
        this.deltaTime0 = deltaTime0;
    }

    /**
     * @return the deltaTime1
     */
    public double getDeltaTime1() {
        return deltaTime1;
    }

    /**
     * @param deltaTime1
     *            the deltaTime1 to set
     */
    public void setDeltaTime1(final double deltaTime1) {
        this.deltaTime1 = deltaTime1;
    }
    
    public int getMinResults() {
		return minResults;
	}

	public void setMinResults(int minResults) {
		this.minResults = minResults;
	}

	public int getMaxResults() {
		return maxResults;
	}

	public void setMaxResults(int maxResults) {
		this.maxResults = maxResults;
	}

	/**
     * @return the productType
     */
    public String getProductType() {
        return productType;
    }

    /**
     * @param productType
     *            the productType to set
     */
    public void setProductType(final String productType) {
        this.productType = productType;
    }

    /**
     * @return the productFamily
     */
    public ProductFamily getProductFamily() {
        return productFamily;
    }

    /**
     * @param productFamily
     *            the productFamily to set
     */
    public void setProductFamily(ProductFamily productFamily) {
        this.productFamily = productFamily;
    }

    public String toJsonString() {
        return String.format(
                "{identifier: %s, retrievalMode: %s, deltaTime0: %s, deltaTime1: %s, productType: %s, productFamily: %s}",
                identifier, retrievalMode, deltaTime0, deltaTime1, productType,
                productFamily);
    }

    /**
     * @return
     */
    public String toLogMessage() {
        return identifier + "|" + retrievalMode + "|" + deltaTime0 + "|"
                + deltaTime1 + "|" + productType + "|" + productFamily;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(identifier, retrievalMode, deltaTime0, deltaTime1,
                productType, productFamily);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        boolean ret;
        if (this == obj) {
            ret = true;
        } else if (obj == null || getClass() != obj.getClass()) {
            ret = false;
        } else {
            SearchMetadataQuery other = (SearchMetadataQuery) obj;
            ret = identifier == other.identifier
                    && Objects.equals(retrievalMode, other.retrievalMode)
                    && Objects.equals(deltaTime0, other.deltaTime0)
                    && Objects.equals(deltaTime1, other.deltaTime1)
                    && Objects.equals(productType, other.productType)
                    && Objects.equals(productFamily, other.productFamily);
        }
        return ret;
    }

	@Override
	public String toString() {
		return "SearchMetadataQuery [identifier=" + identifier + ", retrievalMode=" + retrievalMode + ", deltaTime0="
				+ deltaTime0 + ", deltaTime1=" + deltaTime1 + ", productType=" + productType + ", productFamily="
				+ productFamily + "]";
	}
    
    
}

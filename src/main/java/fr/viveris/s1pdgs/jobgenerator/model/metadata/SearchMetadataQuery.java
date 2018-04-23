package fr.viveris.s1pdgs.jobgenerator.model.metadata;

/**
 * Class describing a search query for metadata
 * 
 * @author Cyrielle Gailliard
 *
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

	/**
	 * Wanted product type
	 */
	private String productType;

	/**
	 * Default constructor
	 */
	public SearchMetadataQuery() {

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
	public SearchMetadataQuery(int identifier, String retrievalMode, double deltaTime0, double deltaTime1, String productType) {
		this();
		this.identifier = identifier;
		this.retrievalMode = retrievalMode;
		this.deltaTime0 = deltaTime0;
		this.deltaTime1 = deltaTime1;
		this.productType = productType;
	}

	/**
	 * Clone
	 * 
	 * @param obj
	 */
	public SearchMetadataQuery(SearchMetadataQuery obj) {
		this(obj.getIdentifier(), obj.getRetrievalMode(), obj.getDeltaTime0(), obj.getDeltaTime1(),
				obj.getProductType());
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
	public void setIdentifier(int identifier) {
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
	public void setRetrievalMode(String retrievalMode) {
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
	public void setDeltaTime0(double deltaTime0) {
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
	public void setDeltaTime1(double deltaTime1) {
		this.deltaTime1 = deltaTime1;
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
	public void setProductType(String productType) {
		this.productType = productType;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SearchMetadataQuery [identifier=" + identifier + ", retrievalMode=" + retrievalMode + ", deltaTime0="
				+ deltaTime0 + ", deltaTime1=" + deltaTime1 + ", productType=" + productType + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + identifier;
		result = prime * result + ((productType == null) ? 0 : productType.hashCode());
		result = prime * result + ((retrievalMode == null) ? 0 : retrievalMode.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SearchMetadataQuery other = (SearchMetadataQuery) obj;
		if (deltaTime0 != other.deltaTime0)
			return false;
		if (deltaTime1 != other.deltaTime1)
			return false;
		if (identifier != other.identifier)
			return false;
		if (productType == null) {
			if (other.productType != null)
				return false;
		} else if (!productType.equals(other.productType))
			return false;
		if (retrievalMode == null) {
			if (other.retrievalMode != null)
				return false;
		} else if (!retrievalMode.equals(other.retrievalMode))
			return false;
		return true;
	}

}

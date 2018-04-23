package fr.viveris.s1pdgs.jobgenerator.model.metadata;

/**
 * Class describing the metdata of a file
 * 
 * @author Cyrielle Gailliard
 *
 */
public class SearchMetadata extends AbstractMetadata {

	/**
	 * Constrcutor using fields
	 * 
	 * @param productName
	 * @param productType
	 * @param keyObjectStorage
	 * @param validityStart
	 * @param validityStop
	 */
	public SearchMetadata(String productName, String productType, String keyObjectStorage, String validityStart,
			String validityStop) {
		super(productName, productType, keyObjectStorage, validityStart, validityStop);
	}

	/**
	 * Clone
	 * 
	 * @param obj
	 */
	public SearchMetadata(SearchMetadata obj) {
		super(obj);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SearchMetadata [productName=" + productName + ", productType=" + productType + ", keyObjectStorage="
				+ keyObjectStorage + ", validityStart=" + validityStart + ", validityStop=" + validityStop + "]";
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}
}

package esa.s1pdgs.cpoc.jobgenerator.model.metadata;

/**
 * Class describing the metdata of a file
 * 
 * @author Cyrielle Gailliard
 *
 */
public class SearchMetadata extends AbstractMetadata {

	/**
	 * Constructor using fields
	 * 
	 * @param productName
	 * @param productType
	 * @param keyObjectStorage
	 * @param validityStart
	 * @param validityStop
	 */
	public SearchMetadata(final String productName, final String productType, final String keyObjectStorage,
			final String validityStart, final String validityStop) {
		super(productName, productType, keyObjectStorage, validityStart, validityStop);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return super.toString();
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return super.hashCode();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		return super.equals(obj);
	}
}

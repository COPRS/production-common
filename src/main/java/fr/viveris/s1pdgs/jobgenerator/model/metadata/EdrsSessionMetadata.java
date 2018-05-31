package fr.viveris.s1pdgs.jobgenerator.model.metadata;

/**
 * 
 * @author Cyrielle Gailliard
 *
 */
public class EdrsSessionMetadata extends AbstractMetadata {

	/**
	 * @param productName
	 * @param productType
	 * @param keyObjectStorage
	 * @param validityStart
	 * @param validityStop
	 */
	public EdrsSessionMetadata(final String productName, final String productType, final String keyObjectStorage,
			final String validityStart, final String validityStop) {
		super(productName, productType, keyObjectStorage, validityStart, validityStop);
	}

}

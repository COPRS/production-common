package fr.viveris.s1pdgs.jobgenerator.model.metadata;

public class EdrsSessionMetadata extends AbstractMetadata {

	public EdrsSessionMetadata() {
		super();
	}

	/**
	 * @param productName
	 * @param productType
	 * @param keyObjectStorage
	 * @param validityStart
	 * @param validityStop
	 */
	public EdrsSessionMetadata(String productName, String productType, String keyObjectStorage, String validityStart,
			String validityStop) {
		super(productName, productType, keyObjectStorage, validityStart, validityStop);
	}

}

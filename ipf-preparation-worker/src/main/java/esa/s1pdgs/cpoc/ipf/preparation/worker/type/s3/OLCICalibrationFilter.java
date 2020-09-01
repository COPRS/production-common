package esa.s1pdgs.cpoc.ipf.preparation.worker.type.s3;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.ElementMapper;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;

public class OLCICalibrationFilter {

	private static final int PRODUCT_TYPE_BEGIN_INDEX = 4;
	private static final int PRODUCT_TYPE_END_INDEX = 15;

	private final MetadataClient metadataClient;
	private final ElementMapper elementMapper;

	public OLCICalibrationFilter(final MetadataClient metadataClient, final ElementMapper elementMapper) {
		this.metadataClient = metadataClient;
		this.elementMapper = elementMapper;
	}

	/**
	 * Checks if the L1Triggering flag on the metadata is not the same as the last 3
	 * characters of the processor
	 * 
	 * @param productName product name of the main input product
	 * @return true if job should be discarded
	 * @throws MetadataQueryException on error in query execution
	 */
	public boolean checkIfJobShouldBeDiscarded(final String productName, final String processorName)
			throws MetadataQueryException {
		ProductFamily productFamily = extractProductFamilyFromProductName(productName);
		String response = this.metadataClient.getL1TriggeringForProductName(productFamily, productName);

		// Discard job, if response doesn't match processor name
		return !response.equals(processorName.substring(processorName.length() - 3));
	}

	/**
	 * Extracts the product family from the product name.
	 */
	private ProductFamily extractProductFamilyFromProductName(final String productName) {
		String productType = productName.substring(PRODUCT_TYPE_BEGIN_INDEX, PRODUCT_TYPE_END_INDEX);

		return elementMapper.inputFamilyOf(productType);
	}

}

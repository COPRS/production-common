/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package esa.s1pdgs.cpoc.preparation.worker.type.s3;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.preparation.worker.tasktable.adapter.ElementMapper;

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
		this.metadataClient.refreshIndex(productFamily,
				productName.substring(PRODUCT_TYPE_BEGIN_INDEX, PRODUCT_TYPE_END_INDEX));
		String response = this.metadataClient.performWithReindexOnNull(
				() -> this.metadataClient.getL1TriggeringForProductName(productFamily, productName),
				productName.substring(PRODUCT_TYPE_BEGIN_INDEX, PRODUCT_TYPE_END_INDEX), productFamily);

		// Discard job, if response doesn't match processor name
		return response == null || !response.equals(processorName.substring(processorName.length() - 3));
	}

	/**
	 * Extracts the product family from the product name.
	 */
	private ProductFamily extractProductFamilyFromProductName(final String productName) {
		String productType = productName.substring(PRODUCT_TYPE_BEGIN_INDEX, PRODUCT_TYPE_END_INDEX);

		return elementMapper.inputFamilyOf(productType);
	}

}

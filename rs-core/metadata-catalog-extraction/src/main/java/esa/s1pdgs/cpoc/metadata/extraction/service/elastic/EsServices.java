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

package esa.s1pdgs.cpoc.metadata.extraction.service.elastic;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.WriteRequest.RefreshPolicy;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataCreationException;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.common.utils.Retries;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.ProductMetadata;

/**
 * Service for accessing to elasticsearch data
 * 
 * @author Cyrielle
 *
 */
@Service
public class EsServices {

	static final String REQUIRED_SATELLITE_ID_PATTERN = "(aux_.*)";

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(EsServices.class);

	/**
	 * Elasticsearch client
	 */
	private final ElasticsearchDAO elasticsearchDAO;
	
	@Autowired
	public EsServices(final ElasticsearchDAO elasticsearchDAO) {
		this.elasticsearchDAO = elasticsearchDAO;
	}

	/**
	 * Check if a given metadata already exist
	 * 
	 */
	public boolean isMetadataExist(final ProductMetadata product) throws Exception {
		try {
			final String productType;
			if (ProductFamily.AUXILIARY_FILE.equals(ProductFamily.valueOf(product.getString("productFamily")))
					|| ProductFamily.EDRS_SESSION.equals(ProductFamily.valueOf(product.getString("productFamily")))) {
				productType = product.getString("productType").toLowerCase();
			} else {
				productType = product.getString("productFamily").toLowerCase();
			}
			final String productName = product.getString("productName");

			final GetRequest getRequest = new GetRequest(productType, productName);

			final GetResponse response = elasticsearchDAO.get(getRequest);

			LOGGER.debug("Product {} response from ES {}", productName, response);

			return response.isExists();
		} catch (final IOException e) {
			throw new Exception(e.getMessage());
		}
	}

	public String createMetadataWithRetries(final ProductMetadata product, final String productName, final int numRetries,
			final long retrySleep) throws InterruptedException {
		return Retries.performWithRetries(() -> {
			if (!isMetadataExist(product)) {
				LOGGER.debug("Creating metadata in ES for product {}", productName);
				return createMetadata(product);
			} else {
				LOGGER.debug("ES already contains metadata for product {}", productName);
			}
			return "";
		}, "Create metadata " + product, numRetries, retrySleep);
	}

	/**
	 * Save the metadata in elastic search. The metadata data is created in the
	 * index named [productType] with id [productName]
	 * 
	 */
	String createMetadata(final ProductMetadata product) throws Exception {
		
		String warningMessage = "";
		
		try {
			final String productType;
			final ProductFamily family = ProductFamily.valueOf(product.getString("productFamily"));

			if (ProductFamily.AUXILIARY_FILE.equals(family) || ProductFamily.EDRS_SESSION.equals(family)) {
				productType = product.getString("productType").toLowerCase();
			} else {
				productType = product.getString("productFamily").toLowerCase();
			}
			final String productName = product.getString("productName");

			IndexRequest request = new IndexRequest(productType).id(productName).source(product.toString(),
					XContentType.JSON).setRefreshPolicy(RefreshPolicy.WAIT_UNTIL);

			IndexResponse response;
			try {
				response = elasticsearchDAO.index(request);
			} catch (final ElasticsearchStatusException e) {
				/*
				 * S1PRO-783: This is a temporary work around for the WV footprint issue that
				 * occurs for WV products when the footprint does cross the date line border. As
				 * it is currently not possible to submit these kind of products, we are not
				 * failing immediately, but trying to resubmit it without a footprint.
				 * 
				 * This is a workaround and will be obsoleted by S1PRO-778. Due to no defined
				 * pattern, we have to parse the exception to identify possible footprint
				 * issues.
				 */
				LOGGER.warn("An exception occurred while accessing the elastic search index: {}", LogUtils.toString(e));
				final String result = e.getMessage();
				boolean fixed = false;
				if (result.contains("failed to parse field [sliceCoordinates] of type [geo_shape]")) {
					warningMessage = "Parsing error occurred for sliceCoordinates, dropping them as workaround for #S1PRO-783";
					LOGGER.warn(warningMessage);
					product.remove("sliceCoordinates");
					fixed = true;
				}
	
				if (result.contains("failed to parse field [segmentCoordinates] of type [geo_shape]")) {
					warningMessage = "Parsing error occurred for segmentCoordinates, dropping them as workaround for #S1PRO-783";
					LOGGER.warn(warningMessage);
					product.remove("segmentCoordinates");
					fixed = true;
				}
				
				/*
				 * RS-1002: There are some situations where the footprint raises a topology exception in ES and breaking the workflow.
				 * It was decided to catch this kind of exceptions as well and remove the footprint as a WA
				 */
				if (e.getDetailedMessage().contains("found non-noded intersection between LINESTRING")) {
					warningMessage = "Parsing error occurred and identified as non-noded intersection between LINESTRING, dropping them as workaround for #RS-1002";
					LOGGER.warn(warningMessage);
					product.remove("sliceCoordinates");
					fixed = true;
				}
				
				// S3 L0 products seem to have broken footprints. If a self intersecting error
				// occurs, remove the sliceCoordinates and try again. Do this for PUG products 
				// as well, as they can be based on S3_L0 products
				if ((family == ProductFamily.S3_L0  || family == ProductFamily.S3_PUG) && e.getDetailedMessage() != null) {					
					if (e.getDetailedMessage().contains("Self-intersection at or near point")) {
						warningMessage = "Invalid self-intersecting footprint detected, dropping it as a workaround for #RS-436";
						LOGGER.warn(warningMessage);
						product.remove("sliceCoordinates");
						fixed = true;
					} else if (e.getDetailedMessage().contains("Cannot determine orientation: signed area equal to 0")) {
						// DO_0_NAV products seems to have a footprint with multiple points in the same spot. Having
						// a footprint without any area is not a valid 
						warningMessage = "Invalid footprint without an area detected, dropping it as a workaround for #RS-986";
						LOGGER.warn(warningMessage);
						product.remove("sliceCoordinates");
						fixed = true;
					}
				}

				if (!fixed) {
					throw e;
				}

				LOGGER.debug("Content of JSON second attempt: {}", product.toString());

				request = new IndexRequest(productType).id(productName).source(product.toString(), XContentType.JSON).setRefreshPolicy(RefreshPolicy.WAIT_UNTIL);
				response = elasticsearchDAO.index(request);
				// END OF WORKAROUND S1PRO-783
			}

			if (response.status() != RestStatus.CREATED) {
				// If it still fails, we cannot fix it. Raise exception
				if (response.status() != RestStatus.CREATED) {
					throw new MetadataCreationException(productName, response.status().toString(),
							response.getResult().toString());
				}

			}
		} catch (IOException e) {
			throw new Exception(e);
		}
		return warningMessage;
	}
	
}

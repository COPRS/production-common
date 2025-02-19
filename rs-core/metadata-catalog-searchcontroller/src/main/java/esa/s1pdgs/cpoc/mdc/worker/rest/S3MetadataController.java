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

package esa.s1pdgs.cpoc.mdc.worker.rest;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.mdc.worker.service.EsServices;
import esa.s1pdgs.cpoc.metadata.model.S3Metadata;

@RestController
@RequestMapping(path = "/s3metadata")
public class S3MetadataController extends AbstractMetadataController<S3Metadata> {

	private static final Logger LOGGER = LogManager.getLogger(S3MetadataController.class);

	@Autowired
	public S3MetadataController(EsServices esServices) {
		super(S3Metadata.class, esServices);
	}

	/**
	 * Queries the elastic search for products matching the given parameters. Query
	 * build is based on the marginTT workflow extension.
	 * 
	 * @return list of matching products
	 */
	@RequestMapping(path = "/{productType}/range", method = RequestMethod.GET)
	public ResponseEntity<List<S3Metadata>> getProductsInRange(@PathVariable(name = "productType") String productType,
			@RequestParam(name = "productFamily") final String productFamily,
			@RequestParam(name = "satellite") final String satellite,
			@RequestParam(name = "start") final String rangeStart,
			@RequestParam(name = "stop") final String rangeStop) {

		try {
			List<S3Metadata> response = new ArrayList<>();

			LOGGER.info("Received S3 MarginTT search query for family '{}', product type '{}'",
					productFamily.toString(), productType);

			List<S3Metadata> result = esServices.rangeCoverQuery(rangeStart, rangeStop, productType, satellite,
					ProductFamily.fromValue(productFamily));

			if (result != null) {
				LOGGER.debug("Query returned {} results", result.size());

				for (S3Metadata s : result) {
					response.add(s);
				}
			}

			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (final AbstractCodedException e) {
			LOGGER.error("Error on performing marginTT search for product type {}: [code {}] {}", productType,
					e.getCode().getCode(), e.getLogMessage());
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		} catch (final Exception e) {
			LOGGER.error("Error on performing marginTT for product type {}: {}", productType, LogUtils.toString(e));
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Queries the elastic search for the first product (insertionTime) of a given
	 * orbit.
	 */
	@RequestMapping(path = "/{productType}/orbit", method = RequestMethod.GET)
	public ResponseEntity<S3Metadata> getFirstProductForOrbit(@PathVariable(name = "productType") String productType,
			@RequestParam(name = "productFamily") final String productFamily,
			@RequestParam(name = "satellite") final String satellite,
			@RequestParam(name = "orbitNumber") final long orbitNumber) {

		try {
			LOGGER.info("Received Orbit search query for family '{}', product type '{}', orbitNumber '{}'",
					productFamily.toString(), productType, orbitNumber);

			S3Metadata result = esServices.getFirstProductForOrbit(ProductFamily.fromValue(productFamily), productType,
					satellite, orbitNumber);

			return new ResponseEntity<>(result, HttpStatus.OK);
		} catch (final AbstractCodedException e) {
			LOGGER.error("Error on performing Orbit search for product type {} and orbit {}: [code {}] {}", productType,
					orbitNumber, e.getCode().getCode(), e.getLogMessage());
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		} catch (final Exception e) {
			LOGGER.error("Error on performing Orbit search for product type {} and orbit {}: {}", productType,
					orbitNumber, LogUtils.toString(e));
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Retrieve the L1Triggering information for the given productName from the
	 * elasticsearch
	 * 
	 * @param productFamily product family of the product, used to determine index
	 * @param productName   product name, which L1Triggering should be extracted
	 * @return L1Triggering, "NONE" as default
	 */
	@RequestMapping(path = "/l1triggering", method = RequestMethod.GET)
	public ResponseEntity<String> getL1Triggering(@RequestParam(name = "productFamily") final String productFamily,
			@RequestParam(name = "productName") final String productName) {
		try {
			LOGGER.info("Received L1Triggering query for productFamily '{}' and productName '{}'",
					productFamily.toString(), productName);

			String response = esServices.getL1Triggering(ProductFamily.fromValue(productFamily), productName);

			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (final AbstractCodedException e) {
			LOGGER.error("Error on performing L1Triggering search for product name {}: [code {}] {}", productName,
					e.getCode().getCode(), e.getLogMessage());
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		} catch (final Exception e) {
			LOGGER.error("Error on performing L1Triggering search for product name {}: {}", productName,
					LogUtils.toString(e));
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Refresh the index determined by product family and type, to ensure new
	 * documents are searchable
	 * 
	 * @param productFamily product family to determine index
	 * @param productType   product type to determine index
	 * @return Empty Response
	 */
	@RequestMapping(path = "/refreshIndex/{productFamily}", method = RequestMethod.GET)
	public ResponseEntity<String> refreshIndex(@PathVariable(name = "productFamily") final String productFamily,
			@RequestParam(name = "productType") final String productType) {
		try {
			LOGGER.info("Received refresh message for productFamily '{}' and productType '{}'",
					productFamily.toString(), productType);

			esServices.refreshIndex(ProductFamily.fromValue(productFamily), productType);

			return ResponseEntity.ok().build();
		} catch (final AbstractCodedException e) {
			LOGGER.error("Error on refreshing index for productFamily {} and productType {}: [code {}] {}",
					productFamily, productType, e.getCode().getCode(), e.getLogMessage());
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		} catch (final Exception e) {
			LOGGER.error("Error on refreshing index for productFamily {} and productType {}: {}", productFamily,
					productType, LogUtils.toString(e));
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Retrieve the metadata for a given productName
	 * 
	 * @param productFamily product family of the product, used to determine index
	 * @param productName   product name, which the metadata should be extracted
	 * @return S3Metadata-Object
	 */
	@RequestMapping(path = "/{productFamily}", method = RequestMethod.GET)
	public ResponseEntity<S3Metadata> getMetadataForProduct(
			@PathVariable(name = "productFamily") final String productFamily,
			@RequestParam(name = "productName") final String productName) {
		try {
			LOGGER.info("Received S3Metadata query for productFamily '{}' and productName '{}'",
					productFamily.toString(), productName);

			S3Metadata response = esServices.getS3ProductMetadata(ProductFamily.fromValue(productFamily), productName);

			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (final AbstractCodedException e) {
			LOGGER.error("Error on performing S3Metadata search for product name {}: [code {}] {}", productName,
					e.getCode().getCode(), e.getLogMessage());
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		} catch (final Exception e) {
			LOGGER.error("Error on performing S3Metadata search for product name {}: {}", productName,
					LogUtils.toString(e));
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}

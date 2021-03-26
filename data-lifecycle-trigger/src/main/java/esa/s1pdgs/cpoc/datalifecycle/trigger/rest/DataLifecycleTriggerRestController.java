package esa.s1pdgs.cpoc.datalifecycle.trigger.rest;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import esa.s1pdgs.cpoc.common.utils.StringUtil;
import esa.s1pdgs.cpoc.datalifecycle.trigger.rest.model.Product;
import esa.s1pdgs.cpoc.datalifecycle.trigger.rest.model.ProductPatchDto;
import esa.s1pdgs.cpoc.datalifecycle.trigger.rest.model.ProductPostDto;
import esa.s1pdgs.cpoc.datalifecycle.trigger.service.error.DataLifecycleMetadataNotFoundException;
import esa.s1pdgs.cpoc.datalifecycle.trigger.service.error.DataLifecycleTriggerBadRequestException;
import esa.s1pdgs.cpoc.datalifecycle.client.error.DataLifecycleTriggerInternalServerErrorException;

@RestController
@RequestMapping("api/v1")
public class DataLifecycleTriggerRestController {

	// TODO: put api_key in a crypted place
	public static final String API_KEY = "LdbEo2020tffcEGS";

	static final Logger LOGGER = LogManager.getLogger(DataLifecycleTriggerRestController.class);

	private final DataLifecycleServiceDelegator dataLifecycleServiceDelegator;

	@Autowired
	public DataLifecycleTriggerRestController(final DataLifecycleServiceDelegator dataLifecycleServiceDelegator) {
		this.dataLifecycleServiceDelegator = dataLifecycleServiceDelegator;
	}

	/**
	 * Get list of Products.
	 * 
	 * @param apiKey token agreed by server and client for authentication
	 * @param namePattern search-pattern to filter by product name. See "Pattern matching in queries" for details
	 * @param persistentInUncompressedStorage flag if product is persisted in uncompressed storage
	 * @param minimalEvictionTimeInUncompressedStorage used to filter by evictionTimeInInUncompresedStorage &gt;= minimalEvictionTimeInUncompressedStorage
	 * @param maximalEvictionTimeInUncompressedStorage used to filter by evictionTimeInInUncompresedStorage &gt;= maximalEvictionTimeInUncompressedStorage
	 * @param persistentIncompressedStorage flag if product is persisted in compressed storage
	 * @param minimalEvictionTimeInCompressedStorage used to filter by evictionTimeInInCompresedStorage &gt;= minimalEvictionTimeInCompressedStorage
	 * @param maximalEvictionTimeInCompressedStorage used to filter by evictionTimeInInCompresedStorage &gt;= maximalEvictionTimeInCompressedStorage
	 * @param availableInLta flag if product can be retrieved from LTA
	 * @param pageSize number of processings returned per page (default is unlimited)
	 * @param pageNumber page number, first page is pageNumber=0 (default). Only used in case `pageSize` is provided
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, path = "/products")
	public List<Product> getProducts(@RequestHeader(name = "ApiKey", required = true) final String apiKey,
			@RequestParam(value = "namePattern", required = false) final String namePattern,
			@RequestParam(value = "persistentInUncompressedStorage", required = false) final Boolean persistentInUncompressedStorage,
			@RequestParam(value = "minimalEvictionTimeInUncompressedStorage", required = false) final String minimalEvictionTimeInUncompressedStorage,
			@RequestParam(value = "maximalEvictionTimeInUncompressedStorage", required = false) final String maximalEvictionTimeInUncompressedStorage,
			@RequestParam(value = "persistentIncompressedStorage", required = false) final Boolean persistentIncompressedStorage,
			@RequestParam(value = "minimalEvictionTimeInCompressedStorage", required = false) final String minimalEvictionTimeInCompressedStorage,
			@RequestParam(value = "maximalEvictionTimeInCompressedStorage", required = false) final String maximalEvictionTimeInCompressedStorage,
			@RequestParam(value = "availableInLta", required = false) final Boolean availableInLta,
			@RequestParam(value = "pageSize", required = false) final Integer pageSize,
			@RequestParam(value = "pageNumber", required = false, defaultValue = "0") final Integer pageNumber) {
		LOGGER.info("get products");

		assertValidApiKey(apiKey);

		assertValidDateTimeString("minimalEvictionTimeInUncompressedStorage", minimalEvictionTimeInUncompressedStorage, true);
		assertValidDateTimeString("maximalEvictionTimeInUncompressedStorage", maximalEvictionTimeInUncompressedStorage, true);
		assertValidDateTimeString("minimalEvictionTimeInCompressedStorage", minimalEvictionTimeInCompressedStorage, true);
		assertValidDateTimeString("maximalEvictionTimeInCompressedStorage", maximalEvictionTimeInCompressedStorage, true);

		try {
			return dataLifecycleServiceDelegator.getProducts(namePattern, persistentInUncompressedStorage,
					minimalEvictionTimeInUncompressedStorage, maximalEvictionTimeInUncompressedStorage,
					persistentIncompressedStorage, minimalEvictionTimeInCompressedStorage,
					maximalEvictionTimeInCompressedStorage, availableInLta, pageSize, pageNumber);
		} catch (DataLifecycleTriggerInternalServerErrorException e) {
			throw new DataLifecycleTriggerRestControllerException(String.format("Internal server error: %s", e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (DataLifecycleTriggerBadRequestException e) {
			throw new DataLifecycleTriggerRestControllerException(String.format("Bad request: %s", e.getMessage()),
					HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			LOGGER.error("internal server error", e);
			throw new DataLifecycleTriggerRestControllerException(String.format("Internal server error: %s", e.getMessage()),
						HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Get all products with the given productname and trigger uncompression from compressed storage or data 
	 * download from LTA if not available in uncompressed storage.
	 * 
	 * @param apiKey token agreed by server and client for authentication
	 * @param productPost array of productnames
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, path = "/products")
	public List<Product> postProducts(@RequestHeader(name = "ApiKey", required = true) final String apiKey,
			@RequestBody final ProductPostDto productPost) {
		LOGGER.info("trigger uncompression or download of products");

		assertValidApiKey(apiKey);

		try {
			return dataLifecycleServiceDelegator.getProducts(productPost.getProductnames());
		} catch (DataLifecycleTriggerInternalServerErrorException e) {
			throw new DataLifecycleTriggerRestControllerException(String.format("Internal server error"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			LOGGER.error("internal server error", e);
			throw new DataLifecycleTriggerRestControllerException(String.format("Internal server error"),
						HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
	 * Trigger eviction (start eviction process, that searches for all products to
	 * be evicted and generate EvictionJob).
	 * 
	 * @param apiKey token agreed by server and client for authentication
	 * @param operatorName optional name of operator, who is in charge of this call. This is for logging only and to enable teams to know who is in charge
	 */
	@RequestMapping(method = RequestMethod.DELETE, path = "/products")
	public void deleteProducts(@RequestHeader(name = "ApiKey", required = true) final String apiKey,
			@RequestParam(value = "operatorName", required = false) final String operatorName) {
		LOGGER.info("trigger deletion of products");

		assertValidApiKey(apiKey);

		try {
			dataLifecycleServiceDelegator.deleteProducts(operatorName);
		} catch (DataLifecycleTriggerInternalServerErrorException e) {
			throw new DataLifecycleTriggerRestControllerException(String.format("Internal server error"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			LOGGER.error("internal server error", e);
			throw new DataLifecycleTriggerRestControllerException(String.format("Internal server error"),
						HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
	 * Get product with the given productname and trigger uncompression from compressed storage or data 
	 * download from LTA if not available in uncompressed storage.
	 * 
	 * @param apiKey token agreed by server and client for authentication
	 * @param productname product name of product
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, path = "/products/{productname}")
	public Product getProduct(@RequestHeader("ApiKey") final String apiKey,
			@PathVariable("productname") final String productname) {
		LOGGER.info("get product with productname {}", productname);

		assertValidApiKey(apiKey);

		Product result = null;
		try {
			result = dataLifecycleServiceDelegator.getProduct(productname);
		} catch (DataLifecycleMetadataNotFoundException e) {
			assertProductFound(result, productname);
		} catch (DataLifecycleTriggerInternalServerErrorException e) {
			LOGGER.error("internal server error: " + productname, e);
			throw new DataLifecycleTriggerRestControllerException(String.format("Internal server error"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			LOGGER.error("internal server error: " + productname, e);
			throw new DataLifecycleTriggerRestControllerException(String.format("Internal server error"),
						HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return result;
	}

	/**
	 * Trigger eviction by productname.
	 * 
	 * @param apiKey token agreed by server and client for authentication
	 * @param productname product name of product to be evicted
	 * @param forceCompressed force eviction of product from compressed storage independent of eviction time
	 * @param forceUncompressed force eviction of product from uncompressed storage independent of eviction time
	 * @param operatorName optional name of operator, who is in charge of this call. This is for logging only and to enable teams to know who is in charge
	 */
	@RequestMapping(method = RequestMethod.DELETE, path = "products/{productname}")
	public void deleteProduct(@RequestHeader(name = "ApiKey", required = true) final String apiKey,
			@PathVariable("productname") final String productname,
			@RequestParam(value = "forceCompressed", required = false) final boolean forceCompressed,
			@RequestParam(value = "forceUncompressed", required = false) final boolean forceUncompressed,
			@RequestParam(value = "operatorName", required = false) final String operatorName) {
		LOGGER.info("delete product with productname {}", productname);
		assertValidApiKey(apiKey);
		try {
			dataLifecycleServiceDelegator.deleteProduct(productname, forceCompressed, forceUncompressed, operatorName);
		} catch (DataLifecycleMetadataNotFoundException e) {
			assertProductFound(null, productname);
		} catch (DataLifecycleTriggerInternalServerErrorException e) {
			LOGGER.error("internal server error: " + productname, e);
			throw new DataLifecycleTriggerRestControllerException(String.format("Internal server error"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			LOGGER.error("internal server error: " + productname, e);
			throw new DataLifecycleTriggerRestControllerException(String.format("Internal server error"),
						HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Set retention time for product.
	 * 
	 * @param apiKey token agreed by server and client for authentication
	 * @param productname product name of product to set retention on
	 * @param productPatch optional name of operator, who is in charge of this call. This is for logging only and to enable teams to know who is in charge,
	 * 		  new date when the product is evicted from uncompressed storage. If this value is set to 'null', retention is removed and Product is stored forever,
	 * 		  new date when the product is evicted from uncompressed storage. If this value is set to 'null', retention is removed and Product is stored forever
	 * @return
	 */
	@RequestMapping(method = RequestMethod.PATCH, path = "products/{productname}")
	public Product patchProduct(@RequestHeader("ApiKey") final String apiKey,
			@PathVariable("productname") final String productname, @RequestBody final ProductPatchDto productPatch) {
		LOGGER.info("patch product with productname {}", productname);
		assertValidApiKey(apiKey);

		assertValidDateTimeString("evictionTimeInUncompressedStorage", productPatch.getEvictionTimeInUncompressedStorage(), true);
		assertValidDateTimeString("evictionTimeInCompressedStorage", productPatch.getEvictionTimeInCompressedStorage(), true);

		Product result = null;
		try {
			result = dataLifecycleServiceDelegator.patchProduct(productname, productPatch.getOperatorName(),
					productPatch.getEvictionTimeInUncompressedStorage(),
					productPatch.getEvictionTimeInCompressedStorage());
		} catch (DataLifecycleTriggerInternalServerErrorException e) {
			throw new DataLifecycleTriggerRestControllerException(String.format("Internal server error"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (DataLifecycleMetadataNotFoundException e) {
			throw new DataLifecycleTriggerRestControllerException(String.format("Not found"),
					HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			LOGGER.error("internal server error: " + productname, e);
			throw new DataLifecycleTriggerRestControllerException(String.format("Internal server error"),
						HttpStatus.INTERNAL_SERVER_ERROR);
		}
		assertProductFound(result, productname);
		return result;
	}

	// *************************************************************************

	private static final void assertValidApiKey(final String apiKey)
			throws DataLifecycleTriggerRestControllerException {
		if (!API_KEY.equals(apiKey)) {
			throw new DataLifecycleTriggerRestControllerException("invalid API key supplied", HttpStatus.FORBIDDEN);
		}
	}

	private static final void assertValidDateTimeString(final String attributeName, final String dateTimeAsString, boolean optional)
			throws DataLifecycleTriggerRestControllerException {
		
		if (optional && (StringUtil.isEmpty(dateTimeAsString) || "null".equalsIgnoreCase(dateTimeAsString))) {
			return;
		}
		
		try {
			DataLifecycleServiceDelegator.convertDateTime(dateTimeAsString);
		} catch (NumberFormatException e) {
			throw new DataLifecycleTriggerRestControllerException(
					String.format("invalid dateTimeString on attribute %s: value: %s: %s", attributeName,
							dateTimeAsString, e),
					HttpStatus.BAD_REQUEST);
		}
	}

	private static final void assertProductFound(Object element, String productname) {
		if (element == null) {
			throw new DataLifecycleTriggerRestControllerException(
					String.format("Product not found, productname %s", productname), HttpStatus.NOT_FOUND);
		}
	}
}

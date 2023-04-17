package esa.s1pdgs.cpoc.mdc.worker.rest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataNotPresentException;
import esa.s1pdgs.cpoc.common.time.TimeInterval;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.mdc.worker.service.EsServices;
import esa.s1pdgs.cpoc.metadata.model.AuxMetadata;
import esa.s1pdgs.cpoc.metadata.model.SearchMetadata;

@RestController
@RequestMapping(path = "/metadata")
public class SearchMetadataController {

	private static final Logger LOGGER = LogManager.getLogger(SearchMetadataController.class);

	private final EsServices esServices;

	@Autowired
	public SearchMetadataController(final EsServices esServices) {
		this.esServices = esServices;
	}
	
	
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, path = "/{productFamily}/query")
	public ResponseEntity<List<SearchMetadata>> query(
			@PathVariable(name = "productFamily") final String productFamily,
			@RequestParam(name = "productType") final String productType,
			@RequestParam(name = "intervalStart") final String intervalStart,
			@RequestParam(name = "intervalStop") final String intervalStop) {
		
		LOGGER.info("Received query for family '{}' using productType {}, startTime '{}', stopTime '{}'", productFamily,
				productType, intervalStart, intervalStop);
				
		try {
			final List<SearchMetadata> results = esServices.query(
					ProductFamily.valueOf(productFamily), 
					productType,
					new TimeInterval(DateUtils.parse(intervalStart), DateUtils.parse(intervalStop))
			);
			if (results == null) {
				return new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);
			}
			return new ResponseEntity<>(results, HttpStatus.OK);
		}
		catch (final Exception ex) {
			LOGGER.error("Query error while doing query: {}", LogUtils.toString(ex));
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, path = "/{productFamily}/searchInterval")
	public ResponseEntity<List<SearchMetadata>> searchTimeInterval(
			@PathVariable(name = "productFamily") final String productFamily,
			@RequestParam(name = "intervalStart") final String intervalStart,
			@RequestParam(name = "intervalStop") final String intervalStop) {

		LOGGER.info("Received interval query for family '{}', startTime '{}', stopTime '{}'", productFamily,
				intervalStart, intervalStop);

		final List<SearchMetadata> response = new ArrayList<>();
		String startTime;
		String stopTime;
		try {
			startTime = convertDateForSearch(intervalStart, -0.0f,
					DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.000000'Z'"));

			stopTime = convertDateForSearch(intervalStop, 0.0f,
					DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.999999'Z'"));
		} catch (final Exception ex) {
			LOGGER.error("Parse error while doing intervalSearch: {}", LogUtils.toString(ex));
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		LOGGER.info("Performing metadata interval search in interval between {} and {}", startTime, stopTime);
		try {
			List<SearchMetadata> results = new ArrayList<>();
			if (productFamily.equals(ProductFamily.AUXILIARY_FILE.name())) {
				final List<SearchMetadata> results1 = esServices.intervalQuery(startTime, stopTime,
						ProductFamily.fromValue(productFamily), "aux*");
				final List<SearchMetadata> results2 = esServices.intervalQuery(startTime, stopTime,
						ProductFamily.fromValue(productFamily), "mpl*");

				final List<SearchMetadata> results3 = esServices.intervalQuery(startTime, stopTime,
						ProductFamily.fromValue(productFamily), "msk_*");

				results.addAll(results1);
				results.addAll(results2);
				results.addAll(results3);
			} else {
				results = esServices.intervalQuery(startTime, stopTime, ProductFamily.fromValue(productFamily), null);
			}

			if (results.isEmpty()) {
				LOGGER.info("No results returned.");
				return new ResponseEntity<>(response, HttpStatus.OK);
			}
			LOGGER.debug("Query returned {} results", results.size());

			for (final SearchMetadata result : results) {
				response.add(new SearchMetadata(result.getProductName(), result.getProductType(),
						result.getKeyObjectStorage(), result.getValidityStart(), result.getValidityStop(),
						result.getMissionId(), result.getSatelliteId(), result.getStationCode()));
			}
		} catch (final Exception ex) {
			LOGGER.error("Query error while doing intervalSearch: {}", LogUtils.toString(ex));
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * Search for products of a given productType in a given interval. productFamily
	 * is needed to determine the correct index of the es
	 */
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, path = "/{productFamily}/searchTypeInterval")
	public ResponseEntity<List<SearchMetadata>> searchInterval(
			@PathVariable(name = "productFamily") final String productFamily,
			@RequestParam(name = "productType") final String productType,
			@RequestParam(name = "intervalStart") final String intervalStart,
			@RequestParam(name = "intervalStop") final String intervalStop,
			@RequestParam(name = "satelliteId", defaultValue = "") final String satelliteId,
			@RequestParam(name = "timeliness", defaultValue = "") final String timeliness) {
		LOGGER.info(
				"Received interval query for family '{}', startTime '{}', stopTime '{}', productType '{}', satelliteId '{}', timeliness '{}'",
				productFamily, intervalStart, intervalStop, productType, satelliteId);

		final List<SearchMetadata> response;
		String startTime;
		String stopTime;
		try {
			startTime = convertDateForSearch(intervalStart, -0.0f, DateUtils.METADATA_DATE_FORMATTER);

			stopTime = convertDateForSearch(intervalStop, 0.0f,
					DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.999999'Z'"));
		} catch (final Exception ex) {
			LOGGER.error("Parse error while doing intervalSearch: {}", LogUtils.toString(ex));
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		try {
			response = esServices.intervalTypeQuery(startTime, stopTime, ProductFamily.fromValue(productFamily),
					productType, satelliteId, timeliness);

			if (response == null) {
				LOGGER.info("No results returned.");
				return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
			}
			LOGGER.debug("Query returned {} results", response.size());
		} catch (final Exception ex) {
			LOGGER.error("Query error while doing intervalSearch: {}", LogUtils.toString(ex));
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, path = "/{productType}/searchAuxiliary")
	public ResponseEntity<AuxMetadata> searchAuxiliary(@PathVariable(name = "productType") final String productType,
			@RequestParam(name = "productName") final String productName) {
		LOGGER.info("Performing auxiliary search for type '{}', name '{}'", productType, productName);

		try {
			final AuxMetadata auxMetadata = esServices.auxiliaryQuery(productType, productName);
			return new ResponseEntity<>(auxMetadata, HttpStatus.OK);

		} catch (final MetadataNotPresentException e) {
			LOGGER.warn("{} '{}' of type {} not available [code {}] {}", this.getClass().getSimpleName(), productName,
					productType, e.getCode().getCode(), e.getLogMessage());
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (final Exception e) {
			LOGGER.error("Query error while doing product name search: {}", LogUtils.toString(e));
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Searches for the product with given productName and in the index =
	 * productFamily. Returns only validity start and stop time.
	 * 
	 */
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, path = "/{productFamily}/searchProductName")
	public ResponseEntity<SearchMetadata> searchProductName(
			@PathVariable(name = "productFamily") final String productFamily,
			@RequestParam(name = "productName") final String productName) {

		LOGGER.info("Performing search for family '{}', name '{}'", productFamily, productName);
		try {
			final SearchMetadata result = esServices.productNameQuery(productFamily, productName);
			return new ResponseEntity<>(result, HttpStatus.OK);
		} catch (final MetadataNotPresentException e) {
			LOGGER.warn("{} '{}' of family {} not available [code {}] {}", this.getClass().getSimpleName(), productName,
					productFamily, e.getCode().getCode(), e.getLogMessage());
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (final Exception ex) {
			LOGGER.error("Query error while doing product name search: {}", LogUtils.toString(ex));
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, path = "/{productFamily}/search")
	public ResponseEntity<List<SearchMetadata>> search(@PathVariable(name = "productFamily") final String productFamily,
			@RequestParam(name = "productType", defaultValue = "NONE") final String productType,
			@RequestParam(name = "mode", defaultValue = "NONE") final String modeInput,
			@RequestParam(name = "satellite", defaultValue = "NONE") final String satellite,
			@RequestParam(name = "t0") final String startDate, @RequestParam(name = "t1") final String stopDate,
			@RequestParam(name = "processMode", defaultValue = "NONE") final String processMode,
			@RequestParam(name = "insConfId", defaultValue = "-1") final int insConfId,
			@RequestParam(value = "dt0", defaultValue = "0.0") final double dt0,
			@RequestParam(value = "dt1", defaultValue = "0.0") final double dt1,
			@RequestParam(value = "polarisation", defaultValue = "NONE") final String polarisation,
			@RequestParam(value = "bandIndexId", required = false) final String bandIndexId) {
		
		// Some tasktables contain trailing spaces in the SelectionPolicy. Make this
		// part more robust
		String mode = modeInput.trim();
		
		LOGGER.info("Received search query for family '{}', product type '{}', mode '{}', satellite '{}'",
				productFamily, productType, mode, satellite);
		try {
			final List<SearchMetadata> response = new ArrayList<>();

			if ("ValCover".equals(mode)) {
				final List<SearchMetadata> f = esServices.valCover(productType, ProductFamily.fromValue(productFamily),
						convertDateForSearch(startDate, -dt0,
								DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")),
						convertDateForSearch(stopDate, dt1,
								DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")),
						satellite, insConfId, processMode);

				if (f != null) {
					LOGGER.debug("Query returned {} results", f.size());
					return new ResponseEntity<>(f, HttpStatus.OK);
				}
				return new ResponseEntity<>(response, HttpStatus.OK);
			} else if ("LatestValCover".equals(mode)) {
				final SearchMetadata f = esServices.lastValCover(productType, ProductFamily.fromValue(productFamily),
						convertDateForSearch(startDate, -dt0,
								DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")),
						convertDateForSearch(stopDate, dt1,
								DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")),
						satellite, insConfId, processMode, bandIndexId);
				if (f != null) {
					response.add(f);
				}
				return new ResponseEntity<>(response, HttpStatus.OK);
			} else if ("ValIntersect".equals(mode)) {
				LOGGER.debug(
						"Using val intersect with productType={}, mode={}, t0={}, t1={}, processMode={}, insConfId={}, dt0={}, dt1={}",
						productType, mode, startDate, stopDate, processMode, insConfId, dt0, dt1);

				final List<SearchMetadata> f = esServices.valIntersect(
						convertDateForSearch(startDate, -dt0,
								DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")),
						convertDateForSearch(stopDate, dt1,
								DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")),
						productType, ProductFamily.fromValue(productFamily), processMode, satellite);

				if (f != null) {
					LOGGER.debug("Query returned {} results", f.size());
					return new ResponseEntity<>(f, HttpStatus.OK);
				}
				return new ResponseEntity<>(response, HttpStatus.OK);
			} else if ("ValIntersectWithoutDuplicates".equals(mode)) {
				LOGGER.debug(
						"Using val intersect without duplicates with productType={}, mode={}, t0={}, t1={}, processMode={}, insConfId={}, dt0={}, dt1={}",
						productType, mode, startDate, stopDate, processMode, insConfId, dt0, dt1);
				
				final List<SearchMetadata> f = esServices.valIntersectWithoutDuplicates(
						convertDateForSearch(startDate, -dt0,
								DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")),
						convertDateForSearch(stopDate, dt1,
								DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")),
						productType, ProductFamily.fromValue(productFamily), processMode, satellite);
				
				if (f != null) {
					LOGGER.debug("Query returned {} results", f.size());
					return new ResponseEntity<>(f, HttpStatus.OK);
				}
				return new ResponseEntity<>(response, HttpStatus.OK);
				
			} else if ("ClosestStartValidity".equals(mode)) {
				final SearchMetadata f = esServices.closestStartValidity(productType,
						ProductFamily.fromValue(productFamily),
						convertDateForSearch(startDate, -dt0,
								DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")),
						convertDateForSearch(stopDate, dt1,
								DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")),
						satellite, insConfId, processMode);

				if (f != null) {
					response.add(f);
				}
				return new ResponseEntity<>(response, HttpStatus.OK);
			} else if ("ClosestStopValidity".equals(mode)) {
				final SearchMetadata f = esServices.closestStopValidity(productType,
						ProductFamily.fromValue(productFamily),
						convertDateForSearch(startDate, -dt0,
								DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")),
						convertDateForSearch(stopDate, dt1,
								DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")),
						satellite, insConfId, processMode, polarisation);

				if (f != null) {
					response.add(f);
				}
				return new ResponseEntity<>(response, HttpStatus.OK);
			} else if ("LatestValIntersect".equals(mode)) {
				final SearchMetadata f = esServices.lastValIntersect(
						convertDateForSearch(startDate, -dt0,
								DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")),
						convertDateForSearch(stopDate, dt1,
								DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")),
						productType, ProductFamily.fromValue(productFamily), satellite);

				if (f != null) {
					response.add(f);
				}
				return new ResponseEntity<>(response, HttpStatus.OK);
			} else if ("LatestValidity".equals(mode)) {
				final SearchMetadata f = esServices.latestValidity(
						convertDateForSearch(startDate, -dt0,
								DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")),
						convertDateForSearch(stopDate, dt1,
								DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")),
						productType, ProductFamily.fromValue(productFamily), satellite);

				if (f != null) {
					response.add(f);
				}
				return new ResponseEntity<>(response, HttpStatus.OK);
			} else if ("FullCoverage".equals(mode)) {
				final List<SearchMetadata> f = esServices.fullCoverage(
						convertDateForSearch(startDate, -dt0,
								DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")),
						convertDateForSearch(stopDate, dt1,
								DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")),
						productType, ProductFamily.fromValue(productFamily), processMode, satellite);

				if (f != null) {
					LOGGER.debug("Query returned {} results", f.size());
					return new ResponseEntity<>(f, HttpStatus.OK);
				}
				return new ResponseEntity<>(response, HttpStatus.OK);
			} else if ("LatestValidityClosest".equals(mode)) {
				final SearchMetadata f = esServices.latestValidityClosest(
						convertDateForSearch(startDate, -dt0,
								DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")),
						convertDateForSearch(stopDate, dt1,
								DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")),
						productType, ProductFamily.fromValue(productFamily), processMode, satellite);

				if (f != null) {
					response.add(f);
				}
				return new ResponseEntity<>(response, HttpStatus.OK);
			} else if ("LatestStartValidity".equals(mode)) {
				final SearchMetadata f = esServices.latestStartValidity(productType, ProductFamily.fromValue(productFamily), satellite);

				if (f != null) {
					response.add(f);
				}
				return new ResponseEntity<>(response, HttpStatus.OK);
			} else if ("LatestStopValidity".equals(mode)) {
				final SearchMetadata f = esServices.latestStopValidity(productType, ProductFamily.fromValue(productFamily), satellite);

				if (f != null) {
					response.add(f);
				}
				return new ResponseEntity<>(response, HttpStatus.OK);
			} else if ("LatestValCoverClosest".equals(mode)) {
				final SearchMetadata f = esServices.latestValCoverClosest(
						convertDateForSearch(startDate, -dt0,
								DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")),
						convertDateForSearch(stopDate, dt1,
								DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")),
						productType, ProductFamily.fromValue(productFamily), satellite);

				if (f != null) {
					response.add(f);
				}
				return new ResponseEntity<>(response, HttpStatus.OK);
			} else {
				LOGGER.error("Invalid selection policy mode {} for product type {}", mode, productType);
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
		} catch (final AbstractCodedException e) {
			LOGGER.error("Error on performing selection policy mode {} for product type {}: [code {}] {}", mode,
					productType, e.getCode().getCode(), e.getLogMessage());
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		} catch (final Exception e) {
			LOGGER.error("Error on performing selection policy mode {} for product type {}: {}", mode, productType,
					LogUtils.toString(e));
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private String convertDateForSearch(final String dateStr, final double delta,
			final DateTimeFormatter outFormatter) {
		final LocalDateTime time = LocalDateTime.parse(dateStr,
				DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'"));
		final LocalDateTime timePlus = time.plusSeconds(Math.round(delta));
		return timePlus.format(outFormatter);
	}
}

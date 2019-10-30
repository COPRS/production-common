package esa.s1pdgs.cpoc.mdcatalog.rest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.mdcatalog.es.EsServices;
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

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/{productFamily}/searchInterval")
	public ResponseEntity<List<SearchMetadata>> searchTimeInterval(
			@PathVariable(name = "productFamily") String productFamily,
			@RequestParam(name = "intervalStart") String intervalStart,
			@RequestParam(name = "intervalStop") String intervalStop) {

		LOGGER.info("Received interval query for family '{}', startTime '{}', stopTime '{}'", productFamily,
				intervalStart, intervalStop);

		List<SearchMetadata> response = new ArrayList<SearchMetadata>();
		String startTime = null;
		String stopTime = null;
		try {
			startTime = convertDateForSearch(intervalStart, -0.0f,
					DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.000000'Z'"));

			stopTime = convertDateForSearch(intervalStop, 0.0f,
					DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.999999'Z'"));
		} catch (Exception ex) {
			LOGGER.error("Parse error while doing intervalSearch: {}", LogUtils.toString(ex));
			return new ResponseEntity<List<SearchMetadata>>(HttpStatus.BAD_REQUEST);
		}

		LOGGER.info("Performing metadata interval search in interval between {} and {}", startTime, stopTime);
		try {
			List<SearchMetadata> results = new ArrayList<>();
			if (productFamily.equals(ProductFamily.AUXILIARY_FILE.name())) {
				List<SearchMetadata> results1 = esServices.intervalQuery(startTime, stopTime,
						ProductFamily.fromValue(productFamily), "aux*");
				List<SearchMetadata> results2 = esServices.intervalQuery(startTime, stopTime,
						ProductFamily.fromValue(productFamily), "mpl*");

				results.addAll(results1);
				results.addAll(results2);
			} else {
				results = esServices.intervalQuery(startTime, stopTime, ProductFamily.fromValue(productFamily), null);
			}

			if (results == null) {
				LOGGER.info("No results returned.");
				return new ResponseEntity<List<SearchMetadata>>(response, HttpStatus.OK);
			}
			LOGGER.debug("Query returned {} results", results.size());

			for (SearchMetadata result : results) {
				response.add(new SearchMetadata(result.getProductName(), result.getProductType(),
						result.getKeyObjectStorage(), result.getValidityStart(), result.getValidityStop(),
						result.getMissionId(), result.getSatelliteId(), result.getStationCode()));
			}
		} catch (Exception ex) {
			LOGGER.error("Query error while doing intervalSearch: {}", LogUtils.toString(ex));
			return new ResponseEntity<List<SearchMetadata>>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<List<SearchMetadata>>(response, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/{productFamily}/search")
	public ResponseEntity<List<SearchMetadata>> search(@PathVariable(name = "productFamily") String productFamily,
			@RequestParam(name = "productType", defaultValue = "NONE") String productType,
			@RequestParam(name = "mode", defaultValue = "NONE") String mode,
			@RequestParam(name = "satellite", defaultValue = "NONE") String satellite,
			@RequestParam(name = "t0") String startDate, @RequestParam(name = "t1") String stopDate,
			@RequestParam(name = "processMode", defaultValue = "NONE") String processMode,
			@RequestParam(name = "insConfId", defaultValue = "-1") int insConfId,
			@RequestParam(value = "dt0", defaultValue = "0.0") double dt0,
			@RequestParam(value = "dt1", defaultValue = "0.0") double dt1,
			@RequestParam(value = "polarisation", defaultValue = "NONE") String polarisation
	) {
		LOGGER.info("Received search query for family '{}', product type '{}', mode '{}', satellite '{}'",
				productFamily, productType, mode, satellite);
		try {
			final List<SearchMetadata> response = new ArrayList<SearchMetadata>();
			
			if ("LatestValCover".equals(mode)) {
				final SearchMetadata f = esServices.lastValCover(
						productType, 
						ProductFamily.fromValue(productFamily),
						convertDateForSearch(startDate, -dt0, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.999999'Z'")),
						convertDateForSearch(stopDate, dt1, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.000000'Z'")),
						satellite, 
						insConfId, 
						processMode
				);
				if (f != null) {
					response.add(new SearchMetadata(
							f.getProductName(), 
							f.getProductType(), 
							f.getKeyObjectStorage(),
							f.getValidityStart(), 
							f.getValidityStop(), 
							f.getMissionId(), 
							f.getSatelliteId(),
							f.getStationCode()
					));
				}
				return new ResponseEntity<List<SearchMetadata>>(response, HttpStatus.OK);
			} else if ("ValIntersect".equals(mode)) {
				LOGGER.debug("Using val intersect with productType={}, mode={}, t0={}, t1={}, proccessingMode={}, insConfId={}, dt0={}, dt1={}", productType, mode, startDate, stopDate, processMode, 
						insConfId, dt0, dt1);
				
				final List<SearchMetadata> f = esServices.valIntersect(
						convertDateForSearch(startDate, -dt0, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")),
						convertDateForSearch(stopDate, dt1, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")),
						productType, 
						processMode, 
						satellite
				);

				if (f != null) {
					LOGGER.debug("Query returned {} results", f.size());				

					for (final SearchMetadata m : f) {
						response.add(new SearchMetadata(
								m.getProductName(), 
								m.getProductType(), 
								m.getKeyObjectStorage(),
								m.getValidityStart(), 
								m.getValidityStop(), 
								m.getMissionId(), 
								m.getSatelliteId(),
								m.getStationCode()
						));
					}
				}
				return new ResponseEntity<List<SearchMetadata>>(response, HttpStatus.OK);				
			} else if ("ClosestStartValidity".equals(mode)) {
				final SearchMetadata f = esServices.closestStartValidity(
						productType, 
						ProductFamily.fromValue(productFamily),
						convertDateForSearch(startDate, -dt0, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")),
						convertDateForSearch(stopDate, dt1, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")),
						satellite, 
						insConfId, 
						processMode
				);

				if (f != null) {
					response.add(new SearchMetadata(
							f.getProductName(), 
							f.getProductType(), 
							f.getKeyObjectStorage(),
							f.getValidityStart(), 
							f.getValidityStop(), 
							f.getMissionId(), 
							f.getSatelliteId(),
							f.getStationCode()
					));
				}
				return new ResponseEntity<List<SearchMetadata>>(response, HttpStatus.OK);
			} else if ("ClosestStopValidity".equals(mode)) {
				final SearchMetadata f = esServices.closestStopValidity(
						productType, 
						ProductFamily.fromValue(productFamily),
						convertDateForSearch(startDate, -dt0,DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")),
						convertDateForSearch(stopDate, dt1, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")),
						satellite, 
						insConfId, 
						processMode, 
						polarisation
				);

				if (f != null) {
					response.add(new SearchMetadata(
							f.getProductName(), 
							f.getProductType(), 
							f.getKeyObjectStorage(),
							f.getValidityStart(), 
							f.getValidityStop(), 
							f.getMissionId(), 
							f.getSatelliteId(),
							f.getStationCode()
					));
				}
				return new ResponseEntity<List<SearchMetadata>>(response, HttpStatus.OK);
			} else {				
				LOGGER.error("Invalid selection policy mode {} for product type {}", mode, productType);				
				return new ResponseEntity<List<SearchMetadata>>(HttpStatus.BAD_REQUEST);
			}
		} catch (AbstractCodedException e) {
			LOGGER.error("Error on performing selection policy mode {} for product type {}: [code {}] {}", 
					mode, productType, e.getCode().getCode(), e.getLogMessage());		
			return new ResponseEntity<List<SearchMetadata>>(HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			LOGGER.error("Error on performing selection policy mode {} for product type {}: {}", 
					mode, productType, LogUtils.toString(e));	
			return new ResponseEntity<List<SearchMetadata>>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private String convertDateForSearch(String dateStr, double delta, DateTimeFormatter outFormatter) {
		LocalDateTime time = LocalDateTime.parse(dateStr,
				DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'"));
		LocalDateTime timePlus = time.plusSeconds(Math.round(delta));
		return timePlus.format(outFormatter);
	}
}

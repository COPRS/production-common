package esa.s1pdgs.cpoc.validation.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.metadata.model.SearchMetadata;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;
import esa.s1pdgs.cpoc.report.LoggerReporting;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.validation.config.ApplicationProperties;
import esa.s1pdgs.cpoc.validation.config.ApplicationProperties.FamilyIntervalConf;
import esa.s1pdgs.cpoc.validation.service.metadata.MetadataService;

@Service
public class ValidationService {
	private static final Logger LOGGER = LogManager.getLogger(ValidationService.class);

	private final MetadataService metadataService;

	private final ObsClient obsClient;

	@Autowired
	private ApplicationProperties properties;

	@Autowired
	public ValidationService(MetadataService metadataService, ObsClient obsClient) {
		this.metadataService = metadataService;
		this.obsClient = obsClient;
	}

	public int checkConsistencyForInterval() {
		final Reporting.Factory reportingFactory = new LoggerReporting.Factory("ValidationService");
		int discrepancies = 0;

		Set<ProductFamily> families = properties.getFamilies().keySet();
		LOGGER.info("Validating {} product families", families.size());
		for (ProductFamily family : families) {
			FamilyIntervalConf conf = properties.getFamilies().get(family);
			
			LocalDateTime startInterval = LocalDateTime.now().minusSeconds(conf.getLifeTime());
			LocalDateTime endInterval = LocalDateTime.now().minusSeconds(conf.getInitialDelay()); 
			discrepancies += validateProductFamily(reportingFactory, family, startInterval, endInterval);
		}
		LOGGER.info("Found {} discrepancies for all families",discrepancies);
		return discrepancies;
	}

	int validateProductFamily(Reporting.Factory reportingFactory, ProductFamily family, LocalDateTime startInterval,
			LocalDateTime endInterval) {

		final Reporting reportingValidation = reportingFactory.newReporting(0);
		reportingValidation.begin(String.format("Starting validation task from %s to %s for family %s",
				startInterval, endInterval, family));

		final Reporting reportingMetadata = reportingFactory.newReporting(1);

		try {

			List<SearchMetadata> metadataResults = null;
			try {
				reportingMetadata.begin("Gathering discrepancies in metadata catalog");

				String queryFamily = getQueryFamily(family);
				LOGGER.info("Performing metadata query for family '{}'", queryFamily);
				metadataResults = metadataService.query(ProductFamily.valueOf(queryFamily), startInterval, endInterval);
				if (metadataResults == null) {
					// set to empty list
					metadataResults = new ArrayList<>();
				}
			} catch (MetadataQueryException e) {
				reportingMetadata.error("Error occured while performing metadata catalog query task [code {}] {}",
						e.getCode().getCode(), e.getLogMessage());
				throw e;
			}

			final Reporting reportingObs = reportingFactory.newReporting(2);
			Map<String, ObsObject> obsResults = null;
			try {
				reportingObs.begin("Gathering discrepancies in OBS");

				Date startDate = Date.from(startInterval.atZone(ZoneId.of("UTC")).toInstant());
				Date endDate = Date.from(endInterval.atZone(ZoneId.of("UTC")).toInstant());

				obsResults = obsClient.listInterval(ProductFamily.valueOf(family.name()), startDate, endDate);
				LOGGER.info("OBS query for family '{}' returned {} results", family, obsResults.size());

			} catch (SdkClientException | DateTimeParseException e) {
				reportingObs.error("Error occured while performing obs query task: {}", e.getMessage());
				throw e;
			}

			List<String> metadataDiscrepancies = new ArrayList<>();
			for (SearchMetadata smd : metadataResults) {
				if (!verifyMetadataForObject(smd, obsResults.values())) {
					LOGGER.info("Product {} does exist in metadata catalog, but not in OBS", smd.getKeyObjectStorage());
					if (family.name().contains("_ZIP")) {
						// To show the actual filename we need to add zip to the metadata name
						metadataDiscrepancies.add(smd.getKeyObjectStorage() + ".zip");
					} else {
						// Its a plain product, we can use the metadata directly
						metadataDiscrepancies.add(smd.getKeyObjectStorage());
					}
				} else {
					LOGGER.debug("Product {} does exist in metadata catalog and OBS", smd.getKeyObjectStorage());
				}
			}

			if (metadataDiscrepancies.isEmpty()) {
				reportingMetadata.end("No discrepancies found in MetadataCatalog");
				reportingValidation.end("No discrepancy found");
			} else {
				reportingMetadata.error("Products present in MetadataCatalog, but not in OBS: {}",
						buildProductList(metadataDiscrepancies));
				reportingValidation.error("Discrepancy found for {} product(s)", metadataDiscrepancies.size());
			}

			LOGGER.info("Found {} discrepancies for family '{}'", metadataDiscrepancies.size(), family);
			return metadataDiscrepancies.size();
		} catch (Exception ex) {
			reportingValidation.error("Error occured while performing validation task: {}", ex.getMessage());
		}

		return -1;
	}

	String getQueryFamily(ProductFamily family) {
		if (family.name().endsWith("_ZIP")) {
			/*
			 * Oops: We are having a zip family here. This means we are not performing the
			 * query on the zipped family itself, but the plain family
			 */
			return family.name().replace("_ZIP", "");
		}

		return family.name();
	}

	private boolean verifyMetadataForObject(SearchMetadata metadata, Collection<ObsObject> objects) {
		LOGGER.debug("Verifying if metadata entry for product {} exist in OBS", metadata.getKeyObjectStorage());
		if (metadata.getKeyObjectStorage().contains("AUX") || metadata.getKeyObjectStorage().contains("MPL")) {
			return verifyAuxMetadataForObject(metadata, objects);
		} else if (metadata.getKeyObjectStorage().startsWith("S1A/")
				|| metadata.getKeyObjectStorage().startsWith("S1B/")) {
			return verifySessionForObject(metadata, objects);
		} else {
			return verifySliceForObject(metadata, objects);
		}
	}

	boolean verifyAuxMetadataForObject(SearchMetadata metadata, Collection<ObsObject> objects) {
		String key = metadata.getKeyObjectStorage();

		for (ObsObject obj : objects) {
			//
			String obsKey = obj.getKey();
			// some products are .SAFE. In this case, we ignore everything behind the first
			// slash
			if (!obsKey.contains(".zip") && obsKey.contains(".SAFE")) {
				obsKey = obsKey.substring(0, obsKey.indexOf("/"));
			}
//			LOGGER.info("key: {}, aux: {}", key, obsKey);
			if (obsKey.contains(key)) {
				return true;
			}
		}

		return false;
	}

	boolean verifySessionForObject(SearchMetadata metadata, Collection<ObsObject> objects) {
		String key = metadata.getKeyObjectStorage();

		for (ObsObject obj : objects) {
			String obsKey = obj.getKey();
			if (key.equals(obsKey)) {
				return true;
			}
		}
		return false;
	}

	boolean verifySliceForObject(SearchMetadata metadata, Collection<ObsObject> objects) {
		String key = metadata.getKeyObjectStorage();

		for (ObsObject obj : objects) {
			String obsKey = null;
			if (obj.getKey().contains(".zip")) {
				// It is a zip file, so we just take the name as key
				obsKey = obj.getKey().replace(".zip", "");
			} else {
				// It is an unzipped, we just take the base name
				obsKey = obj.getKey().substring(0, obj.getKey().indexOf("/"));
			}

			if (key.equals(obsKey)) {
				return true;
			}
		}
		return false;
	}

	private String buildProductList(List<String> products) {
		if (products.size() == 0) {
			return "";
		}
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		for (int i = 0; i < products.size(); i++) {
			builder.append(products.get(i));
			if (i < products.size() - 1) {
				builder.append(",");
			}
		}
		builder.append("]");
		return builder.toString();
	}

}

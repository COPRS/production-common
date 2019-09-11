package esa.s1pdgs.cpoc.validation.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.metadata.model.SearchMetadata;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsServiceException;
import esa.s1pdgs.cpoc.obs_sdk.ObsValidationException;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;
import esa.s1pdgs.cpoc.report.LoggerReporting;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
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
	
	private static class Discrepancy {
		public String obsKey;
		public String reason;
		
		public Discrepancy(String obsKey, String reason) {
			this.obsKey = obsKey;
			this.reason = reason;
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			return sb.append(obsKey).append(" (reason:").append(reason).append(")").toString();
		}
	}

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
		reportingValidation.begin(new ReportingMessage("Starting validation task from {} to {} for family {}",
				startInterval, endInterval, family));

		final Reporting reportingMetadata = reportingFactory.newReporting(1);

		try {

			/*
			 * Step 1: We are gathering all information from catalog and obs at the moment we are performing
			 * the change to ensure that no data is changed or added meanwhile giving false positives.
			 */
			List<SearchMetadata> metadataResults = null;
			try {
				reportingMetadata.begin(new ReportingMessage("Gathering discrepancies in metadata catalog"));

				String queryFamily = getQueryFamily(family);
				LOGGER.info("Performing metadata query for family '{}'", queryFamily);
				metadataResults = metadataService.query(ProductFamily.valueOf(queryFamily), startInterval, endInterval);				
				if (metadataResults == null) {
					// set to empty list
					metadataResults = new ArrayList<>();
				}
				
				LOGGER.info("Metadata query for family '{}' returned {} hits", queryFamily, metadataResults.size());				
			} catch (MetadataQueryException e) {
				reportingMetadata.error(new ReportingMessage("Error occured while performing metadata catalog query task [code {}] {}",
						e.getCode().getCode(), e.getLogMessage()));
				throw e;
			}

			final Reporting reportingObs = reportingFactory.newReporting(2);
			Map<String, ObsObject> obsResults = null;
			try {
				reportingObs.begin(new ReportingMessage("Gathering discrepancies in OBS"));

				Date startDate = Date.from(startInterval.atZone(ZoneId.of("UTC")).toInstant());
				Date endDate = Date.from(endInterval.atZone(ZoneId.of("UTC")).toInstant());

				obsResults = obsClient.listInterval(ProductFamily.valueOf(family.name()), startDate, endDate);
				LOGGER.info("OBS query for family '{}' returned {} results", family, obsResults.size());

			} catch (SdkClientException | DateTimeParseException ex) {
				reportingObs.error(new ReportingMessage("Error occured while performing obs query task: {}", ex.getMessage()));
				throw ex;
			}
			
			/*
			 * Step 2: We are doing a query on the metadata of the family and getting all entries form the
			 * catalog that is available. Then we instruct the obs client to validate if these entries are valid.
			 */

			List<Discrepancy> discrepancies = new ArrayList<>();
			for (SearchMetadata smd : metadataResults) {
				try {
					// If its a zipped family, we need to extend the filename
					String key = smd.getKeyObjectStorage();					
					if (family.name().endsWith("_ZIP")) {
						key += ".zip";
					} 
					obsClient.validate(new ObsObject(family, key));
				} catch (ObsServiceException | ObsValidationException ex) {
					// Validation failed for that object.
					LOGGER.debug(ex);
					discrepancies.add(new Discrepancy(smd.getKeyObjectStorage(), ex.getMessage()));
				}
			}
			
			/*
			 * Step 3: After we know that the catalog data is valid within the OBS, we check if there are additional
			 * products stored within that are not expects.
			 */
			Set<String> realKeys = extractRealKeys(obsResults.values(), family);
			for (SearchMetadata smd : metadataResults) {
				realKeys.remove(smd.getKeyObjectStorage());
			}
			
			LOGGER.info("Found {} keys that are in OBS, but not in MetadataCatalog", realKeys.size());
			for (String key: realKeys) {
				Discrepancy discrepancy = new Discrepancy(key,"Exists in OBS, but not in MDC");
				discrepancies.add(discrepancy);
			}
			
			/*
			 * Step 4: Presenting the results of the discrepancies check
			 */

			if (discrepancies.isEmpty()) {
				reportingMetadata.end(new ReportingMessage("No discrepancies found in MetadataCatalog"));
				reportingValidation.end(new ReportingMessage("No discrepancy found"));
			} else {
				reportingMetadata.error(new ReportingMessage("Discrepancies found for following products: {}",
						buildProductList(discrepancies)));
				reportingValidation.error(new ReportingMessage("Discrepancy found for {} product(s)", discrepancies.size()));
			}

			LOGGER.info("Found {} discrepancies for family '{}'", discrepancies.size(), family);
			return discrepancies.size();
		} catch (Exception ex) {
			reportingValidation.error(new ReportingMessage("Error occured while performing validation task: {}", LogUtils.toString(ex)));
		}

		return 0;
	}
	
	Set<String> extractRealKeys(Collection<ObsObject> obsResults, ProductFamily family) {
		Set<String> realProducts = new HashSet<>();
		for (ObsObject obsResult: obsResults) {
			String key = obsResult.getKey();
			int index = key.indexOf("/");
			String realKey = null;
			
			if (family == ProductFamily.EDRS_SESSION) {
				realKey = key;
			} else if (index != -1) {
				realKey = key.substring(0, index);
			} else {
				realKey = key;							
			}
			if (key.endsWith(".zip")) {
				// Special case zipped products. The MDC key does not contain the zip!
				realKey = realKey.substring(0,key.lastIndexOf(".zip"));
			}	
			LOGGER.trace("key is {}", realKey);
			realProducts.add(realKey);
		}
		return realProducts;
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

	private String buildProductList(List<Discrepancy> discrepancies) {
		if (discrepancies.size() == 0) {
			return "";
		}
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		for (int i = 0; i < discrepancies.size(); i++) {
			builder.append(discrepancies.get(i));
			if (i < discrepancies.size() - 1) {
				builder.append(",");
			}
		}
		builder.append("]");
		return builder.toString();
	}

}

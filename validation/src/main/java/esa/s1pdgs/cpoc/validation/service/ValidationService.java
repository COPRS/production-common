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

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.model.SearchMetadata;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsServiceException;
import esa.s1pdgs.cpoc.obs_sdk.ObsValidationException;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;
import esa.s1pdgs.cpoc.validation.config.ApplicationProperties;
import esa.s1pdgs.cpoc.validation.config.ApplicationProperties.FamilyIntervalConf;

@Service
public class ValidationService {
	private static final Logger LOGGER = LogManager.getLogger(ValidationService.class);

	private final MetadataClient metadataClient;

	private final ObsClient obsClient;

	@Autowired
	private ApplicationProperties properties;
	
	@Autowired
	private AppStatus appStatus;
	
	private int nbRunningConsistencyChecks = 0; 
	private Object nbRunningConsistencyChecksLock = new Object();
    		
	private static class Discrepancy {
		public String obsKey;
		public String reason;
		
		public Discrepancy(final String obsKey, final String reason) {
			this.obsKey = obsKey;
			this.reason = reason;
		}
		
		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder();
			return sb.append(obsKey).append(" (reason:").append(reason).append(")").toString();
		}
	}

	@Autowired
	public ValidationService(final MetadataClient metadataClient, final ObsClient obsClient) {
		this.metadataClient = metadataClient;
		this.obsClient = obsClient;
	}

	public int checkConsistencyForInterval() {
		synchronized(nbRunningConsistencyChecksLock) {
			if (++nbRunningConsistencyChecks == 1) {
				appStatus.setProcessing(esa.s1pdgs.cpoc.appstatus.Status.PROCESSING_MSG_ID_UNDEFINED);
			}
		}
		final Reporting reporting = ReportingUtils.newReportingBuilderFor("ValidationService")
				.newWorkerComponentReporting();
		
		int totalDiscrepancies = 0;

		final Set<ProductFamily> families = properties.getFamilies().keySet();
		LOGGER.info("Validating {} product families", families.size());
		for (final ProductFamily family : families) {
			final FamilyIntervalConf conf = properties.getFamilies().get(family);
			
			final LocalDateTime startInterval = LocalDateTime.now().minusSeconds(conf.getLifeTime());
			final LocalDateTime endInterval = LocalDateTime.now().minusSeconds(conf.getInitialDelay());
			
			try {
				reporting.begin(new ReportingMessage("Starting validation task from {} to {} for family {}",
					startInterval, endInterval, family));		
				int familyDiscrepancies = validateProductFamily(reporting.getChildFactory(), family, startInterval, endInterval);				
				totalDiscrepancies += familyDiscrepancies;
				if (familyDiscrepancies == 0) {
					reporting.end(new ReportingMessage("No discrepancy found"));
				} else {
					reporting.error(new ReportingMessage("Discrepancy found for {} product(s)", familyDiscrepancies));
				}
			} catch (final Exception ex) {
				reporting.error(new ReportingMessage("Error occured while performing validation task: {}", LogUtils.toString(ex)));
			}
		}
		LOGGER.info("Found {} discrepancies for all families", totalDiscrepancies);
		synchronized(nbRunningConsistencyChecksLock) {
			if (--nbRunningConsistencyChecks == 0) {
				appStatus.setWaiting();
			}
		}
		return totalDiscrepancies;
	}

	int validateProductFamily(final Reporting.ChildFactory reportingChildFactory, final ProductFamily family, final LocalDateTime startInterval,
			final LocalDateTime endInterval) throws Exception {

		final Reporting reportingMetadata = reportingChildFactory.newChild("ValidationService.ValidateMDC");

		/*
		 * Step 1: We are gathering all information from catalog and obs at the moment we are performing
		 * the change to ensure that no data is changed or added meanwhile giving false positives.
		 */
		List<SearchMetadata> metadataResults = null;
		try {
			reportingMetadata.begin(new ReportingMessage("Gathering discrepancies in metadata catalog"));

			final String queryFamily = getQueryFamily(family);
			LOGGER.info("Performing metadata query for family '{}'", queryFamily);
			metadataResults = metadataClient.query(ProductFamily.valueOf(queryFamily), startInterval, endInterval);				
			
			LOGGER.info("Metadata query for family '{}' returned {} hits", queryFamily, metadataResults.size());				
		} catch (final MetadataQueryException e) {
			reportingMetadata.error(new ReportingMessage("Error occured while performing metadata catalog query task [code {}] {}",
					e.getCode().getCode(), e.getLogMessage()));
			throw e;
		}

		final Reporting reportingObs = reportingChildFactory.newChild("ValidationService.ValidateOBS");
		Map<String, ObsObject> obsResults = null;
		try {
			reportingObs.begin(new ReportingMessage("Gathering discrepancies in OBS"));

			final Date startDate = Date.from(startInterval.atZone(ZoneId.of("UTC")).toInstant());
			final Date endDate = Date.from(endInterval.atZone(ZoneId.of("UTC")).toInstant());

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

		final List<Discrepancy> discrepancies = new ArrayList<>();
		for (final SearchMetadata smd : metadataResults) {
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
		final Set<String> realKeys = extractRealKeys(obsResults.values(), family);
		for (final SearchMetadata smd : metadataResults) {
			realKeys.remove(smd.getKeyObjectStorage());
		}
		
		LOGGER.info("Found {} keys that are in OBS, but not in MetadataCatalog", realKeys.size());
		for (final String key: realKeys) {
			final Discrepancy discrepancy = new Discrepancy(key,"Exists in OBS, but not in MDC");
			discrepancies.add(discrepancy);
		}
		
		/*
		 * Step 4: Presenting the results of the discrepancies check
		 */

		if (discrepancies.isEmpty()) {
			reportingMetadata.end(new ReportingMessage("No discrepancies found in MetadataCatalog"));
		} else {
			reportingMetadata.error(new ReportingMessage("Discrepancies found for following products: {}",
					buildProductList(discrepancies)));
		}

		LOGGER.info("Found {} discrepancies for family '{}'", discrepancies.size(), family);
		return discrepancies.size();
	}
	
	Set<String> extractRealKeys(final Collection<ObsObject> obsResults, final ProductFamily family) {
		final Set<String> realProducts = new HashSet<>();
		for (final ObsObject obsResult: obsResults) {			
			final String key = obsResult.getKey();
			final int index = key.indexOf("/");
			String realKey = null;
			
			if (family == ProductFamily.EDRS_SESSION) {
				realKey = key;
				/*
				 *  EDRS_Sessions are just queried on raw and not containg DSIB.
				 *  So we are removing them from the check
				 */
				if (realKey.endsWith("DSIB.xml")) {
					LOGGER.debug("Ignoring DSIB file: {}",realKey);
					continue;
				}
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

	String getQueryFamily(final ProductFamily family) {
		if (family.name().endsWith("_ZIP")) {
			/*
			 * Oops: We are having a zip family here. This means we are not performing the
			 * query on the zipped family itself, but the plain family
			 */
			return family.name().replace("_ZIP", "");
		}

		return family.name();
	}

	private String buildProductList(final List<Discrepancy> discrepancies) {
		if (discrepancies.size() == 0) {
			return "";
		}
		final StringBuilder builder = new StringBuilder();
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

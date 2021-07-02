package esa.s1pdgs.cpoc.validation.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.model.DataLifecycleMetadata;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.model.filter.DataLifecycleTextFilter;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.persistence.DataLifecycleMetadataRepository;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.persistence.DataLifecycleMetadataRepositoryException;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.model.SearchMetadata;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsServiceException;
import esa.s1pdgs.cpoc.obs_sdk.ObsValidationException;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingFactory;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;
import esa.s1pdgs.cpoc.validation.config.ApplicationProperties;
import esa.s1pdgs.cpoc.validation.config.ApplicationProperties.FamilyIntervalConf;

@Service
public class ValidationService {
	private static final Logger LOGGER = LogManager.getLogger(ValidationService.class);

	private final MetadataClient metadataClient;

	private final ObsClient obsClient;

	private final DataLifecycleMetadataRepository lifecycleMetadataRepo;

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
	public ValidationService(final MetadataClient metadataClient, final ObsClient obsClient,
			final DataLifecycleMetadataRepository lifecycleMetadataRepo) {
		this.metadataClient = metadataClient;
		this.obsClient = obsClient;
		this.lifecycleMetadataRepo = lifecycleMetadataRepo;
	}

	public void checkConsistencyForInterval() {
		synchronized (nbRunningConsistencyChecksLock) {
			if (++nbRunningConsistencyChecks == 1) {
				appStatus.setProcessing(esa.s1pdgs.cpoc.appstatus.Status.PROCESSING_MSG_ID_UNDEFINED);
			}
		}
		final Reporting reporting = ReportingUtils.newReportingBuilder().newReporting("ValidationService");
		reporting.begin(new ReportingMessage("Starting validation"));
		try {
			int totalDiscrepancies = 0;
			final Set<ProductFamily> families = properties.getFamilies().keySet();
			LOGGER.info("Validating {} product families", families.size());
			for (final ProductFamily family : families) {
				final FamilyIntervalConf conf = properties.getFamilies().get(family);
				final LocalDateTime startInterval = LocalDateTime.now().minusSeconds(conf.getLifeTime());
				final LocalDateTime endInterval = LocalDateTime.now().minusSeconds(conf.getInitialDelay());
				final int familyDiscrepancies = validateProductFamily(reporting, family, startInterval, endInterval);
				totalDiscrepancies += familyDiscrepancies;
			}
			LOGGER.info("Found {} discrepancies for all families", totalDiscrepancies);

			reporting.end(new ReportingMessage("End validation"));
		} catch (final Exception e) {
			reporting
					.error(new ReportingMessage("Error occured while performing validation: {}", LogUtils.toString(e)));
		}

		synchronized (nbRunningConsistencyChecksLock) {
			if (--nbRunningConsistencyChecks == 0) {
				appStatus.setWaiting();
			}
		}
	}

	int validateProductFamily(final ReportingFactory reportingFactory, final ProductFamily family,
			final LocalDateTime startInterval, final LocalDateTime endInterval) throws Exception {
		final Reporting reporting = reportingFactory.newReporting("ValidateMDC");
		reporting.begin(new ReportingMessage("Starting validation from {} to {} for family {}", startInterval,
				endInterval, family));
		try {
			/*
			 * Step 1: Fetch a snapshot from MDC and OBS to ensure that no data is changed
			 * or added meanwhile giving false positives.
			 */
			List<SearchMetadata> metadataResults = null;
			final String queryFamily = getQueryFamily(family);
			LOGGER.info("Performing metadata query for family '{}'", queryFamily);
			metadataResults = metadataClient.query(ProductFamily.valueOf(queryFamily), startInterval, endInterval);
			LOGGER.info("Metadata query for family '{}' returned {} hits", queryFamily, metadataResults.size());

			Map<String, ObsObject> obsResults = null;
			final Date startDate = Date.from(startInterval.atZone(ZoneId.of("UTC")).toInstant());
			final Date endDate = Date.from(endInterval.atZone(ZoneId.of("UTC")).toInstant());
			obsResults = obsClient.listInterval(ProductFamily.valueOf(family.name()), startDate, endDate);
			LOGGER.info("OBS query for family '{}' returned {} results", family, obsResults.size());

			final List<Discrepancy> discrepancies = new ArrayList<>();
			
			/*
			 * Step 2: Compare all files found in OBS if they are present in the MDC.
			 */
			final Set<String> realKeys = extractRealKeysForMDC(obsResults.values(), family);
			for (final SearchMetadata smd : metadataResults) {
				realKeys.remove(smd.getKeyObjectStorage());
			}

			for (final String key : realKeys) {
				/*
				 * Check if key is in MDC (might be not in the results of the previous MDC query interval)
				 */
				try {
					metadataClient.queryByFamilyAndProductName(family.name(), key);
					// exists in MDC
				} catch (final MetadataQueryException e) {
					// not exists in MDC
					final Discrepancy discrepancy = new Discrepancy(key, "Exists in OBS, but not in MDC");
					discrepancies.add(discrepancy);					
				}
			}
			
			/*
			 * Step 3: Validate all OBS files found
			 */
			for (String key : extractRealKeysForObsValidation(obsResults.values(), family)) {
				
				final Reporting obsReporting = reporting.newReporting("ValidateObs");
				try {
					obsReporting.begin(new ReportingMessage("Validating %s", key));
					obsClient.validate(new ObsObject(family, key));
					obsReporting.end(new ReportingMessage("%s is valid",key));
				} catch (ObsServiceException | ObsValidationException ex) {
					obsReporting.error(new ReportingMessage("%s is invalid: %s", key, ex.getMessage()));
					// Validation failed for that object.
					LOGGER.debug(ex);
					discrepancies.add(new Discrepancy(key, ex.getMessage()));
				}
			}

			/*
			 * Step 4: Check that everything that is in OBS is also in the Data Lifecycle
			 * index
			 */
			checkDataLifecycleIndex(obsResults.values(), family, discrepancies);

			/*
			 * Step 5: Presenting the results of the discrepancies check
			 */
			if (discrepancies.isEmpty()) {
				reporting.end(new ReportingMessage("No discrepancies found"));
			} else {
				reporting.error(new ReportingMessage("Discrepancies found for following products: {}",
						buildProductList(discrepancies)));
			}

			LOGGER.info("Found {} discrepancies for family '{}'", discrepancies.size(), family);
			return discrepancies.size();
		} catch (final MetadataQueryException e) {
			reporting.error(
					new ReportingMessage("Error occured while performing metadata catalog query task [code {}] {}",
							e.getCode().getCode(), e.getLogMessage()));
			throw e;
		} catch (final Exception e) {
			reporting.error(new ReportingMessage(LogUtils.toString(e)));
			throw e;
		}

	}

	private void checkDataLifecycleIndex(Collection<ObsObject> obsObjects, ProductFamily family,
			List<Discrepancy> discrepancies) throws DataLifecycleMetadataRepositoryException {

		LOGGER.info("Check that everything that is in OBS is also in the Data Lifecycle index");

		for (String key : extractRealKeysForDataLifecycle(obsObjects, family)) {
			if (isNotPresentInDataLifecycleIndex(key)) {
				LOGGER.trace("Exists in OBS, but not in Data Lifecycle Index: {}", key);
				final Discrepancy discrepancy = new Discrepancy(key,
						"Exists in OBS, but not in Data Lifecycle Index");
				discrepancies.add(discrepancy);
			}
		}
	}

	private boolean isNotPresentInDataLifecycleIndex(String key) throws DataLifecycleMetadataRepositoryException {

		DataLifecycleMetadata.FIELD_NAME field = null;

		if (key.endsWith(".zip")) {
			field = DataLifecycleMetadata.FIELD_NAME.PATH_IN_COMPRESSED_STORAGE;
		} else {
			field = DataLifecycleMetadata.FIELD_NAME.PATH_IN_UNCOMPRESSED_STORAGE;
		}

		List<DataLifecycleMetadata> result = lifecycleMetadataRepo.findWithFilters(
				Collections.singletonList(
						new DataLifecycleTextFilter(field, DataLifecycleTextFilter.Function.EQUALS, key)),
				Optional.empty(), Optional.empty(), Collections.emptyList());
		return result.isEmpty();
	}
	
	Set<String> extractRealKeysForObsValidation(final Collection<ObsObject> obsResults, final ProductFamily family) {
		
		final Set<String> realProducts = new HashSet<>();
		for (final ObsObject obsResult : obsResults) {
			final String key = obsResult.getKey();
			final int index = key.indexOf("/");
			String realKey = null;

			if (family == ProductFamily.EDRS_SESSION) {
				realKey = key;
			} else if (index != -1) {
				realKey = key.substring(0, index);
			} else {
				realKey = key;
			}
			LOGGER.trace("key is {}", realKey);
			realProducts.add(realKey);
		}
		return realProducts;
	}

	Set<String> extractRealKeysForMDC(final Collection<ObsObject> obsResults, final ProductFamily family) {
		final Set<String> realProducts = new HashSet<>();
		for (final ObsObject obsResult : obsResults) {
			final String key = obsResult.getKey();
			final int index = key.indexOf("/");
			String realKey = null;

			if (family == ProductFamily.EDRS_SESSION) {
				realKey = key;
				/*
				 * EDRS_Sessions are just queried on raw and not containg DSIB. So we are
				 * removing them from the check
				 */
				if (realKey.endsWith("DSIB.xml")) {
					LOGGER.debug("Ignoring DSIB file: {}", realKey);
					continue;
				}
			} else if (index != -1) {
				realKey = key.substring(0, index);
			} else {
				realKey = key;
			}
			if (key.endsWith(".zip")) {
				// Special case zipped products. The MDC key does not contain the zip!
				realKey = realKey.substring(0, key.lastIndexOf(".zip"));
			}
			LOGGER.trace("key is {}", realKey);
			realProducts.add(realKey);
		}
		return realProducts;
	}
	
	Set<String> extractRealKeysForDataLifecycle(final Collection<ObsObject> obsResults, final ProductFamily family) {
		final Set<String> realProducts = new HashSet<>();
		for (final ObsObject obsResult : obsResults) {
			final String key = obsResult.getKey();
			final int index = key.indexOf("/");
			String realKey = null;

			if (family == ProductFamily.EDRS_SESSION) {
				realKey = key;
				/*
				 * EDRS_Sessions are just queried on raw and not containg DSIB. So we are
				 * removing them from the check
				 */
				if (realKey.endsWith("DSIB.xml")) {
					LOGGER.debug("Ignoring DSIB file: {}", realKey);
					continue;
				}
			} else if (index != -1) {
				realKey = key.substring(0, index);
			} else {
				realKey = key;
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

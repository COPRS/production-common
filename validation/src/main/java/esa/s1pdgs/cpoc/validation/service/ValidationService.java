package esa.s1pdgs.cpoc.validation.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.metadata.model.SearchMetadata;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;
import esa.s1pdgs.cpoc.report.LoggerReporting;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.validation.service.metadata.MetadataService;

@Service
public class ValidationService {
	private static final Logger LOGGER = LogManager.getLogger(ValidationService.class);

	private final MetadataService metadataService;

	private final ObsClient obsClient;

	@Autowired
	public ValidationService(MetadataService metadataService, ObsClient obsClient) {
		this.metadataService = metadataService;
		this.obsClient = obsClient;
	}

	public boolean checkConsistencyForFamilyAndTimeFrame(ProductFamily family, String intervalStart, String intervalEnd)
			throws MetadataQueryException, SdkClientException {

		final Reporting.Factory reportingFactory = new LoggerReporting.Factory(LOGGER, "ValidationService");
		final Reporting reportingValidation = reportingFactory.newReporting(0);
		reportingValidation
				.reportStart(String.format("Starting validation task from %s to %s", intervalStart, intervalEnd));

		final Reporting reportingMetadata = reportingFactory.newReporting(1);
		List<SearchMetadata> metadataResults = null;
		try {
			reportingMetadata.reportStart("Gathering discrepancies in metadata catalog");
			metadataResults = metadataService.query(family, null, intervalStart, intervalEnd);
			if (metadataResults == null) {
				// set to empty list
				metadataResults = new ArrayList<>();
			}
			LOGGER.info("Metadata query for family '{}' returned {} results", family, metadataResults.size());

		} catch (MetadataQueryException e) {
			reportingMetadata.reportError("Error occured while performing metadata catalog query task [code {}] {}",
					e.getCode().getCode(), e.getLogMessage());
			throw e;
		}

		final Reporting reportingObs = reportingFactory.newReporting(2);
		Map<String, ObsObject> obsResults = null;
		try {
			reportingObs.reportStart("Gathering discrepancies in OBS");
			LocalDateTime localDateTimeStart = LocalDateTime.parse(intervalStart, DateUtils.METADATA_DATE_FORMATTER);
			LocalDateTime localDateTimeEnd = LocalDateTime.parse(intervalEnd, DateUtils.METADATA_DATE_FORMATTER);

			Date startDate = Date.from(localDateTimeStart.atZone(ZoneId.of("UTC")).toInstant());
			Date endDate = Date.from(localDateTimeEnd.atZone(ZoneId.of("UTC")).toInstant());

			obsResults = obsClient.listInterval(family, startDate, endDate);
			LOGGER.info("OBS query for family '{}' returned {} results", family, obsResults.size());

		} catch (SdkClientException | DateTimeParseException e) {
			reportingObs.reportError("Error occured while performing obs query task: {}", e.getMessage());
			throw e;
		}
		
		List<String> metadataDiscrepancies = new ArrayList<>();
		List<String> obsDiscrepancies = new ArrayList<>();

		for (SearchMetadata smd : metadataResults) {
			if (obsResults.get(smd.getKeyObjectStorage()) == null) {
				obsDiscrepancies.add(smd.getKeyObjectStorage());
				LOGGER.info("Product {} does exist in metadata catalog, but not in OBS", smd.getKeyObjectStorage());
			} else {
				LOGGER.debug("Product {} does exist in metadata catalog and OBS", smd.getKeyObjectStorage());
				obsResults.remove(smd.getKeyObjectStorage());
			}
		}

		if (obsResults.size() > 0) {
			LOGGER.info("Found {} products that exist in OBS, but not in metdata catalog", obsResults.size());
			for (ObsObject product : obsResults.values()) {
				metadataDiscrepancies.add(product.getKey());
				LOGGER.info("Product {} does exist in OBS, but not in metadata catalog", product.getKey());
			}
		}
		
		if (metadataDiscrepancies.isEmpty()) {
			reportingMetadata.reportStop("No discrepancies found in metadata catalog");
		} else {
			reportingMetadata.reportError("Product(s) not present in metadata catalog: {}",
					buildProductList(metadataDiscrepancies));
		}

		if (obsDiscrepancies.isEmpty()) {
			reportingObs.reportStop("No discepancies found in OBS");
		} else {
			reportingObs.reportError("Product(s) not present in OBS: {}", buildProductList(obsDiscrepancies));
		}

		if (metadataDiscrepancies.isEmpty() && obsDiscrepancies.isEmpty()) {
			reportingValidation.reportStop("No discrepancy found");
			return true;

		} else {
			int discrepancies = metadataDiscrepancies.size() + obsDiscrepancies.size();
			reportingValidation.reportError("Discrepancy found for {} product(s)", discrepancies);
			return false;
		}
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

package esa.s1pdgs.cpoc.odip.service;


import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.appcatalog.common.OnDemandProcessingRequest;
import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.model.SearchMetadata;
import esa.s1pdgs.cpoc.mqi.model.queue.OnDemandEvent;
import esa.s1pdgs.cpoc.odip.config.OdipConfigurationProperties;
import esa.s1pdgs.cpoc.odip.kafka.producer.SubmissionClient;

@Service
public class OnDemandService {
	public static final Logger LOGGER = LogManager.getLogger(OnDemandService.class);

	private MetadataClient metadataClient;

	private OdipConfigurationProperties properties;

	private final SubmissionClient kafkaSubmissionClient;
	private final AppStatus status;

	@Autowired
	public OnDemandService(final OdipConfigurationProperties properties, final SubmissionClient kafkaSubmissionClient,
			final AppStatus status, final MetadataClient metadataClient) {
		this.properties = properties;
		this.kafkaSubmissionClient = kafkaSubmissionClient;
		this.status = status;
		this.metadataClient = metadataClient;
	}

	public void submit(final OnDemandProcessingRequest request) {
		LOGGER.info("(Re-)Submitting following request {}", request);

		String productName = request.getProductName();
		final String productionType = request.getProductionType();
		final String mode = request.getMode();
		final boolean debug = request.isDebug();
		
		assertNotNull("product name", productName);
		assertNotNull("production type", productionType);
		assertNotNull("mode", mode);
		
		final String keyObjectStorage = productName;
		if ("AIOP".equalsIgnoreCase(productionType)) {
			productName = productName.substring(0, productName.indexOf('/'));
		}

		final ProductFamily productFamily = ProductFamily
				.valueOf(properties.getProductionTypeToProductFamily().get(productionType));

		final OnDemandEvent event = new OnDemandEvent(productFamily, keyObjectStorage, productName, productionType, mode);
		event.setDebug(debug);

		try {
			LOGGER.info("Querying mdc with product family '{}' and product name '{}'...", productFamily.name(), request.getProductName());
			final SearchMetadata metadata = this.metadataClient.queryByFamilyAndProductName(productFamily.name(),
					request.getProductName());
			LOGGER.info("Query result: {}", metadata);

			final Map<String, Object> metadataAsMap = new HashMap<>();
			// TODO check if all metadata is provided
//			metadataAsMap.put("productName", metadata.getProductName());
//			metadataAsMap.put("productType", metadata.getProductType());
//			metadataAsMap.put("satelliteId", metadata.getSatelliteId());
//			metadataAsMap.put("missionId", metadata.getMissionId());		
//			metadataAsMap.put("acquistion", metadata.getSwathtype());
//			metadataAsMap.put("stationCode", metadata.getStationCode());
//			metadataAsMap.put("keyObjectStorage", metadata.getKeyObjectStorage());
			event.setProductType(metadata.getAdditionalProperties().getOrDefault("productType", "NOT_KNOWN"));
			metadataAsMap.putAll(metadata.getAdditionalProperties());
			if (!metadata.getFootprint().isEmpty()) {
				metadataAsMap.put("footprint", metadata.getFootprint());
			}
			metadataAsMap.put("startTime", metadata.getValidityStart());
			metadataAsMap.put("stopTime", metadata.getValidityStop());
			metadataAsMap.put("processMode", mode);
			metadataAsMap.put("timeliness", properties.getTimeliness());
			event.setMetadata(metadataAsMap);

		} catch (final MetadataQueryException e) {
			LOGGER.info("Querying mdc failed", e);
			throw new RuntimeException(e);
		}

		kafkaSubmissionClient.resubmit(event, status);
	}

	static final void assertNotNull(final String name, final String value) 
			throws IllegalArgumentException {
		if (value == null || value.isEmpty()) {
			throw new IllegalArgumentException(
					String.format("%s must not be null or empty", name)
			);
		}
	}
}

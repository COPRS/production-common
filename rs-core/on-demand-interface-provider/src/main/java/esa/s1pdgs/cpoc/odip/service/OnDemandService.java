package esa.s1pdgs.cpoc.odip.service;


import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.appcatalog.common.OnDemandProcessingRequest;
import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.common.utils.Exceptions;
import esa.s1pdgs.cpoc.message.MessageProducer;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.model.SearchMetadata;
import esa.s1pdgs.cpoc.mqi.model.queue.OnDemandEvent;
import esa.s1pdgs.cpoc.odip.config.OdipConfigurationProperties;
import esa.s1pdgs.cpoc.odip.config.TopicConfig;

@Service
public class OnDemandService {
	public static final Logger LOGGER = LogManager.getLogger(OnDemandService.class);

	private MetadataClient metadataClient;

	private OdipConfigurationProperties properties;

	private final TopicConfig topicConfig;
	private final MessageProducer<OnDemandEvent> messageProducer;
	private final AppStatus status;

	@Autowired
	public OnDemandService(final OdipConfigurationProperties properties,
						   final AppStatus status, final MetadataClient metadataClient, final TopicConfig topicConfig, final MessageProducer<OnDemandEvent> messageProducer) {
		this.properties = properties;
		this.topicConfig = topicConfig;
		this.messageProducer = messageProducer;
		this.status = status;
		this.metadataClient = metadataClient;
	}

	public OnDemandEvent submit(final OnDemandProcessingRequest request) {
		LOGGER.info("(Re-)Submitting following request {}", request);

		final String productName = request.getProductName();
		final ApplicationLevel productionType = ApplicationLevel.valueOf(request.getProductionType());
		final String mode = request.getMode();
		final boolean debug = request.isDebug();
		final String tasktableName = request.getTasktableName();
		final String outputProductType = request.getOutputProductType();

		assertNotNull("product name", productName);
		assertNotNull("mode", mode);
		
		final String keyObjectStorage = productName;
		final ProductFamily productFamily = ProductFamily
				.valueOf(properties.getProductionTypeToProductFamily().get(productionType.toString()));

		final OnDemandEvent event = new OnDemandEvent(productFamily, keyObjectStorage, productName, productionType, mode);
		event.setDebug(debug);
		event.setTasktableName(tasktableName);
		event.setOutputProductType(outputProductType);

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

		resubmit(event, status);
		
		return event;
	}

	private void resubmit(final OnDemandEvent event, final AppStatus appStatus) {
		try {
			LOGGER.info("(Re-)Submitting following message '{}' to topic '{}'", event, topicConfig.getTopic());
			messageProducer.send(topicConfig.getTopic(), event);
		} catch (final Exception e) {
			appStatus.getStatus().setFatalError();
			throw new RuntimeException(
					String.format(
							"Error (re)starting request on topic '%s': %s",
							topicConfig.getTopic(),
							Exceptions.messageOf(e)
					),
					e
			);
		}
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

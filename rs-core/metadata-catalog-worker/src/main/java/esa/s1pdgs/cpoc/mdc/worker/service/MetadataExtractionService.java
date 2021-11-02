package esa.s1pdgs.cpoc.mdc.worker.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.mdc.worker.config.MdcWorkerConfigurationProperties;
import esa.s1pdgs.cpoc.mdc.worker.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.mdc.worker.config.TriggerConfigurationProperties;
import esa.s1pdgs.cpoc.mdc.worker.config.TriggerConfigurationProperties.CategoryConfig;
import esa.s1pdgs.cpoc.mdc.worker.extraction.MetadataExtractor;
import esa.s1pdgs.cpoc.mdc.worker.extraction.MetadataExtractorFactory;
import esa.s1pdgs.cpoc.mdc.worker.extraction.report.MetadataExtractionReportingOutput;
import esa.s1pdgs.cpoc.mdc.worker.extraction.report.MetadataExtractionReportingOutput.EffectiveDownlink;
import esa.s1pdgs.cpoc.mdc.worker.status.AppStatusImpl;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.mqi.client.MessageFilter;
import esa.s1pdgs.cpoc.mqi.client.MqiClient;
import esa.s1pdgs.cpoc.mqi.client.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.client.MqiListener;
import esa.s1pdgs.cpoc.mqi.client.MqiMessageEventHandler;
import esa.s1pdgs.cpoc.mqi.client.MqiPublishingJob;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.mqi.model.queue.util.CatalogEventAdapter;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingOutput;
import esa.s1pdgs.cpoc.report.ReportingUtils;

@Service
public class MetadataExtractionService implements MqiListener<CatalogJob> {
	private static final Logger LOG = LogManager.getLogger(MetadataExtractionService.class);

	public static final String KEY_PRODUCT_SENSING_START = "product_sensing_start_date";
	public static final String KEY_PRODUCT_SENSING_STOP = "product_sensing_stop_date";
	
	public static final String QUALITY_CORRUPTED_ELEMENT_COUNT = "corrupted_element_count_long";
	public static final String QUALITY_MISSING_ELEMENT_COUNT = "missing_element_count_long";
	
	private final AppStatusImpl appStatus;
	private final ErrorRepoAppender errorAppender;
	private final ProcessConfiguration processConfiguration;
	private final EsServices esServices;
	private final MqiClient mqiClient;
	private final List<MessageFilter> messageFilter;
	private final MdcWorkerConfigurationProperties properties;
	private final MetadataExtractorFactory extractorFactory;
	private final TriggerConfigurationProperties triggerConfiguration;

	@Autowired
	public MetadataExtractionService(final AppStatusImpl appStatus, final ErrorRepoAppender errorAppender,
			final ProcessConfiguration processConfiguration, final EsServices esServices, final MqiClient mqiClient,
			final List<MessageFilter> messageFilter, final MdcWorkerConfigurationProperties properties,
			final MetadataExtractorFactory extractorFactory,
			final TriggerConfigurationProperties triggerConfiguration) {
		this.appStatus = appStatus;
		this.errorAppender = errorAppender;
		this.processConfiguration = processConfiguration;
		this.esServices = esServices;
		this.mqiClient = mqiClient;
		this.messageFilter = messageFilter;
		this.properties = properties;
		this.extractorFactory = extractorFactory;
		this.triggerConfiguration = triggerConfiguration;
	}

	@PostConstruct
	public void init() {
		final Map<ProductCategory, CategoryConfig> entries = triggerConfiguration.getProductCategories();
		final ExecutorService service = Executors.newFixedThreadPool(entries.size());

		for (final Map.Entry<ProductCategory, CategoryConfig> entry : entries.entrySet()) {
			service.execute(newConsumerFor(entry.getKey(), entry.getValue()));
		}
	}

	@Override
	public final MqiMessageEventHandler onMessage(final GenericMessageDto<CatalogJob> message) throws Exception {
		final CatalogJob catJob = message.getBody();
		final Reporting reporting = ReportingUtils
				.newReportingBuilder(
						MissionId.fromFamilyOrFileName(catJob.getProductFamily(), catJob.getKeyObjectStorage()))
				.predecessor(catJob.getUid()).newReporting("MetadataExtraction");

		reporting.begin(ReportingUtils.newFilenameReportingInputFor(catJob.getProductFamily(), catJob.getProductName()),
				new ReportingMessage("Starting metadata extraction"));
		return new MqiMessageEventHandler.Builder<CatalogEvent>(ProductCategory.CATALOG_EVENT)
				.onSuccess(res -> {
					// S1PRO-2337
					Map<String, String> quality = new LinkedHashMap<>();
					final GenericPublicationMessageDto<CatalogEvent> pub = res.get(0);
					if (pub.getFamily() != ProductFamily.EDRS_SESSION) {
						final CatalogEventAdapter eventAdapter = CatalogEventAdapter.of(pub);
						if (eventAdapter.qualityNumOfMissingElements() != null) {
							quality.put(QUALITY_MISSING_ELEMENT_COUNT, eventAdapter.qualityNumOfMissingElements().toString());
						}
						if (eventAdapter.qualityNumOfCorruptedElements() != null) {
							quality.put(QUALITY_CORRUPTED_ELEMENT_COUNT, eventAdapter.qualityNumOfCorruptedElements().toString());
						}
					} 
					reporting.end(reportingOutput(res), new ReportingMessage("End metadata extraction"),
							quality);
					
				})
				.onWarning(res -> reporting.warning(reportingOutput(res), new ReportingMessage("End metadata extraction")))
				.onError(e -> reporting
						.error(new ReportingMessage("Metadata extraction failed: %s", LogUtils.toString(e))))
				.publishMessageProducer(() -> handleMessage(message, reporting)).newResult();
	}

	@Override
	public final void onTerminalError(final GenericMessageDto<CatalogJob> message, final Exception error) {
		LOG.error(error);
		errorAppender.send(
				new FailedProcessingDto(processConfiguration.getHostname(), new Date(), error.getMessage(), message));
	}
	
	@Override
	public void onWarning(final GenericMessageDto<CatalogJob> message, final String warningMessage) {
		LOG.warn(warningMessage);
				
		final FailedProcessingDto failedProcessing = new FailedProcessingDto(
        		processConfiguration.getHostname(),
        		new Date(), 
        		String.format("Warning on handling CatalogJob message %s: %s", message.getId(), warningMessage), 
        		message
        );
        errorAppender.send(failedProcessing);
	}
	

	private String determineOutputKeyDependentOnProductFamilyAndTimeliness(final CatalogEvent event) {

		String outputKey = "";

		final Object timeliness = event.getMetadata().get("timeliness");
		if (timeliness != null && !timeliness.toString().isEmpty()) {
			outputKey = event.getProductFamily().name() + "@" + timeliness;
		} else {
			outputKey = event.getProductFamily().name();
		}

		return outputKey;
	}

	private final MqiConsumer<CatalogJob> newConsumerFor(final ProductCategory category, final CategoryConfig config) {
		LOG.debug("Creating MQI consumer for category {} using {}", category, config);
		return new MqiConsumer<CatalogJob>(mqiClient, category, this, messageFilter, config.getFixedDelayMs(),
				config.getInitDelayPollMs(), appStatus);
	}

	private final CatalogEvent toCatalogEvent(final CatalogJob catJob, final JSONObject metadata) {
		final CatalogEvent catEvent = new CatalogEvent();
		catEvent.setProductName(catJob.getProductName());
		catEvent.setKeyObjectStorage(catJob.getKeyObjectStorage());
		catEvent.setProductFamily(catJob.getProductFamily());
		catEvent.setProductType(metadata.getString("productType"));
		catEvent.setMetadata(metadata.toMap());
		return catEvent;
	}

	private final MqiPublishingJob<CatalogEvent> handleMessage(final GenericMessageDto<CatalogJob> message,
			final Reporting reporting) throws Exception {
		final CatalogJob catJob = message.getBody();
		final String productName = catJob.getProductName();
		final ProductFamily family = catJob.getProductFamily();
		final ProductCategory category = ProductCategory.of(family);

		final MetadataExtractor extractor = extractorFactory.newMetadataExtractorFor(category,
				properties.getProductCategories().get(category));
		final JSONObject metadata = extractor.extract(reporting, message);

		// TODO move to extractor
		if (null != catJob.getTimeliness() && !metadata.has("timeliness")) {
			metadata.put("timeliness", catJob.getTimeliness());
		}

		// TODO move to extractor
		if (!metadata.has("insertionTime")) {
			metadata.put("insertionTime", DateUtils.formatToMetadataDateTimeFormat(LocalDateTime.now()));
		}
		LOG.debug("Metadata extracted: {} for product: {}", metadata, productName);
		
		String warningMessage = 
				esServices.createMetadataWithRetries(metadata, productName, properties.getProductInsertion().getMaxRetries(),
						properties.getProductInsertion().getTempoRetryMs());

		final CatalogEvent event = toCatalogEvent(message.getBody(), metadata);
		event.setUid(reporting.getUid());
		final GenericPublicationMessageDto<CatalogEvent> messageDto = new GenericPublicationMessageDto<CatalogEvent>(
				message.getId(), event.getProductFamily(), event);
		messageDto.setInputKey(message.getInputKey());
		messageDto.setOutputKey(determineOutputKeyDependentOnProductFamilyAndTimeliness(event));
		return new MqiPublishingJob<CatalogEvent>(Collections.singletonList(messageDto), warningMessage);
	}

	private final ReportingOutput reportingOutput(final List<GenericPublicationMessageDto<CatalogEvent>> pubs) {
		final GenericPublicationMessageDto<CatalogEvent> pub = pubs.get(0);
		final CatalogEventAdapter eventAdapter = CatalogEventAdapter.of(pub);

		final MetadataExtractionReportingOutput output = new MetadataExtractionReportingOutput();

		// S1PRO-1678: trace sensing start/stop
		final String productSensingStartDate = eventAdapter.productSensingStartDate();
		if (!CatalogEventAdapter.NOT_DEFINED.equals(productSensingStartDate)) {
			output.withSensingStart(productSensingStartDate);
		}
		final String productSensingStopDate = eventAdapter.productSensingStopDate();
		if (!CatalogEventAdapter.NOT_DEFINED.equals(productSensingStopDate)) {
			output.withSensingStop(productSensingStopDate);
		}

		// S1PRO-1247: deal with segment scenario
		if (pub.getFamily() == ProductFamily.L0_SEGMENT) {
			output.withConsolidation(eventAdapter.productConsolidation())
					.withSensingConsolidation(eventAdapter.productSensingConsolidation());
		}

		// S1PRO-1840: report channel identifier
		if (pub.getFamily() == ProductFamily.EDRS_SESSION) {
			output.setChannelIdentifierShort(eventAdapter.channelId());
		}

		// S1PRO-1840: report raw count for DSIB files only
		// S1PRO-2036: report station string, start time and stop time for DSIB files only, for all other report mission identifier and type
		if (pub.getFamily() == ProductFamily.EDRS_SESSION && EdrsSessionFileType.SESSION.name().equalsIgnoreCase(eventAdapter.productType())) {
			final List<String> rawNames = eventAdapter.rawNames();
			output.setRawCountShort(rawNames != null ? rawNames.size() : 0);
			output.setStationString(eventAdapter.stationCode());
			final EffectiveDownlink effectiveDownlink = new EffectiveDownlink();
			effectiveDownlink.setStartDate(eventAdapter.startTime());
			effectiveDownlink.setStopDate(eventAdapter.stopTime());
			output.setEffectiveDownlink(effectiveDownlink);
		} else {
			output.setMissionIdentifierString(eventAdapter.missionId());
			output.setTypeString(pub.getFamily().name());
		}

		return output.build();
	}

}

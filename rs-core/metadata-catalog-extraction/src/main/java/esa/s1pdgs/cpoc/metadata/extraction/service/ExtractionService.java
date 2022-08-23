package esa.s1pdgs.cpoc.metadata.extraction.service;

import static esa.s1pdgs.cpoc.metadata.extraction.config.TimelinessConfiguration.FAST24;
import static esa.s1pdgs.cpoc.metadata.extraction.config.TimelinessConfiguration.FILE_PATTERN_S1_GP_HK;
import static esa.s1pdgs.cpoc.metadata.extraction.config.TimelinessConfiguration.NRT;
import static esa.s1pdgs.cpoc.metadata.extraction.config.TimelinessConfiguration.PT;
import static esa.s1pdgs.cpoc.metadata.extraction.config.TimelinessConfiguration.S1_FAST24;
import static esa.s1pdgs.cpoc.metadata.extraction.config.TimelinessConfiguration.S1_NRT;
import static esa.s1pdgs.cpoc.metadata.extraction.config.TimelinessConfiguration.S1_PT;
import static esa.s1pdgs.cpoc.metadata.extraction.config.TimelinessConfiguration.S1_SESSION;
import static esa.s1pdgs.cpoc.metadata.extraction.config.TimelinessConfiguration.S2_L0;
import static esa.s1pdgs.cpoc.metadata.extraction.config.TimelinessConfiguration.S2_L1;
import static esa.s1pdgs.cpoc.metadata.extraction.config.TimelinessConfiguration.S2_L2;
import static esa.s1pdgs.cpoc.metadata.extraction.config.TimelinessConfiguration.S2_SESSION;
import static esa.s1pdgs.cpoc.metadata.extraction.config.TimelinessConfiguration.S3_NRT;
import static esa.s1pdgs.cpoc.metadata.extraction.config.TimelinessConfiguration.S3_NTC;
import static esa.s1pdgs.cpoc.metadata.extraction.config.TimelinessConfiguration.S3_STC;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import esa.s1pdgs.cpoc.common.CommonConfigurationProperties;
import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataMalformedException;
import esa.s1pdgs.cpoc.common.metadata.PathMetadataExtractor;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.metadata.extraction.config.MdcWorkerConfigurationProperties;
import esa.s1pdgs.cpoc.metadata.extraction.config.TimelinessConfiguration;
import esa.s1pdgs.cpoc.metadata.extraction.service.elastic.EsServices;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.MetadataExtractor;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.MetadataExtractorFactory;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.ProductMetadata;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.report.MetadataExtractionReportingOutput;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.report.MetadataExtractionReportingOutput.EffectiveDownlink;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.report.ProductMetadataCustomObjectFiller;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.mqi.model.queue.util.CatalogEventAdapter;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingOutput;
import esa.s1pdgs.cpoc.report.ReportingUtils;

public class ExtractionService implements Function<CatalogJob, CatalogEvent> {
	private static final Logger LOG = LogManager.getLogger(ExtractionService.class);

	public static final String QUALITY_CORRUPTED_ELEMENT_COUNT = "corrupted_element_count_long";
	public static final String QUALITY_MISSING_ELEMENT_COUNT = "missing_element_count_long";

	private final CommonConfigurationProperties commonProperties;
	private final EsServices esServices;
	private final MdcWorkerConfigurationProperties properties;
	private final MetadataExtractorFactory extractorFactory;
	private final TimelinessConfiguration timelinessConfig;

	@Autowired
	public ExtractionService(final CommonConfigurationProperties commonProperties,
			final EsServices esServices, final MdcWorkerConfigurationProperties properties,
			final MetadataExtractorFactory extractorFactory, final TimelinessConfiguration timelinessConfig) {
		this.commonProperties = commonProperties;
		this.esServices = esServices;
		this.properties = properties;
		this.extractorFactory = extractorFactory;
		this.timelinessConfig = timelinessConfig;
	}

	@Override
	public CatalogEvent apply(CatalogJob catalogJob) {
		MissionId mission = null;

		if (catalogJob.getProductFamily().isSessionFamily()) {
			PathMetadataExtractor mExtractor = MetadataExtractorFactory
					.newPathMetadataExtractor(properties.getProductCategories().get(ProductCategory.EDRS_SESSIONS));
			mission = MissionId
					.valueOf(mExtractor.metadataFrom(catalogJob.getMetadataRelativePath()).get(MissionId.FIELD_NAME));
		} else {
			mission = MissionId.fromFileName(catalogJob.getKeyObjectStorage());
		}

		final Reporting reporting = ReportingUtils.newReportingBuilder(mission)
				.rsChainName(commonProperties.getRsChainName())
				.rsChainVersion(commonProperties.getRsChainVersion())
				.predecessor(catalogJob.getUid())
				.newReporting("MetadataExtraction");

		reporting.begin(
				ReportingUtils.newFilenameReportingInputFor(catalogJob.getProductFamily(), catalogJob.getProductName()),
				new ReportingMessage("Starting metadata extraction"));

		CatalogEvent result;
		try {
			result = handleMessage(catalogJob, reporting);
		} catch (Exception e) {
			reporting.error(new ReportingMessage("Metadata extraction failed: %s", LogUtils.toString(e)));
			throw new RuntimeException(e);
		}

		// S1PRO-2337
		Map<String, String> quality = new LinkedHashMap<>();
		if (result.getProductFamily() != ProductFamily.EDRS_SESSION) {
			final CatalogEventAdapter eventAdapter = CatalogEventAdapter.of(result);
			if (eventAdapter.qualityNumOfMissingElements() != null) {
				quality.put(QUALITY_MISSING_ELEMENT_COUNT, eventAdapter.qualityNumOfMissingElements().toString());
			}
			if (eventAdapter.qualityNumOfCorruptedElements() != null) {
				quality.put(QUALITY_CORRUPTED_ELEMENT_COUNT, eventAdapter.qualityNumOfCorruptedElements().toString());
			}
		}
		reporting.end(reportingOutput(result, mission), new ReportingMessage("End metadata extraction"), quality);

		LOG.info("Sucessfully processed metadata extraction for {}", result.getProductName());

		return result;
	}

	private final CatalogEvent handleMessage(final CatalogJob catJob, final Reporting reporting) throws Exception {
		final String productName = catJob.getProductName();
		final ProductFamily family = catJob.getProductFamily();
		final ProductCategory category = ProductCategory.of(family);

		final MetadataExtractor extractor = extractorFactory.newMetadataExtractorFor(category,
				properties.getProductCategories().get(category));
		final ProductMetadata metadata = extractor.extract(reporting, catJob);

		// TODO move to extractor
		if (null != catJob.getTimeliness() && !metadata.has("timeliness")) {
			metadata.put("timeliness", catJob.getTimeliness());
		}

		// TODO move to extractor
		if (!metadata.has("insertionTime")) {
			metadata.put("insertionTime", DateUtils.formatToMetadataDateTimeFormat(LocalDateTime.now()));
		}

		// RS-248: Adding t0PdgsDate into metadata
		if (catJob.getT0PdgsDate() != null) {
			metadata.put("t0PdgsDate", DateUtils.formatToMetadataDateTimeFormat(
					catJob.getT0PdgsDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()));
		}

		LOG.debug("Metadata extracted: {} for product: {}", metadata, productName);

		esServices.createMetadataWithRetries(metadata, productName,
				properties.getProductInsertion().getMaxRetries(), properties.getProductInsertion().getTempoRetryMs());

		final CatalogEvent event = toCatalogEvent(catJob, metadata);
		event.setUid(reporting.getUid());
		return event;
	}

	private final CatalogEvent toCatalogEvent(final CatalogJob catJob, final ProductMetadata metadata)
				throws MetadataMalformedException {
		final CatalogEvent catEvent = new CatalogEvent();
		String satelliteId;
		try {
			satelliteId = metadata.getString("satelliteId");
		} catch (MetadataMalformedException e) {
			satelliteId = catJob.getSatelliteId();
		}

		catEvent.setMetadata(metadata.asMap());
		catEvent.setMissionId(catJob.getMissionId());
		catEvent.setSatelliteId(satelliteId);
		catEvent.setMetadataProductName(catJob.getProductName());
		catEvent.setKeyObjectStorage(catJob.getKeyObjectStorage());
		catEvent.setStoragePath(catJob.getStoragePath());
		catEvent.setProductFamily(catJob.getProductFamily());
		catEvent.setMetadataProductType(metadata.getString("productType"));
		if (catJob.getT0PdgsDate() != null) {
			catEvent.getAdditionalFields().put("t0PdgsDate", catJob.getT0PdgsDate());
		}

		return catEvent;
	}

	private final ReportingOutput reportingOutput(final CatalogEvent catalogEvent, MissionId mission) {
		final CatalogEventAdapter eventAdapter = CatalogEventAdapter.of(catalogEvent);

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
		if (catalogEvent.getProductFamily() == ProductFamily.L0_SEGMENT) {
			output.withConsolidation(eventAdapter.productConsolidation())
					.withSensingConsolidation(eventAdapter.productSensingConsolidation());
		}

		// S1PRO-1840: report channel identifier
		if (catalogEvent.getProductFamily() == ProductFamily.EDRS_SESSION) {
			output.setChannelIdentifierShort(eventAdapter.channelId());
		}

		// S1PRO-1840: report raw count for DSIB files only
		// S1PRO-2036: report station string, start time and stop time for DSIB files
		// only, for all other report mission identifier and type
		if (catalogEvent.getProductFamily() == ProductFamily.EDRS_SESSION
				&& EdrsSessionFileType.SESSION.name().equalsIgnoreCase(eventAdapter.productType())) {
			final List<String> rawNames = eventAdapter.rawNames();
			output.setRawCountShort(rawNames != null ? rawNames.size() : 0);
			output.setStationString(eventAdapter.stationCode());
			final EffectiveDownlink effectiveDownlink = new EffectiveDownlink();
			effectiveDownlink.setStartDate(eventAdapter.startTime());
			effectiveDownlink.setStopDate(eventAdapter.stopTime());
			output.setEffectiveDownlink(effectiveDownlink);
		} else {
			output.setMissionIdentifierString(eventAdapter.missionId());
			output.setTypeString(catalogEvent.getProductFamily().name());
		}
		
		// RS-407
		new ProductMetadataCustomObjectFiller(catalogEvent, output).fillCustomObject();
		
		// RS-444
		output.setEndToEndProduct(catalogEvent.getProductFamily().isEndToEndFamily());
		
		fillTimelinessValues(catalogEvent, mission, output);

		return output.build();
	}
	
	private void fillTimelinessValues(CatalogEvent catalogEvent, MissionId mission,
			MetadataExtractionReportingOutput output) {

		switch (catalogEvent.getProductFamily()) {
		case S2_L0_DS:
		case S2_L0_GR:
			output.setTimelinessName(S2_L0);
			output.setTimelinessValueSeconds(timelinessConfig.get(S2_L0));
			break;
		case S2_HKTM:
		case S2_SAD:
			output.setTimelinessName(S2_SESSION);
			output.setTimelinessValueSeconds(timelinessConfig.get(S2_SESSION));
			break;
		case S2_L1A_DS:
		case S2_L1A_GR:
		case S2_L1B_DS:
		case S2_L1B_GR:
		case S2_L1C_DS:
		case S2_L1C_TL:
		case S2_L1C_TC:
			output.setTimelinessName(S2_L1);
			output.setTimelinessValueSeconds(timelinessConfig.get(S2_L1));
			break;
		case S2_L2A_DS:
		case S2_L2A_TL:
			output.setTimelinessName(S2_L2);
			output.setTimelinessValueSeconds(timelinessConfig.get(S2_L2));
			break;
		case S3_L0:
		case S3_GRANULES:
		case S3_CAL:
		case S3_L1_NRT:
		case S3_L2_NRT:
			output.setTimelinessName(S3_NRT);
			output.setTimelinessValueSeconds(timelinessConfig.get(S3_NRT));
			break;
		case S3_L1_NTC:
		case S3_L2_NTC:
			output.setTimelinessName(S3_NTC);
			output.setTimelinessValueSeconds(timelinessConfig.get(S3_NTC));
			break;
		case S3_L1_STC:
		case S3_L2_STC:
			output.setTimelinessName(S3_STC);
			output.setTimelinessValueSeconds(timelinessConfig.get(S3_STC));
			break;
		default:
			if (mission == MissionId.S1) {
				Matcher matcher = FILE_PATTERN_S1_GP_HK.matcher(catalogEvent.getProductName());
				if (matcher.matches()) {
					output.setTimelinessName(S1_SESSION);
					output.setTimelinessValueSeconds(timelinessConfig.get(S1_SESSION));
				} else {
					Object t = catalogEvent.getMetadata().get("timeliness");
					if (t == null) {
						LOG.warn("Timeliness in CatalogEvent is null");
						break;
					}
					switch ((String) t) {
					case NRT:
						output.setTimelinessName(S1_NRT);
						output.setTimelinessValueSeconds(timelinessConfig.get(S1_NRT));
						break;
					case FAST24:
						output.setTimelinessName(S1_FAST24);
						output.setTimelinessValueSeconds(timelinessConfig.get(S1_FAST24));
						break;
					case PT:
						output.setTimelinessName(S1_PT);
						output.setTimelinessValueSeconds(timelinessConfig.get(S1_PT));
						break;
					default:
						LOG.warn("Unexpected timeliness in CatalogEvent {}", t);
						break;
					}
				}
			}
			break;
		}
	}
}

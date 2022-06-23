package esa.s1pdgs.cpoc.metadata.extraction.service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.metadata.PathMetadataExtractor;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.metadata.extraction.config.MdcWorkerConfigurationProperties;
import esa.s1pdgs.cpoc.metadata.extraction.service.elastic.EsServices;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.MetadataExtractor;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.MetadataExtractorFactory;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.report.MetadataExtractionReportingOutput;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.report.MetadataExtractionReportingOutput.EffectiveDownlink;
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

	private final EsServices esServices;
	private final MdcWorkerConfigurationProperties properties;
	private final MetadataExtractorFactory extractorFactory;

	@Autowired
	public ExtractionService(final EsServices esServices, final MdcWorkerConfigurationProperties properties,
			final MetadataExtractorFactory extractorFactory) {
		this.esServices = esServices;
		this.properties = properties;
		this.extractorFactory = extractorFactory;
	}

	@Override
	public CatalogEvent apply(CatalogJob catalogJob) {
		MissionId mission = null;

		if (catalogJob.getProductFamily().isSessionFamily()) {
			PathMetadataExtractor mExtractor = MetadataExtractorFactory
					.newPathMetadataExtractor(properties.getProductCategories().get(ProductCategory.EDRS_SESSIONS));
			mission = MissionId
					.valueOf(mExtractor.metadataFrom(catalogJob.getRelativePath()).get(MissionId.FIELD_NAME));
		} else {
			mission = MissionId.fromFileName(catalogJob.getKeyObjectStorage());
		}

		final Reporting reporting = ReportingUtils.newReportingBuilder(mission).predecessor(catalogJob.getUid())
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
		reporting.end(reportingOutput(result), new ReportingMessage("End metadata extraction"), quality);
		
		LOG.info("Sucessfully processed metadata extraction for {}", result.getProductName());

		return result;
	}

	private final CatalogEvent handleMessage(final CatalogJob catJob, final Reporting reporting) throws Exception {
		final String productName = catJob.getKeyObjectStorage();
		final ProductFamily family = catJob.getProductFamily();
		final ProductCategory category = ProductCategory.of(family);

		final MetadataExtractor extractor = extractorFactory.newMetadataExtractorFor(category,
				properties.getProductCategories().get(category));
		final JSONObject metadata = extractor.extract(reporting, catJob);

		// TODO move to extractor
		if (null != catJob.getTimeliness() && !metadata.has("timeliness")) {
			metadata.put("timeliness", catJob.getTimeliness());
		}

		// TODO move to extractor
		if (!metadata.has("insertionTime")) {
			metadata.put("insertionTime", DateUtils.formatToMetadataDateTimeFormat(LocalDateTime.now()));
		}
		
		// RS-248: Adding t0_pdgs_date into metadata
		if (catJob.getT0_pdgs_date() != null) {
			metadata.put("t0_pdgs_date", catJob.getT0_pdgs_date());
		}
		
		LOG.debug("Metadata extracted: {} for product: {}", metadata, productName);

		String warningMessage = esServices.createMetadataWithRetries(metadata, productName,
				properties.getProductInsertion().getMaxRetries(), properties.getProductInsertion().getTempoRetryMs());

		final CatalogEvent event = toCatalogEvent(catJob, metadata);
		event.setUid(reporting.getUid());
		return event;
	}

	private final CatalogEvent toCatalogEvent(final CatalogJob catJob, final JSONObject metadata) {
		final CatalogEvent catEvent = new CatalogEvent();
		String satelliteId;
		try {
			satelliteId = metadata.getString("satelliteId");
		} catch (JSONException e) {
			satelliteId = catJob.getSatelliteId();
		}
		
		catEvent.setMetadata(metadata.toMap());
		catEvent.setMissionId(catJob.getMissionId());
		catEvent.setSatelliteId(satelliteId);
		catEvent.setProductName(catJob.getKeyObjectStorage());
		catEvent.setKeyObjectStorage(catJob.getKeyObjectStorage());
		catEvent.setStoragePath(catJob.getStoragePath());
		catEvent.setProductFamily(catJob.getProductFamily());
		catEvent.setProductType(metadata.getString("productType"));
		catEvent.setT0_pdgs_date(catJob.getT0_pdgs_date());
		
		return catEvent;
	}

	private final ReportingOutput reportingOutput(final CatalogEvent catalogEvent) {
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

		return output.build();
	}
}

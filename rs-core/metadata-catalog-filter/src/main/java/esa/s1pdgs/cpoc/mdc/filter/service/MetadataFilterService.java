package esa.s1pdgs.cpoc.mdc.filter.service;

import java.util.UUID;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.mdc.filter.CatalogJobMapper;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;

public class MetadataFilterService implements Function<IngestionEvent, CatalogJob> {
	private static final CatalogJobMapper<IngestionEvent> INGESTION_MAPPER = new CatalogJobMapper<IngestionEvent>() {
		@Override
		public final CatalogJob toCatJob(final IngestionEvent event, final UUID reportingId) {
			final CatalogJob job = new CatalogJob();
			job.setProductName(event.getProductName());
			job.setRelativePath(event.getRelativePath());
			job.setProductFamily(event.getProductFamily());
			job.setKeyObjectStorage(event.getKeyObjectStorage());
			job.setUid(reportingId);
			job.setStationName(event.getStationName());
			job.setMode(event.getMode());
			job.setTimeliness(event.getTimeliness());
			return job;
		}
	};
	private static final CatalogJobMapper<ProductionEvent> PROD_MAPPER = new CatalogJobMapper<ProductionEvent>() {
		@Override
		public final CatalogJob toCatJob(final ProductionEvent event, final UUID reportingId) {
			final CatalogJob job = new CatalogJob();
			job.setProductName(event.getProductName());
			// relativ path should not be needed here --> only evaluated for EDRS_SESSION
			job.setProductFamily(event.getProductFamily());
			job.setKeyObjectStorage(event.getKeyObjectStorage());
			job.setMode(event.getMode());
			job.setOqcFlag(event.getOqcFlag());
			job.setUid(reportingId);
			job.setTimeliness(event.getTimeliness());
			return job;
		}
	};

	private static final CatalogJobMapper<CompressionEvent> COMPRESSION_MAPPER = new CatalogJobMapper<CompressionEvent>() {
		@Override
		public final CatalogJob toCatJob(final CompressionEvent event, final UUID reportingId) {
			final CatalogJob job = new CatalogJob();
			job.setProductName(event.getKeyObjectStorage());
			job.setRelativePath(event.getKeyObjectStorage());
			job.setProductFamily(event.getProductFamily());
			job.setKeyObjectStorage(event.getKeyObjectStorage());
			job.setUid(reportingId);
			return job;
		}
	};

	private static final Logger LOG = LogManager.getLogger(MetadataFilterService.class);

	@Override
	public CatalogJob apply(IngestionEvent message) {
		final String eventType = message.getClass().getSimpleName();

		MissionId mission = null;

		if (message.getProductFamily().isSessionFamily()) {
			if (message instanceof IngestionEvent) {
				mission = MissionId.valueOf(((IngestionEvent) message).getMissionId());
			} else {
				LOG.warn("cannot determine missionId");
				mission = MissionId.UNDEFINED;
			}
		} else {
			mission = MissionId.fromFileName(message.getKeyObjectStorage());
		}

		final Reporting reporting = ReportingUtils.newReportingBuilder(mission).predecessor(message.getUid())
				.newReporting("MetadataTrigger");

		reporting.begin(
				ReportingUtils.newFilenameReportingInputFor(message.getProductFamily(), message.getKeyObjectStorage()),
				new ReportingMessage("Received %s", eventType));

		CatalogJob result;
		try {
			result = newPublicationMessage(reporting, message);
		} catch (Exception e) {
			reporting.error(
					new ReportingMessage(String.format("Error on handling %s: %s", eventType, LogUtils.toString(e))));
			throw e;
		}

		reporting.end(new ReportingMessage("Created CatalogJob for %s", eventType));
		return result;
	}

	final CatalogJob newPublicationMessage(final Reporting reporting, final AbstractMessage message) {

		CatalogJob job = INGESTION_MAPPER.toCatJob((IngestionEvent) message, reporting.getUid());

		LOG.info("Converted %s (%s) to CatalogJob", message.getUid(), message.getClass().getSimpleName());
		return job;
	}
}

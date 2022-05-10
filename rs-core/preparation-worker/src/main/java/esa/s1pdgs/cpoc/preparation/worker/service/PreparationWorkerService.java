package esa.s1pdgs.cpoc.preparation.worker.service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.mqi.client.MqiPublishingJob;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfPreparationJob;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;
import esa.s1pdgs.cpoc.preparation.worker.tasktable.TasktableMapper;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;

public class PreparationWorkerService implements Function<CatalogEvent, List<IpfExecutionJob>> {

	static final Logger LOGGER = LogManager.getLogger(PreparationWorkerService.class);

	@Override
	public List<IpfExecutionJob> apply(CatalogEvent catalogEvent) {
		final Reporting reporting = ReportingUtils
				.newReportingBuilder(MissionId.fromFileName(catalogEvent.getKeyObjectStorage()))
				.predecessor(catalogEvent.getUid()).newReporting("PreparationWorkerService");

		reporting.begin(
				ReportingUtils.newFilenameReportingInputFor(catalogEvent.getProductFamily(),
						catalogEvent.getProductName()),
				new ReportingMessage("Check if any jobs can be finalized for the IPF"));

		try {
			// Map event to tasktables

			// Create new Jobs

			// Find matching jobs

			// Check if jobs are ready

			// Save new status
		} catch (Exception e) {
			reporting.error(new ReportingMessage("Preparation worker failed: %s", LogUtils.toString(e)));
			throw new RuntimeException(e);
		}

		reporting.end(null, new ReportingMessage("End preparation of new execution jobs"));

		return null;
	}
}

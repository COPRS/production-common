package esa.s1pdgs.cpoc.ipf.execution.worker.job.oqc;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.ipf.execution.worker.config.ApplicationProperties;
import esa.s1pdgs.cpoc.ipf.execution.worker.job.oqc.report.OqcReportingOutput;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobOutputDto;
import esa.s1pdgs.cpoc.mqi.model.queue.OQCFlag;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingFactory;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;

public class OQCExecutor {
	/**
	 * Logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(OQCExecutor.class);

	private ApplicationProperties properties;

	public OQCExecutor(final ApplicationProperties properties) {
		this.properties = properties;
	}

	public OQCFlag executeOQC(final File file, final ProductFamily family, final LevelJobOutputDto output, final OQCTaskFactory factory,
			final ReportingFactory reportingFactory) {
		final Reporting reporting = reportingFactory.newReporting("OQC");
		
		// Just check if OQC is enabled after all
		if (properties.isOqcEnabled() && output.isOqcCheck()) {
			// This output needs to be quality checked
			LOGGER.info("Executing OQC check for product {}", file);
			
			reporting.begin(					
					ReportingUtils.newFilenameReportingInputFor(family, file.getName()),
					new ReportingMessage("Start OQC for product %s", file.getName())
			);
			final ExecutorService executor = Executors.newSingleThreadExecutor();

			try {
				final OQCFlag flag = executor.submit(factory.createOQCTask(properties, file)).get();
				reporting.end(
						new OqcReportingOutput(flag.toString()),
						new ReportingMessage("End OQC for product %s: %s", file.getName(), flag)
				);
				return flag;
			} catch (final Exception e) {
				reporting.error(new ReportingMessage(
					"Error on OQC for product %s (%s): %s",				
					file.getName(), 
					OQCFlag.NOT_CHECKED,
					LogUtils.toString(e)
				));
				LOGGER.error("Failed to execute OQC check: {}", LogUtils.toString(e));
			}

		} else {
			// No OQC check received form job order generator, no need to do anything
			LOGGER.debug("OQC check for product {} not required", file);
		}

		// no OQC check performed or an error occured. Assume it unchecked.
		return OQCFlag.NOT_CHECKED;
	}
}

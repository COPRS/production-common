package esa.s1pdgs.cpoc.ipf.execution.worker.job.oqc;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.ipf.execution.worker.config.ApplicationProperties;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobOutputDto;
import esa.s1pdgs.cpoc.mqi.model.queue.OQCFlag;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;

public class OQCExecutor {
	/**
	 * Logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(OQCExecutor.class);

	private ApplicationProperties properties;

	public OQCExecutor(final ApplicationProperties properties) {
		this.properties = properties;
	}

	public OQCFlag executeOQC(final Path originalProduct, final LevelJobOutputDto output, final OQCTaskFactory factory,
			final Reporting.ChildFactory reportingChildFactory) {
		final Reporting reporting = reportingChildFactory.newChild("Online Quality Check");
		
		// Just check if OQC is enabled after all
		if (properties.isOqcEnabled() && output.isOqcCheck()) {
			// This output needs to be quality checked
			LOGGER.info("Executing OQC check for product {}", originalProduct);
			
			reporting.begin(new ReportingMessage("Start of OQC execution for product {}",
					originalProduct.getFileName()));
			final ExecutorService executor = Executors.newSingleThreadExecutor();

			try {
				final OQCFlag flag = executor.submit(factory.createOQCTask(properties, originalProduct)).get();
				reporting.end(new ReportingMessage("End of OQC execution for product {} ({})",
						originalProduct.getFileName(), flag));
				return flag;
			} catch (final Exception e) {
				/*
				 * Whatever happens, something was not working as expected and it needs to be
				 * assumed that the OQC check failed.
				 */
				reporting.error(new ReportingMessage("Error on oqc execution for product {} ({}): {}",
						LogUtils.toString(e), originalProduct.getFileName(), OQCFlag.NOT_CHECKED));
				LOGGER.error("Failed to execute OQC check successfully: {}", LogUtils.toString(e));

			}

		} else {
			// No OQC check received form job order generator, no need to do anything
			LOGGER.debug("OQC check for product {} not required", originalProduct);
		}

		// no OQC check performed or an error occured. Assume it unchecked.
		return OQCFlag.NOT_CHECKED;
	}
}

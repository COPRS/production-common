package esa.s1pdgs.cpoc.preparation.worker.metrics;

import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import esa.s1pdgs.cpoc.common.CommonConfigurationProperties;
import esa.s1pdgs.cpoc.preparation.worker.config.ProcessProperties;
import esa.s1pdgs.cpoc.preparation.worker.service.AppCatJobService;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class PendingProcessingJobGauge {
	
	@Autowired
	private AppCatJobService appCatJobService;

	public Supplier<Number> fetchPendingProcessingJobs() {
		return () -> appCatJobService.getCountOfPendingJobs();
	}

	@Autowired
	public PendingProcessingJobGauge(final MeterRegistry registry, final ProcessProperties processProperties,
			final CommonConfigurationProperties commonProperties) {
		Assert.notNull(processProperties.getMission(), "property process.mission has to be set");
		Assert.notNull(processProperties.getLevel(), "property process.level has to be set");

		Gauge.builder("rs_pending_processing_job", fetchPendingProcessingJobs())
				.tag("mission", processProperties.getMission().toString())
				.tag("level", processProperties.getLevel().toString())
				.tag("addonName", commonProperties.getRsChainName()).register(registry);
	}
}

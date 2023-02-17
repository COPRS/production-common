package esa.s1pdgs.cpoc.preparation.worker.metrics;

import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.preparation.worker.config.PrometheusMetricsProperties;
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
	public PendingProcessingJobGauge(final MeterRegistry registry,
			final PrometheusMetricsProperties metricsProperties) {
		// Expose metric of currently pending jobs to actuator/prometheus, if properties are set
		if (!metricsProperties.getMission().isEmpty() && !metricsProperties.getLevel().isEmpty()
				&& !metricsProperties.getAddonName().isEmpty()) {
			Gauge.builder("rs.pending.processing.job", fetchPendingProcessingJobs())
					.tag("mission", metricsProperties.getMission()).tag("level", metricsProperties.getLevel())
					.tag("addonName", metricsProperties.getAddonName()).register(registry);
		}
	}
}

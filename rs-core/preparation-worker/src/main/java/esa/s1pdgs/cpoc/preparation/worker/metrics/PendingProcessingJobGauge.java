/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

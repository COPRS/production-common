package esa.s1pdgs.cpoc.preparation.worker.config;

import java.util.List;
import java.util.function.Function;

import org.springframework.context.annotation.Bean;

import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.preparation.worker.service.PreparationWorkerService;

/**
 * Configuration class containing the interface for Spring Cloud Dataflow.
 */
public class PreparationWorkerServiceConfiguration {

	@Bean
	public Function<CatalogEvent, List<IpfExecutionJob>> prepareExecutionJobs() {
		return new PreparationWorkerService();
	}
}

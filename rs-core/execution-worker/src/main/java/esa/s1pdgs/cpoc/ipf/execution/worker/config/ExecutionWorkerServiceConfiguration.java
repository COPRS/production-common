package esa.s1pdgs.cpoc.ipf.execution.worker.config;

import java.util.function.Function;

import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.ipf.execution.worker.service.JobProcessor;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;

@Configuration
public class ExecutionWorkerServiceConfiguration {

	public Function<IpfExecutionJob, CatalogJob> processJob() {
		return new JobProcessor(null, null, null, null, null, null, null, null, null, 0, 0)
	}
}

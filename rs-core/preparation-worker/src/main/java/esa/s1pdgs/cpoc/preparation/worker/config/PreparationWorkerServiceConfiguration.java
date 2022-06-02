package esa.s1pdgs.cpoc.preparation.worker.config;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.preparation.worker.publish.Publisher;
import esa.s1pdgs.cpoc.preparation.worker.query.AuxQueryHandler;
import esa.s1pdgs.cpoc.preparation.worker.service.AppCatJobService;
import esa.s1pdgs.cpoc.preparation.worker.service.PreparationWorkerService;
import esa.s1pdgs.cpoc.preparation.worker.service.TaskTableMapperService;
import esa.s1pdgs.cpoc.preparation.worker.tasktable.adapter.TaskTableAdapter;
import esa.s1pdgs.cpoc.preparation.worker.type.ProductTypeAdapter;

/**
 * Configuration class containing the interface for Spring Cloud Dataflow.
 */
@Configuration
public class PreparationWorkerServiceConfiguration {

	@Autowired
	private ProcessProperties processProperties;

	@Autowired
	private TaskTableMapperService taskTableMapperService;

	@Autowired
	private AppCatJobService appCatJobService;

	@Autowired
	private ProductTypeAdapter typeAdapter;

	@Autowired
	private Map<String, TaskTableAdapter> taskTableAdapters;
	
	@Autowired
	private AuxQueryHandler auxQueryHandler;
	
	@Autowired
	private Publisher publisher;

	@Bean
	public Function<CatalogEvent, List<Message<IpfExecutionJob>>> prepareExecutionJobs() {
		return new PreparationWorkerService(taskTableMapperService, typeAdapter, processProperties, appCatJobService,
				taskTableAdapters, auxQueryHandler, publisher);
	}
}

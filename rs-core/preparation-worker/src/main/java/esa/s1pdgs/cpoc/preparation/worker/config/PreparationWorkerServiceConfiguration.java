package esa.s1pdgs.cpoc.preparation.worker.config;

import java.util.List;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.preparation.worker.service.PreparationWorkerService;
import esa.s1pdgs.cpoc.preparation.worker.service.TaskTableMapperService;
import esa.s1pdgs.cpoc.preparation.worker.tasktable.ConfigurableKeyEvaluator;
import esa.s1pdgs.cpoc.preparation.worker.tasktable.RoutingBasedTasktableMapper;
import esa.s1pdgs.cpoc.preparation.worker.tasktable.SingleTasktableMapper;
import esa.s1pdgs.cpoc.preparation.worker.tasktable.TasktableMapper;
import esa.s1pdgs.cpoc.xml.XmlConverter;

/**
 * Configuration class containing the interface for Spring Cloud Dataflow.
 */
public class PreparationWorkerServiceConfiguration {

	@Bean
	public Function<CatalogEvent, List<IpfExecutionJob>> prepareExecutionJobs() {
		return new PreparationWorkerService();
	}

	@Bean
	@Autowired
	public TasktableMapper newTastTableMapper(final XmlConverter xmlConverter,
			final TaskTableMappingProperties properties) {

		if (!StringUtils.isEmpty(properties.getName())) {
			return new SingleTasktableMapper(properties.getName());
		}
		if (!StringUtils.isEmpty(properties.getRoutingFile())) {
			return new RoutingBasedTasktableMapper.Factory(xmlConverter, properties.getRoutingFile(),
					new ConfigurableKeyEvaluator(properties.getRoutingKeyTemplate())).newMapper();
		}
		throw new IllegalStateException(String.format("Missing required elements in configuration: %s", this));
	}

	@Bean
	@Autowired
	public TaskTableMapperService taskTableMapperService(final TasktableMapper ttMapper,
			final ProcessProperties processProperties, final MetadataClient metadataClient) {
		return new TaskTableMapperService(ttMapper, processProperties, metadataClient);
	}
}

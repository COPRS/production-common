package esa.s1pdgs.cpoc.preparation.worker.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.preparation.worker.db.AppDataJobRepository;
import esa.s1pdgs.cpoc.preparation.worker.db.SequenceDao;
import esa.s1pdgs.cpoc.preparation.worker.service.AppCatJobService;
import esa.s1pdgs.cpoc.preparation.worker.service.TaskTableMapperService;
import esa.s1pdgs.cpoc.preparation.worker.tasktable.mapper.ConfigurableKeyEvaluator;
import esa.s1pdgs.cpoc.preparation.worker.tasktable.mapper.RoutingBasedTasktableMapper;
import esa.s1pdgs.cpoc.preparation.worker.tasktable.mapper.SingleTasktableMapper;
import esa.s1pdgs.cpoc.preparation.worker.tasktable.mapper.TasktableMapper;
import esa.s1pdgs.cpoc.xml.XmlConverter;

@Configuration
public class ServiceConfiguration {

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
	
	@Bean
	@Autowired
	public AppCatJobService appCatJobService(final AppDataJobRepository repository, final SequenceDao sequenceDao) {
		return new AppCatJobService(repository, sequenceDao);
	}
}

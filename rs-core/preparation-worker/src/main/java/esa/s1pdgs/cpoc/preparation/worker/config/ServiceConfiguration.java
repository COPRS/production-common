package esa.s1pdgs.cpoc.preparation.worker.config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.common.CommonConfigurationProperties;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.preparation.worker.db.AppDataJobRepository;
import esa.s1pdgs.cpoc.preparation.worker.db.SequenceDao;
import esa.s1pdgs.cpoc.preparation.worker.model.joborder.JobOrderAdapter;
import esa.s1pdgs.cpoc.preparation.worker.query.AuxQueryHandler;
import esa.s1pdgs.cpoc.preparation.worker.service.AppCatJobService;
import esa.s1pdgs.cpoc.preparation.worker.service.InputSearchService;
import esa.s1pdgs.cpoc.preparation.worker.service.JobCreationService;
import esa.s1pdgs.cpoc.preparation.worker.service.TaskTableMapperService;
import esa.s1pdgs.cpoc.preparation.worker.tasktable.adapter.ElementMapper;
import esa.s1pdgs.cpoc.preparation.worker.tasktable.adapter.TaskTableAdapter;
import esa.s1pdgs.cpoc.preparation.worker.tasktable.adapter.TaskTableFactory;
import esa.s1pdgs.cpoc.preparation.worker.tasktable.adapter.TasktableManager;
import esa.s1pdgs.cpoc.preparation.worker.tasktable.mapper.ConfigurableKeyEvaluator;
import esa.s1pdgs.cpoc.preparation.worker.tasktable.mapper.RoutingBasedTasktableMapper;
import esa.s1pdgs.cpoc.preparation.worker.tasktable.mapper.TasktableMapper;
import esa.s1pdgs.cpoc.preparation.worker.type.ProductTypeAdapter;
import esa.s1pdgs.cpoc.xml.XmlConverter;

@Configuration
public class ServiceConfiguration {

	private static final Logger LOG = LoggerFactory.getLogger(ServiceConfiguration.class);

	@Bean
	@Autowired
	public TasktableMapper newTastTableMapper(final TaskTableMappingProperties properties) {
		return new RoutingBasedTasktableMapper.Factory(properties.getRouting(),
				new ConfigurableKeyEvaluator(properties.getRoutingKeyTemplate())).newMapper();
	}

	@Bean
	@Autowired
	public TaskTableMapperService taskTableMapperService(final TasktableMapper ttMapper,
			final ProcessProperties processProperties, final MetadataClient metadataClient,
			final Map<String, TaskTableAdapter> ttAdapters) {
		return new TaskTableMapperService(ttMapper, processProperties, metadataClient, ttAdapters);
	}

	@Bean
	@Autowired
	public AppCatJobService appCatJobService(final AppDataJobRepository repository, final SequenceDao sequenceDao) {
		LOG.info("Create new AppCatJobService with {} and {}", repository.toString(), sequenceDao.toString());
		return new AppCatJobService(repository, sequenceDao);
	}

	@Bean
	@Autowired
	public TasktableManager tasktableManager(final PreparationWorkerProperties settings) {
		return TasktableManager.of(new File(settings.getDiroftasktables()));
	}

	@Bean
	@Autowired
	public Map<String, TaskTableAdapter> taskTableAdapters(final ProcessProperties processSettings,
			final ElementMapper elementMapper, final TaskTableFactory taskTableFactory,
			final PreparationWorkerProperties settings, final TasktableManager ttManager) {
		Map<String, TaskTableAdapter> ttAdapters = new HashMap<>();

		for (File taskTableFile : ttManager.tasktables()) {
			LOG.debug("Loading tasktable {}",taskTableFile.getAbsolutePath());
			ttAdapters.put(taskTableFile.getName(),
					new TaskTableAdapter(taskTableFile,
							taskTableFactory.buildTaskTable(taskTableFile, processSettings.getLevel()), elementMapper,
							settings.getProductMode()));
		}

		return ttAdapters;
	}

	@Bean
	@Autowired
	public AuxQueryHandler auxQueryHandler(final MetadataClient metadataClient,
			final PreparationWorkerProperties settings) {
		return new AuxQueryHandler(metadataClient, settings.getProductMode());
	}

	@Bean
	@Autowired
	public InputSearchService inputSearchService(final ProductTypeAdapter typeAdapter,
			final AuxQueryHandler auxQueryHandler) {
		return new InputSearchService(typeAdapter, auxQueryHandler);
	}

	@Bean
	@Autowired
	public JobCreationService publisher(final CommonConfigurationProperties commonProperties,
			final PreparationWorkerProperties settings, final ProcessProperties processSettings,
			final ProductTypeAdapter typeAdapter, final ElementMapper elementMapper, final XmlConverter xmlConverter) {
		final JobOrderAdapter.Factory jobOrderFactory = new JobOrderAdapter.Factory(
				(tasktableAdapter) -> tasktableAdapter.newJobOrder(processSettings, settings.getProductMode()),
				typeAdapter, elementMapper, xmlConverter);

		return new JobCreationService(commonProperties, settings, processSettings, jobOrderFactory, typeAdapter);
	}

}

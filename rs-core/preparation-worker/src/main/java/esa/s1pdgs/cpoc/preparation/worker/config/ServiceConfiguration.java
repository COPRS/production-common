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

package esa.s1pdgs.cpoc.preparation.worker.config;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.xml.sax.SAXException;

import esa.s1pdgs.cpoc.common.CommonConfigurationProperties;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.preparation.worker.config.PreparationWorkerProperties.InputWaitingConfig;
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
import esa.s1pdgs.cpoc.preparation.worker.timeout.InputTimeoutChecker;
import esa.s1pdgs.cpoc.preparation.worker.timeout.InputTimeoutCheckerImpl;
import esa.s1pdgs.cpoc.preparation.worker.type.ProductTypeAdapter;
import esa.s1pdgs.cpoc.xml.XmlConverter;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTable;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableInputAlternative;

@Configuration
public class ServiceConfiguration {

	private static final Logger LOG = LoggerFactory.getLogger(ServiceConfiguration.class);
	
	@Autowired
	private MetadataClientProperties metaProperties;

	@Bean
	@Autowired
	public TasktableMapper newTastTableMapper(final TaskTableMappingProperties properties) {
		return new RoutingBasedTasktableMapper.Factory(properties.getRouting(),
				new ConfigurableKeyEvaluator(properties.getRoutingKeyTemplate())).newMapper();
	}

	@Bean
	@Autowired
	public TaskTableMapperService taskTableMapperService(final TasktableMapper ttMapper,
			final ProcessProperties processProperties, final Map<String, TaskTableAdapter> ttAdapters) throws XPathExpressionException, IOException, ParserConfigurationException, SAXException {
		return new TaskTableMapperService(ttMapper, processProperties, ttAdapters);
	}

	@Bean
	@Autowired
	public AppCatJobService appCatJobService(final AppDataJobRepository repository, final SequenceDao sequenceDao,
			final ProcessProperties processSettings) {
		LOG.info("Create new AppCatJobService with {} and {}", repository.toString(), sequenceDao.toString());
		return new AppCatJobService(repository, sequenceDao, processSettings);
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
			LOG.debug("Loading tasktable {}", taskTableFile.getAbsolutePath());
			
			TaskTableAdapter adapter = new TaskTableAdapter(taskTableFile, taskTableFactory
					.buildTaskTable(taskTableFile, processSettings.getLevel(), settings.getPathTaskTableXslt()),
					elementMapper, settings.getProductMode());

			/* RS-584: Custom selection policy existing in SL1 that is not supported by the software
			   Thus it will be checked if custom and class matching to com.werum.esa.pfm.selection.policies.PolicyIntersectMinNumber.
			   If this is true, it will be replaced with ValIntersect
			*/
			for (TaskTableInputAlternative alternative: adapter.getAllAlternatives()) {
				if (alternative.getRetrievalMode().equals("Custom")) {
					if (alternative.getCustomClass().equals("com.werum.esa.pfm.selection.policies.PolicyIntersectMinNumber")) {
						alternative.setRetrievalMode("ValIntersect");
						LOG.info("Found custom selection policy for class 'com.werum.esa.pfm.selection.policies.PolicyIntersectMinNumber', using ValIntersect instead");
						continue;
					}
					
					throw new IllegalArgumentException("Unsupported custom selection policy: "+alternative.getCustomClass());
				} else if (alternative.getRetrievalMode().equals("ValIntersectWithoutDuplicates")) {
					if (metaProperties.isValIntersectNoDuplicatesWorkaround()) {
						alternative.setRetrievalMode("ValIntersect");
						LOG.info("Found selection policy 'ValIntersectWithoutDuplicates'. Replace with 'ValIntersect'");					
						continue;
					}
				}
			}
			ttAdapters.put(taskTableFile.getName(), adapter);
		}

		return ttAdapters;
	}

	@Bean
	@Autowired
	public AuxQueryHandler auxQueryHandler(final MetadataClient metadataClient,
			final PreparationWorkerProperties settings, final Function<TaskTable, InputTimeoutChecker> timeoutChecker) {
		return new AuxQueryHandler(metadataClient, settings.getProductMode(), timeoutChecker);
	}

	@Bean
	@Autowired
	public InputSearchService inputSearchService(final ProductTypeAdapter typeAdapter,
			final AuxQueryHandler auxQueryHandler, final Map<String, TaskTableAdapter> taskTableAdapters,
			final AppCatJobService appCatJobService, final JobCreationService jobCreationService) {
		return new InputSearchService(typeAdapter, auxQueryHandler, taskTableAdapters, appCatJobService,
				jobCreationService);
	}

	@Bean
	@Autowired
	public JobCreationService publisher(final CommonConfigurationProperties commonProperties,
			final PreparationWorkerProperties settings, final ProcessProperties processSettings,
			final ProductTypeAdapter typeAdapter, final ElementMapper elementMapper, final XmlConverter xmlConverter) {
		final JobOrderAdapter.Factory jobOrderFactory = new JobOrderAdapter.Factory(
				(tasktableAdapter) -> tasktableAdapter.newJobOrder(processSettings, settings.getProductMode()),
				typeAdapter, elementMapper, xmlConverter, settings);

		return new JobCreationService(commonProperties, settings, processSettings, jobOrderFactory, typeAdapter);
	}
	
	@Bean
	@Autowired
	public Function<TaskTable, InputTimeoutChecker> timeoutCheckerFor(final PreparationWorkerProperties workerProperties) {
		return (taskTable) -> {
			final List<InputWaitingConfig> configsForTaskTable = new ArrayList<>();
			for (final InputWaitingConfig config : workerProperties.getInputWaiting().values()) {
				if (taskTable.getProcessorName().matches(config.getProcessorNameRegexp()) &&
					taskTable.getVersion().matches(config.getProcessorVersionRegexp())) 
				{			
					configsForTaskTable.add(config);
				}					
			}
			// default: always time out
			return new InputTimeoutCheckerImpl(configsForTaskTable, LocalDateTime::now);
		};
	}
}

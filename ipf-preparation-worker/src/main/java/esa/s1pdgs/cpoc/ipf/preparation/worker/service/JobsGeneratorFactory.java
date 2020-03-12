package esa.s1pdgs.cpoc.ipf.preparation.worker.service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.function.BiFunction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerBuildTaskTableException;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.AiopProperties;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.IpfPreparationWorkerSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.IpfPreparationWorkerSettings.InputWaitingConfig;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.ProcessSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.ProductMode;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTable;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableInput;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.mqi.client.MqiClient;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;

@Service
public class JobsGeneratorFactory {
	// sorry for the shitty naming but we want to keep it in line with the existing naming scheme
	public enum JobGenType {
		LEVEL_0,
		LEVEL_SEGMENT,
		LEVEL_PRODUCT
	}
	
	private final ProcessSettings l0ProcessSettings;
	private final IpfPreparationWorkerSettings ipfPreparationWorkerSettings;
	private final AiopProperties aiopProperties;
	private final XmlConverter xmlConverter;
	private final MetadataClient metadataClient;
	private final ProcessConfiguration processConfiguration;
	private final MqiClient mqiClient;
	private final TaskTableFactory taskTableFactory;

	@Autowired
	public JobsGeneratorFactory(
			final ProcessSettings l0ProcessSettings,
			final IpfPreparationWorkerSettings ipfPreparationWorkerSettings, 
			final AiopProperties aiopProperties,
			final XmlConverter xmlConverter, 
			final MetadataClient metadataClient,
			final ProcessConfiguration processConfiguration,
			final MqiClient mqiClient,
			final TaskTableFactory taskTableFactory
	) {
		this.l0ProcessSettings = l0ProcessSettings;
		this.ipfPreparationWorkerSettings = ipfPreparationWorkerSettings;
		this.aiopProperties = aiopProperties;
		this.xmlConverter = xmlConverter;
		this.metadataClient = metadataClient;
		this.processConfiguration = processConfiguration;
		this.mqiClient = mqiClient;
		this.taskTableFactory = taskTableFactory;
	}
	
	public AbstractJobsGenerator newJobGenerator(
			final File xmlFile,
			final AppCatalogJobClient<CatalogEvent> appDataService,
			final JobGenType type
	) throws IpfPrepWorkerBuildTaskTableException {		
		final TaskTable taskTable = taskTableFactory.buildTaskTable(xmlFile, l0ProcessSettings.getLevel());		
		final AbstractJobsGenerator processor = newGenerator(xmlFile, appDataService, taskTable, type);
		processor.initialize();
		return processor;
	}
		
	private AbstractJobsGenerator newGenerator(
			final File xmlFile,
			final AppCatalogJobClient<CatalogEvent> appDataService, 
			final TaskTable taskTable,
			final JobGenType type
	) {
		switch (type) {
			case LEVEL_0:
				return new L0AppJobsGenerator(
						xmlConverter,
						metadataClient, 
						l0ProcessSettings, 
						ipfPreparationWorkerSettings, 
						appDataService, 
						aiopProperties, 
						processConfiguration,
						mqiClient,
						inputWaitTimeoutFor(taskTable),
						() -> LocalDateTime.now(),
						xmlFile.getName(),
						taskTable,
						ProductMode.SLICING
				);
			case LEVEL_SEGMENT:
				return new L0SegmentAppJobsGenerator(
						xmlConverter,
						metadataClient, 
						l0ProcessSettings, 
						ipfPreparationWorkerSettings, 
						appDataService, 
						processConfiguration,
						mqiClient,
						inputWaitTimeoutFor(taskTable),
						() -> LocalDateTime.now(),
						xmlFile.getName(),
						taskTable,
						ProductMode.SLICING	
				);
			case LEVEL_PRODUCT:
				return new LevelProductsJobsGenerator(
						xmlConverter,
						metadataClient, 
						l0ProcessSettings, 
						ipfPreparationWorkerSettings, 
						appDataService, 
						processConfiguration,
						mqiClient,
						inputWaitTimeoutFor(taskTable),
						() -> LocalDateTime.now(),
						xmlFile.getName(),
						taskTable,
						ProductMode.SLICING	
				);
			default:
			  throw new IllegalArgumentException(
					  String.format("Unknown type %s. Available are: %s", type, Arrays.toString(JobGenType.values()))
			  );		
		}
	}
	
	private final BiFunction<String,TaskTableInput, Long> inputWaitTimeoutFor(final TaskTable taskTable) {
		for (final InputWaitingConfig config : ipfPreparationWorkerSettings.getInputWaiting()) {
			if (taskTable.getProcessorName().equals(config.getProcessorNameRegexp()) &&
				taskTable.getVersion().matches(config.getProcessorVersionRegexp())) 
			{
				return (t, i) -> {
					if (t.matches(config.getTimelinessRegexp()) && i.getId().matches(config.getInputIdRegexp())) {
						return config.getWaitingInSeconds();
					}
					return 0L;
				};						
			}					
		}
		return (t, i) -> 0L;
	}	
}

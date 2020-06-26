package esa.s1pdgs.cpoc.ipf.preparation.worker.config;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.IpfPreparationWorkerSettings.CategoryConfig;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.IpfPreparationWorkerSettings.InputWaitingConfig;
import esa.s1pdgs.cpoc.ipf.preparation.worker.dispatch.JobDispatcherImpl;
import esa.s1pdgs.cpoc.ipf.preparation.worker.generator.JobGenerator;
import esa.s1pdgs.cpoc.ipf.preparation.worker.generator.JobsGeneratorFactory;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTable;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TasktableManager;
import esa.s1pdgs.cpoc.ipf.preparation.worker.service.IpfPreparationService;
import esa.s1pdgs.cpoc.ipf.preparation.worker.status.AppStatusImpl;
import esa.s1pdgs.cpoc.ipf.preparation.worker.timeout.InputTimeoutChecker;
import esa.s1pdgs.cpoc.ipf.preparation.worker.timeout.InputTimeoutCheckerImpl;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.ProductTypeFactory;
import esa.s1pdgs.cpoc.mqi.client.MqiClient;
import esa.s1pdgs.cpoc.mqi.client.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.client.MqiListener;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfPreparationJob;

@Configuration
public class IpfPreparationWorkerConfiguration {
	private static final Logger LOG = LogManager.getLogger(IpfPreparationWorkerConfiguration.class);
	
	private final AppCatalogJobClient appCatClient;
    private final AppStatusImpl appStatus;
    private final MqiClient mqiClient;
    private final IpfPreparationWorkerSettings settings;
    private final ErrorRepoAppender errorAppender;
    private final ProcessConfiguration processConfiguration;
	private final ProcessSettings processSettings;
    private final JobsGeneratorFactory jobGeneratorFactory;
        
	@Autowired
	public IpfPreparationWorkerConfiguration(
			final AppStatusImpl appStatus, 
			final MqiClient mqiClient,
			final IpfPreparationWorkerSettings settings, 
			final ErrorRepoAppender errorAppender,
			final ProcessConfiguration processConfiguration,
			final ProcessSettings processSettings,
			final AppCatalogJobClient appCatClient,
		    final JobsGeneratorFactory jobGeneratorFactory
	) {
		this.appStatus = appStatus;
		this.mqiClient = mqiClient;
		this.settings = settings;
		this.errorAppender = errorAppender;
		this.processConfiguration = processConfiguration;
		this.processSettings = processSettings;
		this.appCatClient = appCatClient;
		this.jobGeneratorFactory = jobGeneratorFactory;
	}

	@Bean
	public Function<TaskTable, InputTimeoutChecker> timeoutCheckerFor() {
		return t -> inputWaitTimeoutFor(t);		
	}
	
	@Bean
	public TasktableManager tasktableManager() {
		return TasktableManager.of(new File(settings.getDiroftasktables()));
	}
		
	@Bean(name="jobGenerationTaskScheduler", destroyMethod = "shutdown")
    public ThreadPoolTaskScheduler threadPoolTaskScheduler(final TasktableManager ttManager) {
        final ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(ttManager.size());
        threadPoolTaskScheduler.setThreadNamePrefix("JobGenerationTaskScheduler");
        return threadPoolTaskScheduler;
    }
	
//	@Bean
//	@Autowired
//	public AbstractJobsDispatcher jobsDispatcher(
//			final ProcessSettings processSettings,
//			final JobsGeneratorFactory factory, 
//			final ThreadPoolTaskScheduler taskScheduler,
//			final XmlConverter xmlConverter,
//			@Value("${level-products.pathroutingxmlfile}") final String pathRoutingXmlFile,
//		
//	) {
//		switch (processSettings.getLevel()) {
//			case L0:
//				return new L0AppJobDispatcher(settings, processSettings, factory, taskScheduler, appCatClient);
//			case L0_SEGMENT:
//				return new L0SegmentAppJobDispatcher(settings, processSettings, factory, taskScheduler, appCatClient);
//			case L1:
//			case L2:
//				return new LevelProductsJobDispatcher(settings, processSettings, factory, taskScheduler, xmlConverter, 
//						pathRoutingXmlFile, appCatClient);
//			default:
//				// fall through to throw exception
//		}
//		throw new IllegalArgumentException(
//				String.format(
//						"Unsupported Application Level '%s'. Available are: %s", 
//						processSettings.getLevel(),
//						Arrays.asList(ApplicationLevel.values())
//				)
//		);
//	}
	
	@Bean
	@Autowired
	public IpfPreparationService service(
			final TasktableManager ttManager, 
			final ThreadPoolTaskScheduler taskScheduler
	) {		
		final Map<String, JobGenerator> generators = new HashMap<>(ttManager.size());		
		final ProductTypeFactory factory = ProductTypeFactory.forLevel(processSettings.getLevel());		
	
		for (final File taskTableFile : ttManager.tasktables()) {				
			final JobGenerator jobGenerator = jobGeneratorFactory.newJobGenerator(taskTableFile, factory);
			generators.put(taskTableFile.getName(), jobGenerator);
		    // --> Launch generators
			taskScheduler.scheduleWithFixedDelay(jobGenerator, settings.getJobgenfixedrate());
		}
		
		final IpfPreparationService service = new IpfPreparationService(
				new JobDispatcherImpl(factory.tasktableMapper(), processSettings, appCatClient, generators), 
				errorAppender, 
				processConfiguration
		);
		
		// TODO add ThreadFactory to give a descriptive name for the consumer
		final ExecutorService executor = Executors.newFixedThreadPool(
				settings.getProductCategories().size()
		);
		
		for (final Map.Entry<ProductCategory, CategoryConfig> entry : settings.getProductCategories().entrySet()) {				
			executor.execute(newConsumerFor(entry.getKey(), entry.getValue(), service));
		}		
		return service;
	}
		
	private final MqiConsumer<IpfPreparationJob> newConsumerFor(
			final ProductCategory category, 
			final CategoryConfig config,
			final MqiListener<IpfPreparationJob> listener
			
	) {
		LOG.debug("Creating MQI consumer for category {} using {}", category, config);
		return new MqiConsumer<IpfPreparationJob>(
				mqiClient, 
				category, 
				listener,
				config.getFixedDelayMs(),
				config.getInitDelayPollMs(),
				appStatus
		);
	}	

	private final InputTimeoutChecker inputWaitTimeoutFor(final TaskTable taskTable) {
		final List<InputWaitingConfig> configsForTasktable = new ArrayList<>();
		for (final InputWaitingConfig config : settings.getInputWaiting()) {
			if (taskTable.getProcessorName().equals(config.getProcessorNameRegexp()) &&
				taskTable.getVersion().matches(config.getProcessorVersionRegexp())) 
			{			
				configsForTasktable.add(config);
			}					
		}
		// default: always time out
		return new InputTimeoutCheckerImpl(configsForTasktable, () -> LocalDateTime.now());	
	}
}

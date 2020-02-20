package esa.s1pdgs.cpoc.ipf.preparation.worker.service;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.IpfPreparationWorkerSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.IpfPreparationWorkerSettings.CategoryConfig;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.ipf.preparation.worker.status.AppStatusImpl;
import esa.s1pdgs.cpoc.ipf.preparation.worker.tasks.AbstractJobsDispatcher;
import esa.s1pdgs.cpoc.mqi.client.MqiClient;
import esa.s1pdgs.cpoc.mqi.client.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.client.MqiListener;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfPreparationJob;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

@Service
public class IpfPreparationJobListener implements MqiListener<IpfPreparationJob> {
	private static final Logger LOG = LogManager.getLogger(IpfPreparationJobListener.class);

    private final AppStatusImpl appStatus;
    private final MqiClient mqiClient;
    private final IpfPreparationWorkerSettings ipfPreparationWorkerSettings;
    private final AbstractJobsDispatcher jobsDispatcher;
    private final ErrorRepoAppender errorAppender;
    private final ProcessConfiguration processConfiguration;

    @Autowired
    public IpfPreparationJobListener(
    		final AppStatusImpl appStatus,
			final MqiClient mqiClient,
			final IpfPreparationWorkerSettings properties,
			final AbstractJobsDispatcher jobsDispatcher,
			final ErrorRepoAppender errorAppender,
			final ProcessConfiguration processConfiguration
	) {
		this.appStatus = appStatus;
		this.mqiClient = mqiClient;
		this.ipfPreparationWorkerSettings = properties;
		this.jobsDispatcher = jobsDispatcher;
		this.errorAppender = errorAppender;
		this.processConfiguration = processConfiguration;
	}

	@PostConstruct
    public void init() {	
		final ExecutorService service = Executors.newFixedThreadPool(ipfPreparationWorkerSettings.getProductCategories().size());
		
		for (final Map.Entry<ProductCategory, CategoryConfig> entry : ipfPreparationWorkerSettings.getProductCategories().entrySet()) {				
			service.execute(newConsumerFor(entry.getKey(), entry.getValue()));
		}
    }
	
	private final MqiConsumer<IpfPreparationJob> newConsumerFor(final ProductCategory category, final CategoryConfig config) {
		LOG.debug("Creating MQI consumer for category {} using {}", category, config);
		return new MqiConsumer<IpfPreparationJob>(
				mqiClient, 
				category, 
				this,
				config.getFixedDelayMs(),
				config.getInitDelayPollMs(),
				appStatus
		);
	}		
	
	@Override
	public void onMessage(final GenericMessageDto<IpfPreparationJob> message) throws Exception {
		jobsDispatcher.dispatch(message);
	}

	@Override
	public void onTerminalError(final GenericMessageDto<IpfPreparationJob> message, final Exception error) {
		errorAppender.send(new FailedProcessingDto(
				processConfiguration.getHostname(), 
				new Date(), 
				LogUtils.toString(error), 
				message
		));
	}
}

package esa.s1pdgs.cpoc.datalifecycle.worker.service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.datalifecycle.worker.config.DataLifecycleWorkerConfigurationProperties;
import esa.s1pdgs.cpoc.datalifecycle.worker.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.datalifecycle.worker.es.ElasticsearchDAO;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.mqi.client.MessageFilter;
import esa.s1pdgs.cpoc.mqi.client.MqiClient;
import esa.s1pdgs.cpoc.mqi.client.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.model.queue.EvictionManagementJob;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;

@Service
public class DataLifecycleWorkerService {
    private final MqiClient mqiClient;
    private final List<MessageFilter> messageFilter;
    private final DataLifecycleWorkerConfigurationProperties configurationProperties;
    private final AppStatus appStatus;
    private final ErrorRepoAppender errorRepoAppender;
    private final ProcessConfiguration processConfiguration;
    private final ObsClient obsClient;
    private final ElasticsearchDAO elasticSearchDAO;

    @Autowired
    public DataLifecycleWorkerService(
    		final MqiClient mqiClient, 
    		final List<MessageFilter> messageFilter,
    		final DataLifecycleWorkerConfigurationProperties configurationProperties, 
    		final AppStatus appStatus, 
    		final ErrorRepoAppender errorRepoAppender,
    		final ProcessConfiguration processConfiguration,
    		final ObsClient obsClient, 
    		final ElasticsearchDAO elasticSearchDAO
    ) {
        this.mqiClient = mqiClient;
        this.messageFilter = messageFilter;
        this.configurationProperties = configurationProperties;
        this.appStatus = appStatus;
        this.errorRepoAppender = errorRepoAppender;
        this.processConfiguration = processConfiguration;
        this.obsClient = obsClient;
        this.elasticSearchDAO = elasticSearchDAO;
    }

    @PostConstruct
    public void initService() {
        final DataLifecycleWorkerConfigurationProperties.CategoryConfig evictionEventCategoryConfig = configurationProperties.getProductCategories()
                .get(ProductCategory.EVICTION_MANAGEMENT_JOBS);

        final MqiConsumer<EvictionManagementJob> evictionEventConsumer = new MqiConsumer<>(
                mqiClient,
                ProductCategory.EVICTION_MANAGEMENT_JOBS,
                new DataLifecycleWorkerListener(errorRepoAppender, processConfiguration, obsClient, elasticSearchDAO),
                messageFilter,
                evictionEventCategoryConfig.getFixedDelayMs(),
                evictionEventCategoryConfig.getInitDelayPolMs(),
                appStatus);

        final ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.execute(evictionEventConsumer);
    }

}

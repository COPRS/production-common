package esa.s1pdgs.cpoc.datalifecycle.worker.service;

import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.datalifecycle.worker.config.DataLifecycleWorkerConfigurationProperties;
import esa.s1pdgs.cpoc.datalifecycle.worker.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.mqi.client.MqiClient;
import esa.s1pdgs.cpoc.mqi.client.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.model.queue.EvictionManagementJob;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;

@Service
public class DataLifecycleWorkerService {

    private static final Logger LOG = LogManager.getLogger(DataLifecycleWorkerService.class);

    private final MqiClient mqiClient;
    private final DataLifecycleWorkerConfigurationProperties configurationProperties;
    private final AppStatus appStatus;
    private final ErrorRepoAppender errorRepoAppender;
    private final ProcessConfiguration processConfiguration;
    private final ObsClient obsClient;

    @Autowired
    public DataLifecycleWorkerService(MqiClient mqiClient, DataLifecycleWorkerConfigurationProperties configurationProperties, AppStatus appStatus, ErrorRepoAppender errorRepoAppender, ProcessConfiguration processConfiguration, ObsClient obsClient) {

        this.mqiClient = mqiClient;
        this.configurationProperties = configurationProperties;
        this.appStatus = appStatus;
        this.errorRepoAppender = errorRepoAppender;
        this.processConfiguration = processConfiguration;
        this.obsClient = obsClient;
    }

    @PostConstruct
    public void initService() {

        DataLifecycleWorkerConfigurationProperties.CategoryConfig evictionEventCategoryConfig = configurationProperties.getProductCategories()
                .get(ProductCategory.EVICTION_MANAGEMENT_JOBS);

        final MqiConsumer<EvictionManagementJob> evictionEventConsumer = new MqiConsumer<>(
                mqiClient,
                ProductCategory.EVICTION_MANAGEMENT_JOBS,
                new DataLifecycleWorkerListener(errorRepoAppender, processConfiguration, obsClient),
                Collections.emptyList(),
                evictionEventCategoryConfig.getFixedDelayMs(),
                evictionEventCategoryConfig.getInitDelayPolMs(),
                appStatus);

        final ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.execute(evictionEventConsumer);
    }

}

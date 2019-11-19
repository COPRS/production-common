package esa.s1pdgs.cpoc.ipf.preparation.worker.tasks.l0app;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.util.CollectionUtils;

import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJobFile;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJobState;
import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InvalidFormatProduct;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.ProcessSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.tasks.AbstractGenericConsumer;
import esa.s1pdgs.cpoc.ipf.preparation.worker.tasks.AbstractJobsDispatcher;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.model.EdrsSessionMetadata;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.client.MqiListener;
import esa.s1pdgs.cpoc.mqi.client.StatusService;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.report.FilenameReportingInput;
import esa.s1pdgs.cpoc.report.LoggerReporting;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;

public class L0AppConsumer extends AbstractGenericConsumer<IngestionEvent> implements MqiListener<IngestionEvent> {
    
    private String taskForFunctionalLog;
    
    private final MetadataClient metadataClient;
    
    private final long pollingIntervalMs;
    
    private final long pollingInitialDelayMs;

	public L0AppConsumer(final AbstractJobsDispatcher<IngestionEvent> jobDispatcher,
			final ProcessSettings processSettings, final GenericMqiClient mqiClient,
			final StatusService mqiStatusService, final AppCatalogJobClient<IngestionEvent> appDataService,
			final ErrorRepoAppender errorRepoAppender, final AppStatus appStatus, final MetadataClient metadataClient,
			final long pollingIntervalMs, final long pollingInitialDelayMs) {
		super(jobDispatcher, processSettings, mqiClient, mqiStatusService, appDataService, appStatus, errorRepoAppender,
				ProductCategory.EDRS_SESSIONS);
		this.metadataClient = metadataClient;
		this.pollingIntervalMs = pollingIntervalMs;
		this.pollingInitialDelayMs = pollingInitialDelayMs;
	}

	@PostConstruct
	public void initService() {
		appStatus.setWaiting();
		if (pollingIntervalMs > 0) {
			final ExecutorService service = Executors.newFixedThreadPool(1);
			service.execute(new MqiConsumer<IngestionEvent>(mqiClient, category, this, pollingIntervalMs,
					pollingInitialDelayMs, esa.s1pdgs.cpoc.appstatus.AppStatus.NULL));
		}
	}

    @Override
    public void onMessage(GenericMessageDto<IngestionEvent> mqiMessage) {
    
    	appStatus.setWaiting();
        final Reporting.Factory reportingFactory = new LoggerReporting.Factory("L0JobGeneration");   
        
        // First, consume message
        if (mqiMessage == null || mqiMessage.getBody() == null) {
            LOGGER.trace("[MONITOR] [step 0] No message received: continue");
            return;
        }

        // Second process message
        IngestionEvent leveldto = mqiMessage.getBody();
        String productName = leveldto.getKeyObjectStorage();

        if (leveldto.getProductType() == EdrsSessionFileType.SESSION) {

            int step = 0;
            boolean ackOk = false;
            String errorMessage = "";
            // Note: the report log of consume and global log is raised during
            // building job to get the session identifier which is the real
            // product name
            appStatus.setProcessing(mqiMessage.getId());            
    	 	
            final Reporting reporting = reportingFactory.newReporting(0);
            FailedProcessingDto failedProc = new FailedProcessingDto();
            
            try {

                // Create the EdrsSessionFile object from the consumed message
                step = 1;
                LOGGER.info(
                        "[MONITOR] [step {}] [productName {}] Building product",
                        step, leveldto.getKeyObjectStorage());
                if (leveldto.getChannelId() != 1
                        && leveldto.getChannelId() != 2) {
                    throw new InvalidFormatProduct("Invalid channel identifier "
                            + leveldto.getChannelId());
                }
                reporting.begin(
                		new FilenameReportingInput(Collections.singletonList(mqiMessage.getBody().getKeyObjectStorage())),
                		new ReportingMessage("Start job generation using  {}", mqiMessage.getBody().getKeyObjectStorage())
                );
                
                AppDataJob<IngestionEvent> appDataJob = buildJob(mqiMessage);
                
                LOGGER.debug ("== appDataJob(1) {}",appDataJob.toString());
                productName = appDataJob.getProduct().getProductName();

                // Dispatch
                step++;
                if (appDataJob.getMessages().size() == 2) {
                    LOGGER.info(
                            "[MONITOR] [step 2] [productName {}] Dispatching product",
                            productName);
                    if (appDataJob.getState() == AppDataJobState.WAITING) {
                        appDataJob.setState(AppDataJobState.DISPATCHING);
                        appDataJob = appDataService.patchJob(
                                appDataJob.getId(), appDataJob, false,
                                false, false);
                        LOGGER.debug ("== appDataJob(2) {}",appDataJob.toString());
                    }
                    jobsDispatcher.dispatch(appDataJob);
                }
                // Ack
                step++;
                ackOk = true;
            } catch (AbstractCodedException ace) {
                ackOk = false;
                errorMessage = String.format(
                        "[MONITOR] [step %d] [productName %s] [code %d] %s",
                        step, productName, ace.getCode().getCode(),
                        ace.getLogMessage());
                
                reporting.error(new ReportingMessage("[code {}] {}", ace.getCode().getCode(), ace.getLogMessage()));
                
                failedProc = new FailedProcessingDto(processSettings.getHostname(),new Date(),errorMessage, mqiMessage);
            }  
            
            // Ack and check if application shall stopped
            ackProcessing(mqiMessage, failedProc, ackOk, productName, errorMessage);

            step = 0;
            LOGGER.info("[MONITOR] [step 0] [productName {}] End", step,
                    leveldto.getKeyObjectStorage());
                       
            reporting.end(new ReportingMessage("End job generation using {}", mqiMessage.getBody().getKeyObjectStorage()));
        }

    }

	protected AppDataJob<IngestionEvent> buildJob(GenericMessageDto<IngestionEvent> mqiMessage)
            throws AbstractCodedException {
    	
    	// Check if a job is already created for message identifier
		List<AppDataJob<IngestionEvent>> existingJobs = appDataService
                .findByMessagesId(mqiMessage.getId());

        if (CollectionUtils.isEmpty(existingJobs)) {
        	final IngestionEvent sessionDto = mqiMessage.getBody();
        	final String productType = sessionDto.getProductType().name();
        	final String productName = new File(sessionDto.getProductName()).getName();
        	LOGGER.debug("Querying metadata for product {} of type {}", productName, productType); 
        	final EdrsSessionMetadata edrsSessionMetadata = metadataClient.getEdrsSession(productType, productName);
           	LOGGER.debug ("Got result {}", edrsSessionMetadata); 
        	
            // Search if session is already in progress
			List<AppDataJob<IngestionEvent>> existingJobsForSession =
                    (List<AppDataJob<IngestionEvent>>) appDataService.findByProductSessionId(sessionDto.getSessionId());

            if (CollectionUtils.isEmpty(existingJobsForSession)) {
            	LOGGER.debug ("== creating jobDTO from {}",mqiMessage ); 
                // Create the JOB
                AppDataJob<IngestionEvent> jobDto = new AppDataJob<>();
                // General details
                jobDto.setLevel(processSettings.getLevel());
                jobDto.setPod(processSettings.getHostname());
                // Messages
                jobDto.getMessages().add(mqiMessage);
                // Product
                AppDataJobProduct productDto = new AppDataJobProduct();
                productDto.setProductType(productType);
                productDto.setSessionId(sessionDto.getSessionId());
                productDto.setMissionId(edrsSessionMetadata.getMissionId());
                productDto.setStationCode(sessionDto.getStationCode());
                productDto.setProductName(sessionDto.getSessionId());
                productDto.setSatelliteId(sessionDto.getSatelliteId());
                productDto.setStartTime(edrsSessionMetadata.getStartTime());
                productDto.setStopTime(edrsSessionMetadata.getStopTime());

                if (sessionDto.getChannelId() == 1) {
                    LOGGER.debug ("== ch1 ");    
                    productDto.setRaws1(edrsSessionMetadata.getRawNames().stream().map(
                            s -> new AppDataJobFile(s))
                    		.collect(Collectors.toList()));
                } else {
                	LOGGER.debug ("== ch2 ");
                    productDto.setRaws2(edrsSessionMetadata.getRawNames().stream().map(
                            s -> new AppDataJobFile(s))
                            .collect(Collectors.toList()));
                }

                jobDto.setProduct(productDto);
               
                LOGGER.debug ("== jobDTO {}",jobDto.toString());
				AppDataJob<IngestionEvent> newJobDto = appDataService.newJob(jobDto);
                LOGGER.debug ("== newJobDto {}",newJobDto.toString());
                return newJobDto;
            } else {

                // Update pod if needed
                boolean update = false;
                boolean updateMessage = false;
                boolean updateProduct = false;
                AppDataJob<IngestionEvent> jobDto = (AppDataJob<IngestionEvent>) existingJobsForSession.get(0);
                LOGGER.debug ("== existingJobsForSession.get(0) jobDto {}", jobDto.toString());
                
                if (!jobDto.getPod().equals(processSettings.getHostname())) {
                    jobDto.setPod(processSettings.getHostname());
                    update = true;
                }                
                LOGGER.debug ("== existing message {}", jobDto.getMessages().toString());
                
				final GenericMessageDto<IngestionEvent> firstMess = (GenericMessageDto<IngestionEvent>) jobDto.getMessages().get(0);
                
				LOGGER.debug ("== firstMessage {}",firstMess.toString());
                // Updates messages if needed
                final IngestionEvent dto = firstMess.getBody();
                
                if (jobDto.getMessages().size() == 1 && dto.getChannelId() != mqiMessage.getBody().getChannelId()) {
                	LOGGER.debug ("== existing message {}",jobDto.getMessages());
                	
                    jobDto.getMessages().add(mqiMessage);
                    if (mqiMessage.getBody().getChannelId() == 1) {
                        jobDto.getProduct()
                                .setRaws1(edrsSessionMetadata.getRawNames().stream()
                                        .map(s -> new AppDataJobFile(s))
                                        .collect(Collectors.toList()));
                        
                        LOGGER.debug ("== channel1 ");    
                        
                    } else {
                        jobDto.getProduct()
                                .setRaws2(edrsSessionMetadata.getRawNames().stream()
                                        .map(s -> new AppDataJobFile(s))
                                        .collect(Collectors.toList()));
                        LOGGER.debug ("== channel2 ");
                    }
                    update = true;
                    updateMessage = true;
                    updateProduct = true;
                }
                // Update
                if (update) {                	
                    jobDto = appDataService.patchJob(jobDto.getId(),
                            jobDto, updateMessage, updateProduct, false);
                    LOGGER.debug ("== updated(1) jobDto {}", jobDto.toString());
                }
                // Return object
                return jobDto;

            }

        } else {
            // Update pod if needed
            boolean update = false;
            boolean updateMessage = false;
            boolean updateProduct = false;
            AppDataJob<IngestionEvent> jobDto = existingJobs.get(0);
            LOGGER.debug ("== existingJobs.get(0) jobDto {}", jobDto.toString());

            if (!jobDto.getPod().equals(processSettings.getHostname())) {
                jobDto.setPod(processSettings.getHostname());
                update = true;
            }
            // Update
            if (update) {
                jobDto = appDataService.patchJob(jobDto.getId(), jobDto,
                        updateMessage, updateProduct, false);
                LOGGER.debug ("== updated(2) jobDto {}", jobDto.toString());
            }
            // Return object
            return jobDto;
        }

    }

    @Override
    protected String getTaskForFunctionalLog() {
    	return this.taskForFunctionalLog;
    }
    
    @Override
    public void setTaskForFunctionalLog(String taskForFunctionalLog) {
    	this.taskForFunctionalLog = taskForFunctionalLog; 
    }
}

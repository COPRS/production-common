package esa.s1pdgs.cpoc.ipf.preparation.trigger.tasks;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.util.CollectionUtils;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobFile;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobState;
import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InvalidFormatProduct;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.ipf.preparation.trigger.config.ProcessSettings;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.model.EdrsSessionMetadata;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.StatusService;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.report.FilenameReportingInput;
import esa.s1pdgs.cpoc.report.LoggerReporting;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;

public class EdrsSessionConsumer extends AbstractGenericConsumer<CatalogEvent> {	
    private String taskForFunctionalLog;    
    private final MetadataClient metadataClient;

	public EdrsSessionConsumer(
			final ProcessSettings processSettings, 
			final GenericMqiClient mqiClient,
			final StatusService mqiStatusService, 
			final AppCatalogJobClient<CatalogEvent> appDataService,
			final ErrorRepoAppender errorRepoAppender, 
			final AppStatus appStatus, 
			final MetadataClient metadataClient
	) {
		super(processSettings, mqiClient, mqiStatusService, appDataService, appStatus, errorRepoAppender,
				ProductCategory.EDRS_SESSIONS);
		this.metadataClient = metadataClient;
	}

    @Override
    public void onMessage(final GenericMessageDto<CatalogEvent> mqiMessage) {        	
        final Reporting.Factory reportingFactory = new LoggerReporting.Factory("L0JobGeneration");   

        // Second process message
        final CatalogEvent event = mqiMessage.getBody();
        final CatalogEventAdapter eventAdapter = new CatalogEventAdapter(event);
        
        String productName = event.getKeyObjectStorage();

        if (event.getProductType().equals(EdrsSessionFileType.SESSION.toString())) {
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
                        step, event.getKeyObjectStorage());

               	final int channelId = eventAdapter.channelId();
                if (channelId != 1 && channelId != 2) {
                    throw new InvalidFormatProduct("Invalid channel identifier " + channelId);
                }
                reporting.begin(
                		new FilenameReportingInput(Collections.singletonList(mqiMessage.getBody().getKeyObjectStorage())),
                		new ReportingMessage("Start job generation using  {}", mqiMessage.getBody().getKeyObjectStorage())
                );
                
                AppDataJob<CatalogEvent> appDataJob = buildJob(mqiMessage);                
                LOGGER.debug ("== appDataJob(1) {}", appDataJob.toString());
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
                        LOGGER.debug ("== appDataJob(2) {}", appDataJob.toString());
                    }
                    publish(appDataJob, ProductFamily.EDRS_SESSION, mqiMessage.getInputKey());
                }
                // Ack
                step++;
                ackOk = true;
            } catch (final AbstractCodedException ace) {
                ackOk = false;
                errorMessage = String.format(
                        "[MONITOR] [step %d] [productName %s] [code %d] %s",
                        step, productName, ace.getCode().getCode(),
                        ace.getLogMessage());
                
                reporting.error(new ReportingMessage("[code {}] {}", ace.getCode().getCode(), ace.getLogMessage()));
                
                failedProc = new FailedProcessingDto(processSettings.getHostname(),new Date(),errorMessage, mqiMessage);
                errorRepoAppender.send(failedProc);
            }  
            step = 0;
            LOGGER.info("[MONITOR] [step 0] [productName {}] End", step, event.getKeyObjectStorage());                       
            reporting.end(new ReportingMessage("End job generation using {}", mqiMessage.getBody().getKeyObjectStorage()));
        }
    }
    
	protected AppDataJob<CatalogEvent> buildJob(final GenericMessageDto<CatalogEvent> mqiMessage)
            throws AbstractCodedException {
    	
    	// Check if a job is already created for message identifier
		final List<AppDataJob<CatalogEvent>> existingJobs = appDataService
                .findByMessagesId(mqiMessage.getId());

        if (CollectionUtils.isEmpty(existingJobs)) {
        	final CatalogEvent event = mqiMessage.getBody();
        	final String productType = event.getProductType();
        	final String productName = new File(event.getKeyObjectStorage()).getName();
        	LOGGER.debug("Querying metadata for product {} of type {}", productName, productType); 
        	final EdrsSessionMetadata edrsSessionMetadata = metadataClient.getEdrsSession(productType, productName);
           	LOGGER.debug ("Got result {}", edrsSessionMetadata); 
           	
           	final CatalogEventAdapter eventAdapter = new CatalogEventAdapter(event);
          			           	
            // Search if session is already in progress
			final List<AppDataJob<CatalogEvent>> existingJobsForSession =
                    appDataService.findByProductSessionId(eventAdapter.sessionId());

            if (CollectionUtils.isEmpty(existingJobsForSession)) {
            	LOGGER.debug ("== creating jobDTO from {}",mqiMessage ); 
                // Create the JOB
                final AppDataJob<CatalogEvent> jobDto = new AppDataJob<>();
                // General details
                jobDto.setLevel(processSettings.getLevel());
                jobDto.setPod(processSettings.getHostname());
                // Messages
                jobDto.getMessages().add(mqiMessage);
                // Product
                final AppDataJobProduct productDto = new AppDataJobProduct();
                productDto.setProductType(productType);
                productDto.setSessionId(eventAdapter.sessionId());
                productDto.setMissionId(edrsSessionMetadata.getMissionId());
                productDto.setStationCode(eventAdapter.stationCode());
                productDto.setProductName(eventAdapter.sessionId());
                productDto.setSatelliteId(eventAdapter.satelliteId());
                productDto.setStartTime(edrsSessionMetadata.getStartTime());
                productDto.setStopTime(edrsSessionMetadata.getStopTime());

                if (eventAdapter.channelId() == 1) {
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
				final AppDataJob<CatalogEvent> newJobDto = appDataService.newJob(jobDto);
                LOGGER.debug ("== newJobDto {}",newJobDto.toString());
                return newJobDto;
            } else {
                // Update pod if needed
                boolean update = false;
                boolean updateMessage = false;
                boolean updateProduct = false;
                AppDataJob<CatalogEvent> jobDto = existingJobsForSession.get(0);
                LOGGER.debug ("== existingJobsForSession.get(0) jobDto {}", jobDto.toString());
                
                if (!jobDto.getPod().equals(processSettings.getHostname())) {
                    jobDto.setPod(processSettings.getHostname());
                    update = true;
                }                
                LOGGER.debug ("== existing message {}", jobDto.getMessages().toString());
                
				final GenericMessageDto<CatalogEvent> firstMess = jobDto.getMessages().get(0);
                
				LOGGER.debug ("== firstMessage {}",firstMess.toString());
                // Updates messages if needed
                final CatalogEvent dto = firstMess.getBody();
                
             	final int queriedChannelId = new CatalogEventAdapter(dto).channelId();
             	
                if (jobDto.getMessages().size() == 1 && queriedChannelId != eventAdapter.channelId()) {
                	LOGGER.debug ("== existing message {}",jobDto.getMessages());
                	
                    jobDto.getMessages().add(mqiMessage);
                    if (eventAdapter.channelId() == 1) {
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
                return jobDto;
            }

        } else {
            // Update pod if needed
            boolean update = false;
            final boolean updateMessage = false;
            final boolean updateProduct = false;
            AppDataJob<CatalogEvent> jobDto = existingJobs.get(0);
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
            return jobDto;
        }
    }
}

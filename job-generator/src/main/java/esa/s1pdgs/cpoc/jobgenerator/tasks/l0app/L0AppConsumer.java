package esa.s1pdgs.cpoc.jobgenerator.tasks.l0app;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;

import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobDto;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobDtoState;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobFileDto;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobProductDto;
import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InvalidFormatProduct;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.jobgenerator.config.ProcessSettings;
import esa.s1pdgs.cpoc.jobgenerator.model.EdrsSessionFile;
import esa.s1pdgs.cpoc.jobgenerator.service.EdrsSessionFileService;
import esa.s1pdgs.cpoc.jobgenerator.status.AppStatus;
import esa.s1pdgs.cpoc.jobgenerator.tasks.AbstractGenericConsumer;
import esa.s1pdgs.cpoc.jobgenerator.tasks.AbstractJobsDispatcher;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.StatusService;
import esa.s1pdgs.cpoc.mqi.model.queue.EdrsSessionDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.report.LoggerReporting;
import esa.s1pdgs.cpoc.report.Reporting;

public class L0AppConsumer extends AbstractGenericConsumer<EdrsSessionDto> {
    /**
     * Service for EDRS session file
     */
    private final EdrsSessionFileService edrsService;
    
    /**
     * 
     */
    private String taskForFunctionalLog;

    public L0AppConsumer(
            final AbstractJobsDispatcher<EdrsSessionDto> jobDispatcher,
            final ProcessSettings processSettings,
            final GenericMqiClient mqiService,
            final EdrsSessionFileService edrsService,
            final StatusService mqiStatusService,
            final AppCatalogJobClient appDataService,
            final ErrorRepoAppender errorRepoAppender,
            final AppStatus appStatus) {
        super(jobDispatcher, processSettings, mqiService, mqiStatusService,
                appDataService, appStatus, errorRepoAppender, ProductCategory.EDRS_SESSIONS);
        this.edrsService = edrsService;
    }

    @Scheduled(fixedDelayString = "${process.fixed-delay-ms}", initialDelayString = "${process.initial-delay-ms}")
    public void consumeMessages() {    	
        final Reporting.Factory reportingFactory = new LoggerReporting.Factory(LOGGER, "L0JobGeneration");   
        
        // First, consume message
        GenericMessageDto<EdrsSessionDto> mqiMessage = readMessage();
        if (mqiMessage == null || mqiMessage.getBody() == null) {
            LOGGER.trace("[MONITOR] [step 0] No message received: continue");
            return;
        }

        // Second process message
        EdrsSessionDto leveldto = mqiMessage.getBody();
        String productName = leveldto.getObjectStorageKey();

        if (leveldto.getProductType() == EdrsSessionFileType.SESSION) {

            int step = 0;
            boolean ackOk = false;
            String errorMessage = "";
            // Note: the report log of consume and global log is raised during
            // building job to get the session identifier which is the real
            // product name
            appStatus.setProcessing(mqiMessage.getIdentifier());            
    	 	
            final Reporting reporting = reportingFactory.newReporting(0);
            FailedProcessingDto failedProc = new FailedProcessingDto();
            
            try {

                // Create the EdrsSessionFile object from the consumed message
                step = 1;
                LOGGER.info(
                        "[MONITOR] [step {}] [productName {}] Building product",
                        step, leveldto.getObjectStorageKey());
                if (leveldto.getChannelId() != 1
                        && leveldto.getChannelId() != 2) {
                    throw new InvalidFormatProduct("Invalid channel identifier "
                            + leveldto.getChannelId());
                }
                reporting.reportStart("Start job generation using " + mqiMessage.getBody().getObjectStorageKey());
                
                AppDataJobDto appDataJob = buildJob(mqiMessage);
                productName = appDataJob.getProduct().getProductName();

                // Dispatch
                step++;
                if (appDataJob.getMessages().size() == 2) {
                    LOGGER.info(
                            "[MONITOR] [step 2] [productName {}] Dispatching product",
                            productName);
                    if (appDataJob.getState() == AppDataJobDtoState.WAITING) {
                        appDataJob.setState(AppDataJobDtoState.DISPATCHING);
                        appDataJob = appDataService.patchJob(
                                appDataJob.getIdentifier(), appDataJob, false,
                                false, false);
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
                
                reporting.reportError("[code {}] {}", ace.getCode().getCode(), ace.getLogMessage());
                
                failedProc = new FailedProcessingDto(processSettings.getHostname(),new Date(),errorMessage, mqiMessage);
            }  
            
            // Ack and check if application shall stopped
            ackProcessing(mqiMessage, failedProc, ackOk, productName, errorMessage);

            step = 0;
            LOGGER.info("[MONITOR] [step 0] [productName {}] End", step,
                    leveldto.getObjectStorageKey());
            
            reporting.reportStop("End job generation using " + mqiMessage.getBody().getObjectStorageKey());
        }

    }

    protected AppDataJobDto buildJob(GenericMessageDto<EdrsSessionDto> mqiMessage)
            throws AbstractCodedException {
    	
        // Check if a job is already created for message identifier
        List<AppDataJobDto> existingJobs = appDataService
                .findByMessagesIdentifier(mqiMessage.getIdentifier());

        if (CollectionUtils.isEmpty(existingJobs)) {
            EdrsSessionFile file = edrsService.createSessionFile(
                    mqiMessage.getBody().getObjectStorageKey());

            // Search if session is already in progress
            List<AppDataJobDto> existingJobsForSession =
                    appDataService.findByProductSessionId(file.getSessionId());

            if (CollectionUtils.isEmpty(existingJobsForSession)) {

                // Create the JOB
                AppDataJobDto jobDto = new AppDataJobDto();
                // General details
                jobDto.setLevel(processSettings.getLevel());
                jobDto.setPod(processSettings.getHostname());
                // Messages
                jobDto.getMessages().add(mqiMessage);
                // Product
                AppDataJobProductDto productDto = new AppDataJobProductDto();
                productDto.setSessionId(file.getSessionId());
                productDto.setMissionId(mqiMessage.getBody().getMissionId());
                productDto.setProductName(file.getSessionId());
                productDto
                        .setSatelliteId(mqiMessage.getBody().getSatelliteId());
                productDto.setStartTime(DateUtils.convertToAnotherFormat(
                        file.getStartTime(), EdrsSessionFile.TIME_FORMATTER,
                        AppDataJobProductDto.TIME_FORMATTER));
                productDto.setStopTime(DateUtils.convertToAnotherFormat(
                        file.getStopTime(), EdrsSessionFile.TIME_FORMATTER,
                        AppDataJobProductDto.TIME_FORMATTER));
                if (mqiMessage.getBody().getChannelId() == 1) {
                    productDto.setRaws1(file.getRawNames().stream().map(
                            rawI -> new AppDataJobFileDto(rawI.getFileName()))
                            .collect(Collectors.toList()));
                } else {
                    productDto.setRaws2(file.getRawNames().stream().map(
                            rawI -> new AppDataJobFileDto(rawI.getFileName()))
                            .collect(Collectors.toList()));
                }

                jobDto.setProduct(productDto);
               
                return appDataService.newJob(jobDto);

            } else {

                // Update pod if needed
                boolean update = false;
                boolean updateMessage = false;
                boolean updateProduct = false;
                AppDataJobDto jobDto = existingJobsForSession.get(0);
                
                if (!jobDto.getPod().equals(processSettings.getHostname())) {
                    jobDto.setPod(processSettings.getHostname());
                    update = true;
                }
                // Updates messages if needed
                final EdrsSessionDto dto = (EdrsSessionDto) jobDto.getMessages()
                        .get(0).getBody();
                
                if (jobDto.getMessages().size() == 1 && dto.getChannelId() != mqiMessage.getBody().getChannelId()) {
                    jobDto.getMessages().add(mqiMessage);
                    if (mqiMessage.getBody().getChannelId() == 1) {
                        jobDto.getProduct()
                                .setRaws1(file.getRawNames().stream()
                                        .map(rawI -> new AppDataJobFileDto(
                                                rawI.getFileName()))
                                        .collect(Collectors.toList()));
                    } else {
                        jobDto.getProduct()
                                .setRaws2(file.getRawNames().stream()
                                        .map(rawI -> new AppDataJobFileDto(
                                                rawI.getFileName()))
                                        .collect(Collectors.toList()));
                    }
                    update = true;
                    updateMessage = true;
                    updateProduct = true;
                }
                // Update
                if (update) {
                    jobDto = appDataService.patchJob(jobDto.getIdentifier(),
                            jobDto, updateMessage, updateProduct, false);
                }
                // Return object
                return jobDto;

            }

        } else {
            // Update pod if needed
            boolean update = false;
            boolean updateMessage = false;
            boolean updateProduct = false;
            AppDataJobDto jobDto = existingJobs.get(0);

            if (!jobDto.getPod().equals(processSettings.getHostname())) {
                jobDto.setPod(processSettings.getHostname());
                update = true;
            }
            // Update
            if (update) {
                jobDto = appDataService.patchJob(jobDto.getIdentifier(), jobDto,
                        updateMessage, updateProduct, false);
            }
            // Retrun object
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

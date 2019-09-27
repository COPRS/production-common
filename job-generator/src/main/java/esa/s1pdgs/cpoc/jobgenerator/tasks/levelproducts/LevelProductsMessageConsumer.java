package esa.s1pdgs.cpoc.jobgenerator.tasks.levelproducts;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;

import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJobState;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InvalidFormatProduct;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.jobgenerator.config.L0SlicePatternSettings;
import esa.s1pdgs.cpoc.jobgenerator.config.ProcessSettings;
import esa.s1pdgs.cpoc.jobgenerator.status.AppStatus;
import esa.s1pdgs.cpoc.jobgenerator.tasks.AbstractGenericConsumer;
import esa.s1pdgs.cpoc.jobgenerator.tasks.AbstractJobsDispatcher;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.mqi.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.MqiListener;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.StatusService;
import esa.s1pdgs.cpoc.mqi.model.queue.EdrsSessionDto;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.report.LoggerReporting;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;

/**
 * @author birol_colak@net.werum
 *
 */
public class LevelProductsMessageConsumer extends AbstractGenericConsumer<ProductDto> implements MqiListener<ProductDto>{

	 /**
     * Settings used to extract information from product name
     */
    private final L0SlicePatternSettings patternSettings;

    /**
     * Pattern built from the regular expression given in configuration
     */
    private final Pattern l0SLicesPattern;
    
    private final Pattern seaCoverageCheckPattern;
    
    /**
     * 
     */
    private String taskForFunctionalLog;
    
    private final MetadataClient metadataClient;

    private final long pollingIntervalMs;
  
    public LevelProductsMessageConsumer(
            final AbstractJobsDispatcher<ProductDto> jobsDispatcher,
            final L0SlicePatternSettings patternSettings,
            final ProcessSettings processSettings,
            final GenericMqiClient mqiClient,
            final StatusService mqiStatusService,
            final AppCatalogJobClient<ProductDto> appDataService,
            final ErrorRepoAppender errorRepoAppender,
            final AppStatus appStatus,
            final MetadataClient metadataClient,
            final long pollingIntervalMs) {
        super(jobsDispatcher, processSettings, mqiClient, mqiStatusService,
                appDataService, appStatus, errorRepoAppender, ProductCategory.LEVEL_PRODUCTS);
        this.patternSettings = patternSettings;
        this.l0SLicesPattern = Pattern.compile(this.patternSettings.getRegexp(),
                Pattern.CASE_INSENSITIVE);
        this.seaCoverageCheckPattern = Pattern.compile(patternSettings.getSeaCoverageCheckPattern());
        this.metadataClient = metadataClient;
        this.pollingIntervalMs = pollingIntervalMs;
    }
    
    @PostConstruct
   	public void initService() {
    	appStatus.setWaiting();
    	if(pollingIntervalMs > 0) {
	   		final ExecutorService service = Executors.newFixedThreadPool(1);
	   		service.execute(
	   				new MqiConsumer<ProductDto>(mqiClient, category, this, pollingIntervalMs));
    	}
   	}

    
    @Override
    public void onMessage(GenericMessageDto<ProductDto> mqiMessage) {
    	appStatus.setWaiting();
    	final Reporting.Factory reportingFactory = new LoggerReporting.Factory("L1JobGeneration"); 
    	final Reporting reporting = reportingFactory.newReporting(0);
    	
        // First, consume message
        if (mqiMessage == null || mqiMessage.getBody() == null) {
            LOGGER.trace("[MONITOR] [step 0] No message received: continue");
            return;
        }
        // process message
        appStatus.setProcessing(mqiMessage.getIdentifier());
        int step = 1;
        boolean ackOk = false;
        String errorMessage = "";
        String productName = mqiMessage.getBody().getProductName();
        ProductFamily family = mqiMessage.getBody().getFamily();

        FailedProcessingDto failedProc =  new FailedProcessingDto();
        
        try {
            LOGGER.info("[MONITOR] [step 1] [productName {}] Creating job", productName);
            reporting.begin(new ReportingMessage("Start job generation using {}", productName));
            
            // S1PRO-483: check for matching products if they are over sea. If not, simply skip the
            // production
            if (seaCoverageCheckPattern.matcher(productName).matches()) {
            	final Reporting reportingSeaCheck = reportingFactory.newReporting(1);
            	reportingSeaCheck.begin(new ReportingMessage("Start checking if {} is over sea", productName));            	
            	if (metadataClient.getSeaCoverage(family, productName) <= processSettings.getMinSeaCoveragePercentage()) {
            		reportingSeaCheck.end(new ReportingMessage("Skip job generation using {} (not over ocean)", productName));
                    ackPositively(appStatus.getStatus().isStopping(), mqiMessage, productName);
                    reporting.end(new ReportingMessage("End job generation using {}", productName));
                    return;
                }
               	reportingSeaCheck.begin(new ReportingMessage("End checking if {} is over sea", productName)); 
            }        	
        	
            // Check if a job is already created for message identifier
            AppDataJob<ProductDto> appDataJob = buildJob(mqiMessage);
            productName = appDataJob.getProduct().getProductName();

            // Dispatch job
            step++;
            LOGGER.info(
                    "[MONITOR] [step 2] [productName {}] Dispatching product",
                    productName);
            if (appDataJob.getState() == AppDataJobState.WAITING) {
                appDataJob.setState(AppDataJobState.DISPATCHING);
                appDataJob = appDataService.patchJob(appDataJob.getIdentifier(),
                        appDataJob, false, false, false);
            }
            jobsDispatcher.dispatch(appDataJob);

            // Ack
            step++;
            ackOk = true;

        } catch (AbstractCodedException ace) {
            ackOk = false;
            errorMessage = String.format(
                    "[MONITOR] [step %d] [productName %s] [code %d] %s", step,
                    productName, ace.getCode().getCode(), ace.getLogMessage());
            reporting.error(new ReportingMessage("[code {}] {}", ace.getCode().getCode(), ace.getLogMessage()));
            
            failedProc = new FailedProcessingDto(processSettings.getHostname(),new Date(),errorMessage, mqiMessage);  
        }

        // Ack and check if application shall stopped
        ackProcessing(mqiMessage, failedProc, ackOk, productName, errorMessage);

        LOGGER.info("[MONITOR] [step 0] [productName {}] End", productName);
        reporting.end(new ReportingMessage("End job generation using {}", productName));
    }

    protected AppDataJob<ProductDto> buildJob(GenericMessageDto<ProductDto> mqiMessage)
            throws AbstractCodedException {
        ProductDto leveldto = mqiMessage.getBody();

        // Check if a job is already created for message identifier
        List<AppDataJob<ProductDto>> existingJobs = appDataService
                .findByMessagesIdentifier(mqiMessage.getIdentifier());

        if (CollectionUtils.isEmpty(existingJobs)) {
            // Job does not exists => create it
            Matcher m = l0SLicesPattern.matcher(leveldto.getProductName());
            if (!m.matches()) {
                throw new InvalidFormatProduct(
                        "Don't match with regular expression "
                                + this.patternSettings.getRegexp());
            }

            final String satelliteId = m.group(this.patternSettings.getMGroupSatId());
            final String missionId = m.group(this.patternSettings.getMGroupMissionId());
            final String acquisition = m.group(this.patternSettings.getMGroupAcquisition());
            final String startTime = m.group(this.patternSettings.getMGroupStartTime());
            final String stopTime = m.group(this.patternSettings.getMGroupStopTime());

            // Create the JOB
            AppDataJob<ProductDto> jobDto = new AppDataJob<>();
            // General details
            jobDto.setLevel(processSettings.getLevel());
            jobDto.setPod(processSettings.getHostname());
            // Messages
            jobDto.getMessages().add(mqiMessage);
            // Product
            AppDataJobProduct productDto = new AppDataJobProduct();
            productDto.setAcquisition(acquisition);
            productDto.setMissionId(missionId);
            productDto.setProductName(leveldto.getProductName());
            productDto.setProcessMode(leveldto.getMode());
            productDto.setSatelliteId(satelliteId);
            productDto.setStartTime(DateUtils.convertToAnotherFormat(startTime,
                    L0SlicePatternSettings.TIME_FORMATTER,
                    AppDataJobProduct.TIME_FORMATTER));
            productDto.setStopTime(DateUtils.convertToAnotherFormat(stopTime,
                    L0SlicePatternSettings.TIME_FORMATTER,
                    AppDataJobProduct.TIME_FORMATTER));
            
            // FIXME dirty workaround to get things working
            productDto.setStationCode("WILE");
                
            jobDto.setProduct(productDto);

            return appDataService.newJob(jobDto);

        } else {
            // Update pod if needed
			AppDataJob<ProductDto> jobDto = (AppDataJob<ProductDto>) existingJobs.get(0);

            if (!jobDto.getPod().equals(processSettings.getHostname())) {
                jobDto.setPod(processSettings.getHostname());
                jobDto = appDataService.patchJob(jobDto.getIdentifier(), jobDto,
                        false, false, false);
            }
            // Job already exists
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

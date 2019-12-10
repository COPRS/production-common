package esa.s1pdgs.cpoc.ipf.preparation.trigger.tasks;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.util.CollectionUtils;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobState;
import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.ipf.preparation.trigger.config.ProcessSettings;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.StatusService;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.report.FilenameReportingInput;
import esa.s1pdgs.cpoc.report.LoggerReporting;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;

/**
 * @author birol_colak@net.werum
 *
 */
public class L0SliceConsumer extends AbstractGenericConsumer<CatalogEvent> {
    private final Pattern seaCoverageCheckPattern;    
    private final MetadataClient metadataClient;
  
	public L0SliceConsumer(
			final ProcessSettings processSettings,
			final GenericMqiClient mqiClient,
			final StatusService mqiStatusService,
			final AppCatalogJobClient<CatalogEvent> appDataService, 
			final ErrorRepoAppender errorRepoAppender,
			final AppStatus appStatus, 
			final MetadataClient metadataClient
	) {
		super(processSettings, mqiClient, mqiStatusService, appDataService, appStatus,
				errorRepoAppender, ProductCategory.LEVEL_PRODUCTS);
		this.seaCoverageCheckPattern = Pattern.compile(processSettings.getSeaCoverageCheckPattern());
		this.metadataClient = metadataClient;
	}

    
    @Override
    public void onMessage(final GenericMessageDto<CatalogEvent> mqiMessage) {
    	final Reporting.Factory reportingFactory = new LoggerReporting.Factory("L1JobGeneration"); 
    	final Reporting reporting = reportingFactory.newReporting(0);
    	
        // First, consume message
        if (mqiMessage == null || mqiMessage.getBody() == null) {
            LOGGER.trace("[MONITOR] [step 0] No message received: continue");
            return;
        }
        // process message
        appStatus.setProcessing(mqiMessage.getId());
        int step = 1;
        boolean ackOk = false;
        String errorMessage = "";
        String productName = mqiMessage.getBody().getKeyObjectStorage();
        final ProductFamily family = mqiMessage.getBody().getProductFamily();

        FailedProcessingDto failedProc =  new FailedProcessingDto();
        
        try {
            LOGGER.info("[MONITOR] [step 1] [productName {}] Creating job", productName);
            reporting.begin(
            		new FilenameReportingInput(Collections.singletonList(mqiMessage.getBody().getKeyObjectStorage())),
            		new ReportingMessage("Start job generation using {}", productName)
            );
            
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
            AppDataJob<CatalogEvent> appDataJob = buildJob(mqiMessage);
            productName = appDataJob.getProduct().getProductName();

            // Dispatch job
            step++;
            LOGGER.info(
                    "[MONITOR] [step 2] [productName {}] Dispatching product",
                    productName);
            if (appDataJob.getState() == AppDataJobState.WAITING) {
                appDataJob.setState(AppDataJobState.DISPATCHING);
                appDataJob = appDataService.patchJob(appDataJob.getId(),
                        appDataJob, false, false, false);
            }
            publish(appDataJob, family, mqiMessage.getInputKey());

            // Ack
            step++;
            ackOk = true;

        } catch (final AbstractCodedException ace) {
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

    @Override
	protected AppDataJob<CatalogEvent> buildJob(final GenericMessageDto<CatalogEvent> mqiMessage)
            throws AbstractCodedException {
        final CatalogEvent event = mqiMessage.getBody();

        // Check if a job is already created for message identifier
        final List<AppDataJob<CatalogEvent>> existingJobs = appDataService
                .findByMessagesId(mqiMessage.getId());

        if (CollectionUtils.isEmpty(existingJobs)) {
        	final CatalogEventAdapter eventAdapter = new CatalogEventAdapter(event);
            
            // Create the JOB
            final AppDataJob<CatalogEvent> jobDto = new AppDataJob<>();
            // General details
            jobDto.setLevel(processSettings.getLevel());
            jobDto.setPod(processSettings.getHostname());
            // Messages
            jobDto.getMessages().add(mqiMessage);
            // Product
            final AppDataJobProduct productDto = new AppDataJobProduct();
            productDto.setAcquisition(eventAdapter.swathType());
            productDto.setMissionId(eventAdapter.missionId());
            productDto.setProductName(event.getKeyObjectStorage());
            productDto.setProcessMode(eventAdapter.processMode());
            productDto.setSatelliteId(eventAdapter.satelliteId());
            productDto.setStartTime(eventAdapter.startTime());
            productDto.setStopTime(eventAdapter.stopTime());
            productDto.setStationCode(eventAdapter.stationCode());   
           	productDto.setPolarisation(eventAdapter.polarisation()); 
            jobDto.setProduct(productDto);

            return appDataService.newJob(jobDto);

        } else {
            // Update pod if needed
			AppDataJob<CatalogEvent> jobDto = existingJobs.get(0);

            if (!jobDto.getPod().equals(processSettings.getHostname())) {
                jobDto.setPod(processSettings.getHostname());
                jobDto = appDataService.patchJob(jobDto.getId(), jobDto, false, false, false);
            }
            // Job already exists
            return jobDto;
        }
    }
}

package esa.s1pdgs.cpoc.production.trigger.tasks;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.util.CollectionUtils;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobFile;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobState;
import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.production.trigger.config.ProcessSettings;

public final class EdrsSessionConsumer extends AbstractGenericConsumer<CatalogEvent> {	
	public EdrsSessionConsumer(
			final ProcessSettings processSettings, 
			final GenericMqiClient mqiClient,
			final AppCatalogJobClient<CatalogEvent> appDataService,
			final ErrorRepoAppender errorRepoAppender, 
			final AppStatus appStatus,
			final MetadataClient metadataClient
	) {
		super(
				processSettings, 
				mqiClient, 
				appDataService, 
				appStatus, 
				errorRepoAppender,
				ProductCategory.EDRS_SESSIONS,
				metadataClient
		);
	}
	
    @Override
	protected final AppDataJob<CatalogEvent> dispatch(final GenericMessageDto<CatalogEvent> mqiMessage) 
			throws AbstractCodedException {    	
        final AppDataJob<CatalogEvent> appDataJob = buildJob(mqiMessage);                
        LOGGER.debug ("== appDataJob(1) {}", appDataJob.toString());
        final String productName = appDataJob.getProduct().getProductName();
    	
    	if (appDataJob.getMessages().size() == 2) {
            LOGGER.info("Dispatching product {}", productName);
            if (appDataJob.getState() == AppDataJobState.WAITING) {
                appDataJob.setState(AppDataJobState.DISPATCHING);
                return appDataService.patchJob(appDataJob.getId(), appDataJob, false,false, false);
            }
        }
    	return appDataJob;
	}
    
	private final AppDataJob<CatalogEvent> buildJob(final GenericMessageDto<CatalogEvent> mqiMessage)
            throws AbstractCodedException {
    	
    	// Check if a job is already created for message identifier
		final List<AppDataJob<CatalogEvent>> existingJobs = appDataService
                .findByMessagesId(mqiMessage.getId());

        if (CollectionUtils.isEmpty(existingJobs)) {
        	final CatalogEvent event = mqiMessage.getBody();
        	final String productType = event.getProductType();
        	final String productName = new File(event.getKeyObjectStorage()).getName();
           	LOGGER.debug ("Got metadata result for product {} of type {}: {}", productName, 
           			productType, event.getMetadata()); 
           	
           	final CatalogEventAdapter eventAdapter = new CatalogEventAdapter(event);
          			           	
            // Search if session is already in progress
			final List<AppDataJob<CatalogEvent>> existingJobsForSession =
                    appDataService.findByProductSessionId(eventAdapter.sessionId());

            if (CollectionUtils.isEmpty(existingJobsForSession)) {
            	LOGGER.debug ("== creating jobDTO from {}",mqiMessage ); 
            	
                // Create the JOB
                final AppDataJob<CatalogEvent> jobDto = new AppDataJob<>();
                jobDto.setLevel(processSettings.getLevel());
                jobDto.setPod(processSettings.getHostname());
                jobDto.getMessages().add(mqiMessage);
                final AppDataJobProduct productDto = new AppDataJobProduct();
                productDto.setProductType(productType);
                productDto.setSessionId(eventAdapter.sessionId());
                productDto.setMissionId(eventAdapter.missionId());
                productDto.setStationCode(eventAdapter.stationCode());
                productDto.setProductName(eventAdapter.sessionId());
                productDto.setSatelliteId(eventAdapter.satelliteId());
                productDto.setStartTime(eventAdapter.startTime());
                productDto.setStopTime(eventAdapter.stopTime());
                
                final List<AppDataJobFile> raws = raws(eventAdapter);

                if (eventAdapter.channelId() == 1) {
                    LOGGER.debug ("== ch1 ");    
                    productDto.setRaws1(raws);
                } else {
                	LOGGER.debug ("== ch2 ");
                    productDto.setRaws2(raws);
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
                    final List<AppDataJobFile> raws = raws(eventAdapter);
                    
                    if (eventAdapter.channelId() == 1) {
                        jobDto.getProduct().setRaws1(raws);                        
                        LOGGER.debug ("== channel1 ");    
                        
                    } else {
                        jobDto.getProduct().setRaws2(raws);
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
	
    
    private final List<AppDataJobFile> raws(final CatalogEventAdapter eventAdapter) {
    	return eventAdapter.listValues("rawNames").stream()
    			.map(s -> new AppDataJobFile(s))
                .collect(Collectors.toList());
    }
}

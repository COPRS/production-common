package esa.s1pdgs.cpoc.ipf.preparation.trigger.tasks;


import java.util.List;

import org.springframework.util.CollectionUtils;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobState;
import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.ipf.preparation.trigger.config.ProcessSettings;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.StatusService;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

public final class L0SegmentConsumer extends AbstractGenericConsumer<CatalogEvent> {    
	public L0SegmentConsumer(
			final ProcessSettings processSettings, 
			final GenericMqiClient mqiClient,
			final StatusService mqiStatusService, 
			final AppCatalogJobClient<CatalogEvent> appDataService,
			final ErrorRepoAppender errorRepoAppender, 
			final AppStatus appStatus,
			final MetadataClient metadataClient
	) {
		super(
				processSettings, 
				mqiClient, 
				mqiStatusService, 
				appDataService, 
				appStatus, 
				errorRepoAppender,
				ProductCategory.EDRS_SESSIONS,
				metadataClient
		);
	}
	
    @Override
	protected final AppDataJob<CatalogEvent> dispatch(final GenericMessageDto<CatalogEvent> mqiMessage) throws AbstractCodedException {
        final AppDataJob<CatalogEvent> appDataJob = buildJob(mqiMessage);
        final String productName = appDataJob.getProduct().getProductName();
        LOGGER.info("Dispatching product {}", productName);
        
        if (appDataJob.getState() == AppDataJobState.WAITING || appDataJob.getState() == AppDataJobState.DISPATCHING) {
            appDataJob.setState(AppDataJobState.DISPATCHING);
            return appDataService.patchJob(appDataJob.getId(), appDataJob, false, false, false);
        }
        LOGGER.info("Job for datatake already dispatched, product {}", productName);
        return appDataJob;
	}    

	private final AppDataJob<CatalogEvent> buildJob(
            final GenericMessageDto<CatalogEvent> mqiMessage)
            throws AbstractCodedException {
        final CatalogEvent catEvent = mqiMessage.getBody();

        // Check if a job is already created for message identifier
        final List<AppDataJob<CatalogEvent>> existingJobs = appDataService.findByMessagesId(mqiMessage.getId());

        if (CollectionUtils.isEmpty(existingJobs)) {        	
        	final CatalogEventAdapter eventAdapter = new CatalogEventAdapter(catEvent);

            // Search job for given datatake id
            final List<AppDataJob<CatalogEvent>> existingJobsForDatatake =
                    appDataService.findByProductDataTakeId(eventAdapter.datatakeId());

            if (CollectionUtils.isEmpty(existingJobsForDatatake)) {

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
                productDto.setDataTakeId(eventAdapter.datatakeId());
                productDto.setProductName("l0_segments_for_" + eventAdapter.datatakeId());
                //FIXME catalog event shall take processMode into account
                //productDto.setProcessMode(eventAdapter.processMode());
                productDto.setProcessMode("FAST24");
                productDto.setSatelliteId(eventAdapter.satelliteId());
                jobDto.setProduct(productDto);

                return appDataService.newJob(jobDto);
            } else {
				final AppDataJob<CatalogEvent> jobDto = existingJobsForDatatake.get(0);

                if (!jobDto.getPod().equals(processSettings.getHostname())) {
                    jobDto.setPod(processSettings.getHostname());
                }
                jobDto.getMessages().add(mqiMessage);
                return appDataService.patchJob(jobDto.getId(), jobDto,
                        true, false, false);

            }

        } else {
            // Update pod if needed
			AppDataJob<CatalogEvent> jobDto = existingJobs.get(0);

            if (!jobDto.getPod().equals(processSettings.getHostname())) {
                jobDto.setPod(processSettings.getHostname());
                jobDto = appDataService.patchJob(jobDto.getId(), jobDto,
                        false, false, false);
            }
            // Job already exists
            return jobDto;
        }
    }
}

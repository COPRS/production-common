package esa.s1pdgs.cpoc.preparation.worker.type.edrs;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.springframework.util.Assert;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobFile;
import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerInputsMissingException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.model.EdrsSessionMetadata;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfPreparationJob;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobInputDto;
import esa.s1pdgs.cpoc.mqi.model.queue.util.CatalogEventAdapter;
import esa.s1pdgs.cpoc.preparation.worker.model.exception.TimedOutException;
import esa.s1pdgs.cpoc.preparation.worker.service.AppCatJobService;
import esa.s1pdgs.cpoc.preparation.worker.tasktable.adapter.TaskTableAdapter;
import esa.s1pdgs.cpoc.preparation.worker.type.AbstractProductTypeAdapter;
import esa.s1pdgs.cpoc.preparation.worker.type.Product;
import esa.s1pdgs.cpoc.preparation.worker.type.ProductTypeAdapter;
import esa.s1pdgs.cpoc.xml.model.joborder.AbstractJobOrderConf;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrder;

public final class EdrsSessionTypeAdapter extends AbstractProductTypeAdapter implements ProductTypeAdapter {		
	private final MetadataClient metadataClient;
    private final AiopPropertiesAdapter aiopAdapter;
    private final EdrsSessionProductValidator validator;
      
	public EdrsSessionTypeAdapter(
			final MetadataClient metadataClient, 
			final AiopPropertiesAdapter aiopAdapter,
			final EdrsSessionProductValidator validator
	) {
		this.metadataClient = metadataClient;
		this.aiopAdapter = aiopAdapter;
		this.validator = validator;
	}
	
	@Override
	public final Optional<AppDataJob> findAssociatedJobFor(final AppCatJobService appCat,
			final CatalogEventAdapter catEvent, final AppDataJob job) throws AbstractCodedException {
		List<AppDataJob> result = appCat.findByProductSessionId(catEvent.sessionId());
		
		if (result == null || result.isEmpty()) {
			LOGGER.debug("No AppDataJob found for {}", "session" + catEvent.sessionId());
			return Optional.empty();
		}
		return Optional.of(result.get(0));
	}

	@Override
	public final Product mainInputSearch(final AppDataJob job, final TaskTableAdapter tasktableAdpter) {	
    	Assert.notNull(job, "Provided AppDataJob is null");
       	Assert.notNull(job.getProduct(), "Provided AppDataJobProduct is null");

       	final EdrsSessionProduct product = EdrsSessionProduct.of(job);
    	
        try {
        	final EdrsSessionMetadataAdapter edrsMetadata = EdrsSessionMetadataAdapter.parse(        			
        			metadataClient.getEdrsSessionFor(product.getSessionId())
        	);
        	final EdrsSessionMetadata dsib1 = edrsMetadata.getChannel1();
        	final EdrsSessionMetadata dsib2 = edrsMetadata.getChannel2();
        	
        	if (dsib1 == null) {    
        		product.setRawsForChannel(1, edrsMetadata.availableRaws1());
        	}   
        	else {        	
            	product.setDsibForChannel(1, dsib1.getKeyObjectStorage());
            	product.setRawsForChannel(1, edrsMetadata.raws1());
            	product.setStartTime(dsib1.getStartTime());
            	product.setStopTime(dsib1.getStopTime());            	
        	}
        	
        	if (dsib2 == null) { 
        		product.setRawsForChannel(2, edrsMetadata.availableRaws2());
        	} 
        	else {
        		product.setDsibForChannel(2, dsib2.getKeyObjectStorage());
               	product.setRawsForChannel(2, edrsMetadata.raws2());
            	product.setStartTime(dsib2.getStartTime());
            	product.setStopTime(dsib2.getStopTime());  
        	}
        } 
        catch (final MetadataQueryException me) {
        	LOGGER.error("Error on query execution, retrying next time", me);
        }
	    return product;
	}	

	@Override
	public final void validateInputSearch(final AppDataJob job, final TaskTableAdapter tasktableAdpter) throws IpfPrepWorkerInputsMissingException {       	
        // S1PRO-1101: if timeout for primary search is reached -> just start the job 
    	if (aiopAdapter.isTimedOut(job)) {	        		
    		throw new TimedOutException();
    	}
       	validator.assertIsComplete(EdrsSessionProduct.of(job));
	}

	@Override
	public List<AppDataJob> createAppDataJobs(final IpfPreparationJob job) {
		final AppDataJob appDataJob = AppDataJob.fromPreparationJob(job);
		// Add productType RAW, as it is not included in the TaskTable
		appDataJob.getTriggerProducts().add("RAW");
		
		final CatalogEventAdapter eventAdapter = CatalogEventAdapter.of(appDataJob);				
		final EdrsSessionProduct product = EdrsSessionProduct.of(appDataJob);		
		// IMPORTANT workaround!!! Allows to get the session identifier in exec-worker
		product.setProductName(eventAdapter.sessionId());
		product.setSessionId(eventAdapter.sessionId());
		product.setStationCode(eventAdapter.stationCode());
		
		// S1PRO-1772: logic from PIC: For chunks, start/stop time is not known, so as the default value
		// now and now+3h is used
		if (eventAdapter.productType().equals(EdrsSessionFileType.RAW.toString())) {
			final LocalDateTime now = LocalDateTime.now();
			final LocalDateTime nowPlusOffset = now.plus(
					Duration.parse(
							System.getProperty("edrs.raw.nowOffsetForDefaultStopTime","PT3H") // default: 3h
					)
			);			
			final String startTime = DateUtils.formatToMetadataDateTimeFormat(now);
			final String stopTime = DateUtils.formatToMetadataDateTimeFormat(nowPlusOffset);
			product.setStartTime(startTime);
			product.setStopTime(stopTime);
			appDataJob.setStartTime(startTime);
			appDataJob.setStopTime(stopTime);
		}
		else {
			product.setStartTime(eventAdapter.startTime());
			product.setStopTime(eventAdapter.stopTime());
			appDataJob.setStartTime(eventAdapter.startTime());
			appDataJob.setStopTime(eventAdapter.stopTime());
		}
		
		return Collections.singletonList(appDataJob);
	}
	
	@Override
	public final void customJobOrder(final AppDataJob job, final JobOrder jobOrder) {
    	final AbstractJobOrderConf conf = jobOrder.getConf();    	
    	
    	final Map<String,String> aiopParams = aiopAdapter.aiopPropsFor(job);    	
    	LOGGER.trace("Existing parameters: {}", conf.getProcParams());
    	LOGGER.trace("New AIOP parameters: {}", aiopParams);
    	
    	for (final Entry<String, String> newParam : aiopParams.entrySet()) {
    		updateProcParam(jobOrder, newParam.getKey(), newParam.getValue());
		}    	
    	LOGGER.debug("Configured AIOP for product {} of job {} with configuration {}", 
    			job.getProductName(), job.getId(), conf);		
	}

	@Override
	public final void customJobDto(final AppDataJob job, final IpfExecutionJob dto) {
        // Add input relative to the channels
        if (job.getProduct() != null) {
        	final EdrsSessionProduct product = EdrsSessionProduct.of(job);
        	
        	final List<AppDataJobFile> raws1 = product.getRawsForChannel(1);
        	final List<AppDataJobFile> raws2 = product.getRawsForChannel(2);

            // Add raw to the job order, one file per channel, alternating and in alphabetic order
            for (int i = 0; i < Math.max(raws1.size(), raws2.size()); i++) {            	            	
                if (i < raws1.size()) { 
                    dto.addInput(newInputFor(raws1.get(i), dto.getWorkDirectory(), "ch01"));
                }
                if (i < raws2.size()) {
                    dto.addInput(newInputFor(raws2.get(i), dto.getWorkDirectory(), "ch02"));
                }
            }
            
            // Add DSIB as Input (at least necessary for S3 SESSIONs)
            dto.addInput(newInputForDSIB(product.getDsibForChannel(1), dto.getWorkDirectory(), "ch01"));
            dto.addInput(newInputForDSIB(product.getDsibForChannel(2), dto.getWorkDirectory(), "ch02"));
        }		
	}

	private final LevelJobInputDto newInputFor(
			final AppDataJobFile file, 
			final String directory,
			final String channelSubdir
	) {
		final File channelFolder = new File(directory, channelSubdir);
		return new LevelJobInputDto(
				ProductFamily.EDRS_SESSION.name(),
				new File(channelFolder, file.getFilename()).getPath(), 
				file.getKeyObs()
		);
	}
	
	private final LevelJobInputDto newInputForDSIB(
			final String obsKey, 
			final String directory,
			final String channelSubdir
	) {
		final Path path = Paths.get(obsKey).getFileName();
		final File channelFolder = new File(directory, channelSubdir);
		if (path != null) {
			return new LevelJobInputDto(ProductFamily.EDRS_SESSION.name(),
					new File(channelFolder, path.toString()).getPath(), obsKey);
		}
		return new LevelJobInputDto(ProductFamily.EDRS_SESSION.name(),
				new File(channelFolder, obsKey).getPath(), obsKey);
	}
}

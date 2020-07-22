package esa.s1pdgs.cpoc.ipf.preparation.worker.type.edrs;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.springframework.util.Assert;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobFile;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerInputsMissingException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.ipf.preparation.worker.appcat.AppCatJobService;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.AbstractProductTypeAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.Product;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.ProductTypeAdapter;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobInputDto;
import esa.s1pdgs.cpoc.mqi.model.queue.util.CatalogEventAdapter;
import esa.s1pdgs.cpoc.xml.model.joborder.AbstractJobOrderConf;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrder;

public final class EdrsSessionTypeAdapter extends AbstractProductTypeAdapter implements ProductTypeAdapter {		
	private final MetadataClient metadataClient;
    private final AiopPropertiesAdapter aiopAdapter;
      
	public EdrsSessionTypeAdapter(
			final MetadataClient metadataClient, 
			final AiopPropertiesAdapter aiopAdapter
	) {
		this.metadataClient = metadataClient;
		this.aiopAdapter = aiopAdapter;
	}
	
	@Override
	public final Optional<AppDataJob> findAssociatedJobFor(final AppCatJobService appCat, final CatalogEventAdapter catEvent) 
			throws AbstractCodedException {
		return appCat.findJobForSession(catEvent.sessionId());
	}

	@Override
	public final Product mainInputSearch(final AppDataJob job) {	
    	Assert.notNull(job, "Provided AppDataJob is null");
       	Assert.notNull(job.getProduct(), "Provided AppDataJobProduct is null");

       	final EdrsSessionProduct product = EdrsSessionProduct.of(job);
    	
        try {
        	final EdrsSessionMetadataAdapter edrsMetadata = EdrsSessionMetadataAdapter.parse(        			
        			metadataClient.getEdrsSessionFor(product.getSessionId())
        	);
        	
        	if (edrsMetadata.getChannel1() == null) {    
        		product.setRawsForChannel(1, edrsMetadata.availableRaws1());
        	}   
        	else {
            	product.setDsibForChannel(1, edrsMetadata.getChannel1().getKeyObjectStorage());
            	product.setRawsForChannel(1, edrsMetadata.raws1());
        	}
        	
        	if (edrsMetadata.getChannel2() == null) { 
        		product.setRawsForChannel(2, edrsMetadata.availableRaws2());
        	} 
        	else {
        		product.setDsibForChannel(2, edrsMetadata.getChannel2().getKeyObjectStorage());
               	product.setRawsForChannel(2, edrsMetadata.raws2());
        	}
        } 
        catch (final MetadataQueryException me) {
        	LOGGER.error("Error on query execution, retrying next time", me);
//        	 throw new IpfPrepWorkerInputsMissingException(
//    			  Collections.singletonMap(
//    					product.getProductName(), 
//    					  String.format("Query error: %s", Exceptions.messageOf(me))
//    			  )
// 	    	  );
        }
	    return product;
	}
	

	@Override
	public final void validateInputSearch(final AppDataJob job) throws IpfPrepWorkerInputsMissingException {       	
        // S1PRO-1101: if timeout for primary search is reached -> just start the job 
    	if (aiopAdapter.isTimedOut(job)) {	        		
    		return;
    	}
       	final EdrsSessionProduct product = EdrsSessionProduct.of(job);       	    	

    	if (product.getDsibForChannel(1) == null) {    
    	  	throw new IpfPrepWorkerInputsMissingException(
				  Collections.singletonMap(
						  product.getProductName(), 
						  "No DSIB for channel 1"
				  )
    	  	);
    	}     
    	if (product.getDsibForChannel(2) == null) { 
    		throw new IpfPrepWorkerInputsMissingException(
				  Collections.singletonMap(
						  product.getProductName(), 
						  "No DSIB for channel 2"
				  )
          	);
    	} 
    	final Map<String,String> missingRaws = missingRawsOf(product); 
	    if (!missingRaws.isEmpty()) {
            throw new IpfPrepWorkerInputsMissingException(missingRaws);
        }
	}
	
	private final Map<String,String> missingRawsOf(final EdrsSessionProduct product) {		
    	final Map<String,String> missingRaws = new HashMap<>();
    	
    	for (final AppDataJobFile raw : product.getRawsForChannel(1)) {
    		if (raw.getKeyObs() == null) {
    			missingRaws.put(raw.getFilename(), "Missing RAW1 " + raw.getFilename());
    		}
    	}
    	for (final AppDataJobFile raw : product.getRawsForChannel(2)) {
    		if (raw.getKeyObs() == null) {
    			missingRaws.put(raw.getFilename(), "Missing RAW2 " + raw.getFilename());
    		}
    	}
    	return missingRaws;
	}

	@Override
	public final void customAppDataJob(final AppDataJob job) {			
		final CatalogEventAdapter eventAdapter = CatalogEventAdapter.of(job);				
		final EdrsSessionProduct product = EdrsSessionProduct.of(job);		
		// IMPORTANT workaround!!! Allows to get the session identifier in exec-worker
		product.setProductName(eventAdapter.sessionId());
		product.setSessionId(eventAdapter.sessionId());
		product.setStationCode(eventAdapter.stationCode());
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
}

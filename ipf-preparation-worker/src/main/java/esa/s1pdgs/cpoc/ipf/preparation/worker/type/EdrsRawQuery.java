package esa.s1pdgs.cpoc.ipf.preparation.worker.type;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerInputsMissingException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.JobGen;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.model.EdrsSessionMetadata;

final class EdrsRawQuery implements Callable<JobGen> {
	private final JobGen job;
	private final MetadataClient metadataClient;
	private final AiopPropertiesAdapter aiopAdapter;
	
	public EdrsRawQuery(final JobGen job, final MetadataClient metadataClient, final AiopPropertiesAdapter aiopAdapter) {
		this.job = job;
		this.metadataClient = metadataClient;
		this.aiopAdapter = aiopAdapter;
	}
	
	@Override
	public JobGen call() throws Exception {
        final Map<String, String> missingRaws = new HashMap<>();
        
        if (job.job() != null && job.job().getProduct() != null) {
        	
            // Channel 1
        	job.job().getProduct().getRaws1().forEach(raw -> {
                try {
                    final EdrsSessionMetadata file = metadataClient.getEdrsSession(
                    		"RAW", 
                    		new File(raw.getFilename()).getName()
                    );
                    if (file != null) {
                        raw.setKeyObs(file.getKeyObjectStorage());
                    } else {
                        missingRaws.put(raw.getFilename(), "No raw with name");
                    }
                } catch (final MetadataQueryException me) {
                    missingRaws.put(raw.getFilename(), me.getMessage());
                }
            });
            // Channel 2
        	job.job().getProduct().getRaws2().forEach(raw -> {
                try {
                    final EdrsSessionMetadata file = metadataClient.getEdrsSession(
                    		"RAW", 
                    		new File(raw.getFilename()).getName()
                    );
                    if (file != null) {
                        raw.setKeyObs(file.getKeyObjectStorage());
                    } else {
                        missingRaws.put(raw.getFilename(), "No raw with name");
                    }
                } catch (final MetadataQueryException me) {
                    missingRaws.put(raw.getFilename(), me.getMessage());
                }
            });
            
            // S1PRO-1101: if timeout for primary search is reached -> just start the job 
        	if (aiopAdapter.isTimedOut(job.job())) {	        		
        		return job;
        	}
        }
        
	    if (!missingRaws.isEmpty()) {
            throw new IpfPrepWorkerInputsMissingException(missingRaws);
        }
	    
	    // S1PRO-1065: Make sure that there is at least one RAW for each channel
	    if (job.job().getProduct().getRaws1().isEmpty()) {
	    	  throw new IpfPrepWorkerInputsMissingException(
	    			  Collections.singletonMap(
	    					  job.job().getProduct().getProductName(), 
	    					  "No raws for channel 1"
	    			  )
	    	);
	    }
	    if (job.job().getProduct().getRaws2().isEmpty()) {
	    	  throw new IpfPrepWorkerInputsMissingException(
	    			  Collections.singletonMap(
	    					  job.job().getProduct().getProductName(), 
	    					  "No raws for channel 2"
	    			  )
	    	);
	    }	
		return job;
	}
}
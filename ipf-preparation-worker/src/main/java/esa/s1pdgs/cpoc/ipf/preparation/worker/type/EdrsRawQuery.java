package esa.s1pdgs.cpoc.ipf.preparation.worker.type;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import esa.s1pdgs.cpoc.appcatalog.AppDataJobFile;
import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerInputsMissingException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.common.utils.Exceptions;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.JobGen;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;

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
	public final JobGen call() throws Exception {		
        // S1PRO-1101: if timeout for primary search is reached -> just start the job 
    	if (aiopAdapter.isTimedOut(job.job())) {	        		
    		return job;
    	}
        
        if (job.job() != null && job.job().getProduct() != null) {
            try {
            	// in case of EDRS session, the product name is the sessionId
            	final EdrsSessionMetadataAdapter edrsMetadata = EdrsSessionMetadataAdapter.parse(        			
            			metadataClient.getEdrsSessionFor(job.productName())
            	);       	
            	final List<AppDataJobFile> raws1 = edrsMetadata.expectedRaws1();
            	if (raws1 == null) {
            	  	job.job().getProduct().setRaws1(edrsMetadata.availableRaws1());
            	  	throw new IpfPrepWorkerInputsMissingException(
						  Collections.singletonMap(
								  job.job().getProduct().getProductName(), 
								  "No DSIB for channel 1"
						  )
            	  	);
            	}        	
            	job.job().getProduct().setRaws1(raws1);
            	
            	final List<AppDataJobFile> raws2 = edrsMetadata.expectedRaws2();
            	if (raws2 == null) {      	 
               	  	job.job().getProduct().setRaws2(edrsMetadata.availableRaws2());
            		throw new IpfPrepWorkerInputsMissingException(
      					  Collections.singletonMap(
      							  job.job().getProduct().getProductName(), 
      							  "No DSIB for channel 2"
      					  )
                  	);
            	}        	
            	job.job().getProduct().setRaws2(raws2);
            	
            	final Map<String, String> missingRaws = collectMissingRaws();            	
                
        	    if (!missingRaws.isEmpty()) {
                    throw new IpfPrepWorkerInputsMissingException(missingRaws);
                }
            } 
            catch (final MetadataQueryException me) {
            	 throw new IpfPrepWorkerInputsMissingException(
   	    			  Collections.singletonMap(
   	    					  job.job().getProduct().getProductName(), 
   	    					  String.format("Query error: %s", Exceptions.messageOf(me))
   	    			  )
     	    	  );
            }
        }
		return job;
	}
	
	private final Map<String, String> collectMissingRaws() {
		  final Map<String, String> missingRaws = new HashMap<>();
		  
		  for (final AppDataJobFile raw : job.job().getProduct().getRaws1()) {
			  if (raw.getKeyObs() == null) {
				  missingRaws.put(raw.getFilename(), "No raw1 with name");
			  }			  
		  }
		  
		  for (final AppDataJobFile raw : job.job().getProduct().getRaws2()) {
			  if (raw.getKeyObs() == null) {
				  missingRaws.put(raw.getFilename(), "No raw2 with name");
			  }			  
		  }
		  return missingRaws;
	}
}
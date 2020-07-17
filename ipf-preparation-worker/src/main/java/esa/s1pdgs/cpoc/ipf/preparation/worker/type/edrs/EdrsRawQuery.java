package esa.s1pdgs.cpoc.ipf.preparation.worker.type.edrs;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;

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
        	final EdrsSessionProduct product = EdrsSessionProduct.of(job.job());
        	
            try {
            	final EdrsSessionMetadataAdapter edrsMetadata = EdrsSessionMetadataAdapter.parse(        			
            			metadataClient.getEdrsSessionFor(product.getSessionId())
            	);
            	
            	if (edrsMetadata.getChannel1() == null) {    
            		product.setRawsForChannel(1, edrsMetadata.availableRaws1());
            	  	throw new IpfPrepWorkerInputsMissingException(
						  Collections.singletonMap(
								  product.getProductName(), 
								  "No DSIB for channel 1"
						  )
            	  	);
            	}        
            	product.setRawsForChannel(1, edrsMetadata.raws1());
            	
            	if (edrsMetadata.getChannel2() == null) { 
            		product.setRawsForChannel(2, edrsMetadata.availableRaws2());
            		throw new IpfPrepWorkerInputsMissingException(
      					  Collections.singletonMap(
      							  product.getProductName(), 
      							  "No DSIB for channel 2"
      					  )
                  	);
            	} 
            	product.setRawsForChannel(2, edrsMetadata.raws2());
            	
            	final Map<String,String> missingRaws = edrsMetadata.missingRaws(); 
        	    if (!missingRaws.isEmpty()) {
                    throw new IpfPrepWorkerInputsMissingException(missingRaws);
                }
            } 
            catch (final MetadataQueryException me) {
            	 throw new IpfPrepWorkerInputsMissingException(
   	    			  Collections.singletonMap(
   	    					product.getProductName(), 
   	    					  String.format("Query error: %s", Exceptions.messageOf(me))
   	    			  )
     	    	  );
            }
        }
		return job;
	}
}
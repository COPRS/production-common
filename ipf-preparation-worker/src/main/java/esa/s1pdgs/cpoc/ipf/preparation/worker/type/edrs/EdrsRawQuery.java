package esa.s1pdgs.cpoc.ipf.preparation.worker.type.edrs;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerInputsMissingException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.common.utils.Exceptions;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;

final class EdrsRawQuery implements Callable<Void> {
	private final AppDataJob job;
	private final MetadataClient metadataClient;
	private final AiopPropertiesAdapter aiopAdapter;
	
	public EdrsRawQuery(final AppDataJob job, final MetadataClient metadataClient, final AiopPropertiesAdapter aiopAdapter) {
		this.job = job;
		this.metadataClient = metadataClient;
		this.aiopAdapter = aiopAdapter;
	}
	
	@Override
	public final Void call() throws Exception {		
        // S1PRO-1101: if timeout for primary search is reached -> just start the job 
    	if (aiopAdapter.isTimedOut(job)) {	        		
    		return null;
    	}
        
        if (job != null && job.getProduct() != null) {
        	final EdrsSessionProduct product = EdrsSessionProduct.of(job);
        	
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
		return null;
	}
}
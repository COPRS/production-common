package esa.s1pdgs.cpoc.ipf.preparation.worker.type.edrs;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.appcatalog.AppDataJobFile;
import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerInputsMissingException;

public final class EdrsSessionProductValidator {
	private static final Logger LOG = LogManager.getLogger(EdrsSessionProductValidator.class);
	
	public void assertIsComplete(final EdrsSessionProduct product) throws IpfPrepWorkerInputsMissingException {  
       	final String dsib1 = product.getDsibForChannel(1);
       	final String dsib2 = product.getDsibForChannel(2);       	
       	final List<AppDataJobFile> raws1 = product.getRawsForChannel(1);
       	final List<AppDataJobFile> raws2 = product.getRawsForChannel(2);
       	
       	final Map<String,String> missing = new LinkedHashMap<>();
       	
       	// both DSIBS not available (at the moment, not possible as one of it has triggered production)
       	if (dsib1 == null && dsib2 == null) {
       		missing.put(product.getProductName(),"No DSIB for channel 1 and 2");
       	}
       	// channel 1 DSIB missing
       	else if (dsib1 == null) {
       		// check if all chunks are available for ch 2
       		missing.putAll(missingRawsForChannel(product, 2));      		
       		if (!raws1.isEmpty()) {
       			missing.put(product.getProductName(),"No DSIB for channel 1");   
       		}
       		else if (missing.isEmpty()) {
       			LOG.info("channel2 is complete for {}. Continue without channel1", product.getProductName());
       		}
       	}
       	// channel 2 DSIB missing
       	else if (dsib2 == null) {
       		// check if all chunks are available for ch 1
       		missing.putAll(missingRawsForChannel(product, 1));
       		if (!raws2.isEmpty()) {
       			missing.put(product.getProductName(),"No DSIB for channel 2"); 
       		}
       		else if (missing.isEmpty()) {
       			LOG.info("channel1 is complete for {}. Continue without channel2", product.getProductName());
       		}
       	}
       	// DSIB for both channels available
       	else {
       		missing.putAll(missingRawsForChannel(product, 1));
       		missing.putAll(missingRawsForChannel(product, 2));
       	}
	    if (!missing.isEmpty()) {
	    	  throw new IpfPrepWorkerInputsMissingException(missing);
        }
	}
	
	private final Map<String, String> missingRawsForChannel(final EdrsSessionProduct product, final int channel) {
		final Map<String,String> missingRaws = new HashMap<>();
    	for (final AppDataJobFile raw : product.getRawsForChannel(channel)) {
    		if (raw.getKeyObs() == null) {
    			missingRaws.put(raw.getFilename(), "Missing RAW" + channel + " " + raw.getFilename());
    		}
    	}
		return missingRaws;
	}
}

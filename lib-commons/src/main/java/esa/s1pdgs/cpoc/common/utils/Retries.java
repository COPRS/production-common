package esa.s1pdgs.cpoc.common.utils;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Retries {
	private static final Logger LOG = LoggerFactory.getLogger(Retries.class);
	
	public static <E> E performWithRetries(
			final Callable<E> command, 
			final String name,
			final int numRetries, 
			final long retrySleep
	) throws InterruptedException {
    	int attempt = 0;
    	while (true) {
    		try {
    			return command.call();
    		} catch (Exception e) {
    			attempt++;  
    			if (attempt > numRetries) {
    				throw new RuntimeException(
    						String.format(
    								"Error: Number of retries has exceeded while performing %s after %s attempts: %s", 
    								 name,
    								 String.valueOf(attempt),
    								 LogUtils.toString(e)
    						),
    						e
    				);
    			}  			
    			if (LOG.isWarnEnabled()) {
    				if (LOG.isDebugEnabled()) {
    					LOG.debug("Error on performing {} ({}/{}), retrying in {}ms: {}", name, attempt, numRetries+1, retrySleep, 
    							LogUtils.toString(e));
        			} 
    				else {
    					LOG.warn("Error on performing {} ({}/{}), retrying in {}ms", name, attempt, numRetries+1, retrySleep);
        			}
    			}
    			Thread.sleep(retrySleep);
    		}
    	}
	}
}

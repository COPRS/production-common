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
    		} catch (final Exception e) {
    			attempt++;  
    			// max amount of retries exceeded? 
    			if (attempt > numRetries) {
    				throwRuntimeException(name, attempt, e);  		
    			}    				
    			if (LOG.isWarnEnabled()) {
    				if (LOG.isDebugEnabled()) {
    					LOG.debug("Error on performing {} ({}/{}), retrying in {}ms: {}", name, attempt, numRetries+1, retrySleep, 
    							errorMessage(attempt, e));
        			} 
    				else {
    					LOG.warn("Error on performing {} ({}/{}), retrying in {}ms", name, attempt, numRetries+1, retrySleep);
        			}
    			}
    			Thread.sleep(retrySleep);
    		}
    	}
	}

	private static void throwRuntimeException(final String name, final int attempt, final Exception e) 
			throws InterruptedException {
		// simply propagate interruption
		if (e instanceof InterruptedException) {
			throw (InterruptedException) e;
		}		
		final Throwable cause = Exceptions.unwrap(e);
				
		throw new RuntimeException(
				String.format(
						"Error: Number of retries has exceeded while performing %s after %s attempts: %s", 
						 name,
						 String.valueOf(attempt),
						 LogUtils.toString(cause)
				),
				e
		);
	}

	
	private static final String errorMessage(final int attempt, final Exception e) {		
		final Throwable cause = Exceptions.unwrap(e);
		
		// only dump stacktrace on the first attempt to avoid boilerplate stacktrace in the log
		// first stacktrace is useful to see the initial problem that might change on retry (as observed in OBS)
		if (attempt == 1) {
			return Exceptions.toString(cause);
		}
		return Exceptions.messageOf(cause);	
	}
	

}

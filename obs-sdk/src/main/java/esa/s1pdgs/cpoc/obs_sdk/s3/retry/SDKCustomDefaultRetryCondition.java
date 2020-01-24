package esa.s1pdgs.cpoc.obs_sdk.s3.retry;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.retry.RetryPolicy;

/**
 * <p>
 * Simple retry condition that allows retries up to a certain max number of
 * retries.
 * </p>
 * 
 * @author Viveris Technologies
 */
public class SDKCustomDefaultRetryCondition
        implements RetryPolicy.RetryCondition {
	
	/**
     * Logger
     */
    private static final Logger LOGGER = LogManager.getLogger(SDKCustomDefaultRetryCondition.class);

    /**
     * Maximal number of retries
     */
    private final int maxNumberRetries;

    /**
     * Constructor
     * 
     * @param maxNumberRetries
     */
    public SDKCustomDefaultRetryCondition(final int maxNumberRetries) {
        this.maxNumberRetries = maxNumberRetries;
    }

    /**
     * @see com.amazonaws.retry.RetryPolicy.RetryCondition#shouldRetry
     */
    @Override
    public boolean shouldRetry(final AmazonWebServiceRequest originalRequest,
            final AmazonClientException exception, final int retriesAttempted) {
    	LOGGER.info("=== DEBUG: shouldRetry exception:{}",exception);
    	if(retriesAttempted < maxNumberRetries) {
    		LOGGER.info(String.format("[MONITOR] retry attempt number %s", retriesAttempted));
    	} else {
    		LOGGER.info(String.format("[MONITOR] retry stop after attempt number %s", retriesAttempted));
    	}
        return retriesAttempted < maxNumberRetries;
    }

}

package fr.viveris.s1pdgs.libs.obs_sdk.s3.retry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
    private static final Log LOGGER = LogFactory.getLog(SDKCustomDefaultRetryCondition.class);

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
    	if(retriesAttempted < maxNumberRetries) {
    		LOGGER.info(String.format("[MONITOR] retry attempt number %s", retriesAttempted));
    	} else {
    		LOGGER.info(String.format("[MONITOR] retry stop after attempt number %s", retriesAttempted));
    	}
        return retriesAttempted < maxNumberRetries;
    }

}

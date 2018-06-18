package fr.viveris.s1pdgs.libs.obs_sdk.s3.retry;

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
        return retriesAttempted < maxNumberRetries;
    }

}

package esa.s1pdgs.cpoc.obs_sdk.s3.retry;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonWebServiceRequest;

import esa.s1pdgs.cpoc.obs_sdk.s3.retry.SDKCustomDefaultRetryCondition;

/**
 * Test the custom default retry condition
 * 
 * @author Viveris Technologies
 */
public class SDKCustomDefaultRetryConditionTest {

    /**
     * Test should retry
     */
    @Test
    public void testShouldRetry() {
        SDKCustomDefaultRetryCondition retryCondition =
                new SDKCustomDefaultRetryCondition(4);

        assertTrue(retryCondition.shouldRetry(null, null, 3));
        assertTrue(retryCondition.shouldRetry(null, null, 0));
        assertFalse(retryCondition.shouldRetry(null, null, 4));
        assertFalse(retryCondition.shouldRetry(null, null, 8));

        assertFalse(retryCondition.shouldRetry(new AmazonWebServiceRequest() {
        }, new AmazonClientException("tutu"), 8));
    }
}

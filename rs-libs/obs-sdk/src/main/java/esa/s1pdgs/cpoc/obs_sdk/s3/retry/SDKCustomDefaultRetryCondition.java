/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

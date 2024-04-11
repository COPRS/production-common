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

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

package esa.s1pdgs.cpoc.obs_sdk.s3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.obs_sdk.s3.S3SdkClientException;

/**
 * Tests the OBS objects for S3
 * 
 * @author Viveris Technologies
 */
public class S3SdkClientExceptionTest {

    /**
     * Check constructors / getters / setters for ObsUploadObject
     */
    @Test
    public void sdkServiceExceptionTest() {

        S3SdkClientException error =
                new S3SdkClientException("bucket-t", "key-t", "error message");
        assertNull(error.getCause());
        assertEquals("key-t", error.getKey());
        assertEquals("bucket-t", error.getBucket());

        S3SdkClientException error2 = new S3SdkClientException("bucket-t",
                "key-t", "error 2 message", new Exception("cause error"));
        assertNotNull(error2.getCause());
        assertEquals("key-t", error2.getKey());
        assertEquals("bucket-t", error2.getBucket());
        assertEquals("cause error", error2.getCause().getMessage());
    }

    /**
     * Check getMessage for ObsUploadObject
     */
    @Test
    public void sdkServiceExceptionMessageTest() {

        S3SdkClientException error =
                new S3SdkClientException("bucket-t", "key-t", "error message");
        assertTrue(error.getMessage().contains("'msg': \"error message\""));
        assertTrue(error.getMessage().contains("'bucket': \"bucket-t\""));
        assertTrue(error.getMessage().contains("'key': \"key-t\""));

        S3SdkClientException error2 = new S3SdkClientException("bucket-t",
                "key-t", "error 2 message", new Exception("cause error"));
        assertTrue(error2.getMessage().contains("'msg': \"error 2 message\""));
        assertTrue(error2.getMessage().contains("'bucket': \"bucket-t\""));
        assertTrue(error2.getMessage().contains("'key': \"key-t\""));
    }

}

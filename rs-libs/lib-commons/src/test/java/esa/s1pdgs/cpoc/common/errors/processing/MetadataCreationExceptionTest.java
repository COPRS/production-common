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

package esa.s1pdgs.cpoc.common.errors.processing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;


/**
 * Test the exception MetadataCreationException
 */

public class MetadataCreationExceptionTest {

    /**
     * Test getters and constructors
     */
    @Test
    public void testGettersConstructors() {
        MetadataCreationException exception = new MetadataCreationException("product-name",
                "test-result", "test-status");
        
        assertEquals(ErrorCode.ES_CREATION_ERROR, exception.getCode());
        assertEquals("test-result", exception.getResult());
        assertEquals("test-status", exception.getStatus());
    }

    /**
     * Test get log message
     */
    @Test
    public void testLogMessage() {
        MetadataCreationException exception = new MetadataCreationException("product-name",
                "test-result", "test-status");
        
        String log = exception.getLogMessage();
        assertTrue(log.contains("[result test-result]"));
        assertTrue(log.contains("[status test-status]"));
        assertTrue(log.contains("[msg"));
    }


}

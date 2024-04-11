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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataExtractionException;

/**
 * Test the exception MetadataCreationException
 */
public class MetadataExtractionExceptionTest {

    /**
     * Test getters and constructors
     */
    @Test
    public void testGettersConstructors() {
        MetadataExtractionException exception =
                new MetadataExtractionException(new Exception("message error"));

        assertEquals(ErrorCode.METADATA_EXTRACTION_ERROR, exception.getCode());
        assertEquals("message error", exception.getMessage());
        assertNotNull(exception.getCause());
    }

    /**
     * Test get log message
     */
    @Test
    public void testLogMessage() {
        MetadataExtractionException exception =
                new MetadataExtractionException(new Exception("message error"));

        String log = exception.getLogMessage();
        assertTrue(log.contains("[msg message error]"));
    }

}

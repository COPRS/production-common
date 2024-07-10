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
import esa.s1pdgs.cpoc.common.errors.processing.IngestorFilePathException;

/**
 * Test the exception FilePathException
 */
public class IngestorFilePathExceptionTest {

    /**
     * Test getters and constructors
     */
    @Test
    public void testGettersConstructors() {
        IngestorFilePathException exception = new IngestorFilePathException(
                "path-test", "family", "msg exception");

        assertEquals(ErrorCode.INGESTOR_INVALID_PATH, exception.getCode());
        assertEquals("path-test", exception.getPath());
        assertEquals("family", exception.getFamily());
        assertTrue(exception.getMessage().contains("msg exception"));
    }

    /**
     * Test get log message
     */
    @Test
    public void testLogMessage() {
        IngestorFilePathException exception = new IngestorFilePathException(
                "path-test", "family", "msg exception");

        String log = exception.getLogMessage();
        assertTrue(log.contains("[path path-test]"));
        assertTrue(log.contains("[family family]"));
        assertTrue(log.contains("[msg "));
        assertTrue(log.contains("msg exception]"));
    }

}

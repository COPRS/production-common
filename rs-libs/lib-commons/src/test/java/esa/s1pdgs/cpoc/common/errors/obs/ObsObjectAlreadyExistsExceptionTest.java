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

package esa.s1pdgs.cpoc.common.errors.obs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;
import esa.s1pdgs.cpoc.common.errors.obs.ObsObjectAlreadyExistsException;

/**
 * Test the exception ObsAlreadyExist
 */
public class ObsObjectAlreadyExistsExceptionTest {

    /**
     * Test getters and constructors
     */
    @Test
    public void testGettersConstructors() {
        Throwable cause = new Exception("cause exception");
        ObsObjectAlreadyExistsException exception = new ObsObjectAlreadyExistsException(
                ProductFamily.AUXILIARY_FILE, "key-test", cause);

        assertEquals(ErrorCode.OBS_ALREADY_EXIST, exception.getCode());
        assertEquals("key-test", exception.getKey());
        assertEquals(ProductFamily.AUXILIARY_FILE, exception.getFamily());
        assertEquals(cause, exception.getCause());
        assertTrue(exception.getMessage().contains("cause exception"));
    }

    /**
     * Test get log message
     */
    @Test
    public void testLogMessage() {
        Throwable cause = new Exception("cause exception");
        ObsObjectAlreadyExistsException exception = new ObsObjectAlreadyExistsException(
                ProductFamily.AUXILIARY_FILE, "key-test", cause);

        String log = exception.getLogMessage();
        assertTrue(log.contains("[msg "));
        assertTrue(log.contains("cause exception]"));
    }
}

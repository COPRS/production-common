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

package esa.s1pdgs.cpoc.common.errors.mqi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;

/**
 * Test the class MqiStatusApiError
 * 
 * @author Viveris Technologies
 */
public class MqiStatusApiErrorTest {

    /**
     * Test the exception
     */
    @Test
    public void test() {
        MqiStatusApiError e1 = new MqiStatusApiError("error-message");
        assertEquals("error-message", e1.getMessage());
        assertEquals(ErrorCode.MQI_STATUS_API_ERROR, e1.getCode());
        assertNull(e1.getCause());

        String str1 = e1.getLogMessage();
        assertTrue(str1.contains("[msg error-message]"));

        MqiStatusApiError e2 = new MqiStatusApiError("error-message",
                new Exception("cause-message"));
        assertEquals("error-message", e2.getMessage());
        assertEquals(ErrorCode.MQI_STATUS_API_ERROR, e2.getCode());
        assertNotNull(e2.getCause());
    }

}

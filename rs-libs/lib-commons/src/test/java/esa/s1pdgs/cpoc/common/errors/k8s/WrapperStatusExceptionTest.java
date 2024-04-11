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

package esa.s1pdgs.cpoc.common.errors.k8s;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;
import esa.s1pdgs.cpoc.common.errors.k8s.WrapperStatusException;

/**
 * Test the exception WrapperStatusException
 * 
 * @author Viveris Technologies
 */
public class WrapperStatusExceptionTest {

    /**
     * Test getters and log
     */
    @Test
    public void testWrapperStatusException() {
        WrapperStatusException e1 =
                new WrapperStatusException("ip", "name", "message");
        assertEquals(ErrorCode.K8S_WRAPPER_STATUS_ERROR, e1.getCode());
        assertEquals("message", e1.getMessage());
        assertEquals("ip", e1.getIpAddress());
        assertEquals("name", e1.getName());
        assertNull(e1.getCause());

        String str1 = e1.getLogMessage();
        assertTrue(str1.contains("[podIp ip] [podName name] [msg message]"));

        WrapperStatusException e2 = new WrapperStatusException("ip", "name",
                "message", new Throwable("throw"));

        assertEquals(ErrorCode.K8S_WRAPPER_STATUS_ERROR, e2.getCode());
        assertEquals("message", e2.getMessage());
        assertEquals("throw", e2.getCause().getMessage());

        String str2 = e2.getLogMessage();
        assertTrue(str2.contains("[podIp ip] [podName name] [msg message]"));
    }

}

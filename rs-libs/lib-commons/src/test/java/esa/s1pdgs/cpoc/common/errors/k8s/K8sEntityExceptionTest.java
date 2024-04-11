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
import esa.s1pdgs.cpoc.common.errors.k8s.K8sEntityException;

/**
 * Test the exception K8sEntityException
 * 
 * @author Viveris Technologies
 */
public class K8sEntityExceptionTest {

    /**
     * Test getters and log
     */
    @Test
    public void testK8sEntityException() {
        K8sEntityException e1 =
                new K8sEntityException(ErrorCode.INTERNAL_ERROR, "message");
        assertEquals(ErrorCode.INTERNAL_ERROR, e1.getCode());
        assertEquals("message", e1.getMessage());
        assertNull(e1.getCause());

        String str1 = e1.getLogMessage();
        assertTrue(str1.contains("[msg message]"));

        K8sEntityException e2 = new K8sEntityException(ErrorCode.INTERNAL_ERROR,
                "message", new Throwable("throw"));

        assertEquals(ErrorCode.INTERNAL_ERROR, e2.getCode());
        assertEquals("message", e2.getMessage());
        assertEquals("throw", e2.getCause().getMessage());

        String str2 = e2.getLogMessage();
        assertTrue(str2.contains("[msg message]"));
    }

}

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

import org.junit.Test;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;
import esa.s1pdgs.cpoc.common.errors.k8s.K8sUnknownResourceException;

/**
 * Test the exception K8sUnknownResourceException
 * 
 * @author Viveris Technologies
 */
public class K8sUnknownResourceExceptionTest {

    /**
     * Test getters and log
     */
    @Test
    public void testK8sUnknownResourceException() {
        K8sUnknownResourceException e1 =
                new K8sUnknownResourceException("message");
        assertEquals(ErrorCode.K8S_UNKNOWN_RESOURCE, e1.getCode());
        assertEquals("message", e1.getMessage());
        assertNull(e1.getCause());
    }

}

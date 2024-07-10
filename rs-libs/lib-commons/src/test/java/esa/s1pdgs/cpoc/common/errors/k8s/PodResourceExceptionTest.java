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
import esa.s1pdgs.cpoc.common.errors.k8s.PodResourceException;

/**
 * Test the exception PodResourceException
 * 
 * @author Viveris Technologies
 */
public class PodResourceExceptionTest {

    /**
     * Test getters and log
     */
    @Test
    public void testPodResourceException() {
        PodResourceException e1 = new PodResourceException("message");
        assertEquals(ErrorCode.K8S_NO_TEMPLATE_POD, e1.getCode());
        assertEquals("message", e1.getMessage());
        assertNull(e1.getCause());

        PodResourceException e2 =
                new PodResourceException("message", new Throwable("throw"));

        assertEquals(ErrorCode.K8S_NO_TEMPLATE_POD, e2.getCode());
        assertEquals("message", e2.getMessage());
        assertEquals("throw", e2.getCause().getMessage());
    }

}

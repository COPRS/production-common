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

package esa.s1pdgs.cpoc.preparation.worker.model.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.preparation.worker.model.exception.AbstractAppDataException.ErrorCode;

public class AppCatalogJobNotFoundExceptionTest {

    @Test
    public void testConstructors() {
        AppCatalogJobNotFoundException obj =
                new AppCatalogJobNotFoundException(1254L);
        assertEquals(ErrorCode.JOB_NOT_FOUND, obj.getCode());
        assertEquals(1254L, obj.getJobId());
    }

    @Test
    public void testLogMessage() {
        AppCatalogJobNotFoundException obj =
                new AppCatalogJobNotFoundException(124L);
        String str = obj.getLogMessage();
        assertTrue(str.contains("[jobId 124]"));
    }
}

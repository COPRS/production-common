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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;
import esa.s1pdgs.cpoc.common.errors.obs.ObsUnknownObjectException;

/**
 * Test the class ObsUnknownObject
 * 
 * @author Viveris Technologies
 */
public class ObsUnknownObjectExceptionTest {

    /**
     * Test the ObsUnknownObjectException
     */
    @Test
    public void testObsUnknownObjectException() {
        ObsUnknownObjectException e1 =
                new ObsUnknownObjectException(ProductFamily.EDRS_SESSION, "key1");

        assertEquals("key1", e1.getKey());
        assertEquals(ProductFamily.EDRS_SESSION, e1.getFamily());
        assertEquals(ErrorCode.OBS_UNKOWN_OBJ, e1.getCode());
        assertNull(e1.getCause());

        String str1 = e1.getLogMessage();
        assertTrue(str1.contains("[family EDRS_SESSION]"));
        assertTrue(str1.contains("[key key1]"));
    }

}

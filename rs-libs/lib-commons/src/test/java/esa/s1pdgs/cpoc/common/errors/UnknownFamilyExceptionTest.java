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

package esa.s1pdgs.cpoc.common.errors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.errors.UnknownFamilyException;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;

/**
 * Test the class UnknownFamilyException
 * 
 * @author Viveris Technologies
 */
public class UnknownFamilyExceptionTest {

    /**
     * Test the ObsUnknownObjectException
     */
    @Test
    public void testObsUnknownFamilyException() {
        UnknownFamilyException e1 =
                new UnknownFamilyException("inv-family", "error message");

        assertEquals("inv-family", e1.getFamily());
        assertEquals(ErrorCode.UNKNOWN_FAMILY, e1.getCode());
        assertNull(e1.getCause());

        String str1 = e1.getLogMessage();
        assertTrue(str1.contains("[msg error message]"));
        assertTrue(str1.contains("[family inv-family]"));
    }

}

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

import org.junit.Test;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;

/**
 * @author Viveris Technologies
 */
public class AbstractCodedExceptionTest {

    @Test
    public void testEnumErroCode() {
        assertEquals(62, ErrorCode.values().length);
        
        assertEquals(ErrorCode.ES_CREATION_ERROR,
                ErrorCode.valueOf("ES_CREATION_ERROR"));
        assertEquals(ErrorCode.INTERNAL_ERROR,
                ErrorCode.valueOf("INTERNAL_ERROR"));
        assertEquals(ErrorCode.METADATA_FILE_EXTENSION,
                ErrorCode.valueOf("METADATA_FILE_EXTENSION"));
    }
}

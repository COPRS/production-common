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

package esa.s1pdgs.cpoc.common;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test the enumeration ApplicationLevel
 * 
 * @author Viveris Technologies
 */
public class ApplicationLevelTest {

    /**
     * Test default method of enumeration
     */
    @Test
    public void testValueOf() {
        assertEquals(12, ApplicationLevel.values().length);
        assertEquals(ApplicationLevel.L0, ApplicationLevel.valueOf("L0"));
        assertEquals(ApplicationLevel.L1, ApplicationLevel.valueOf("L1"));
        assertEquals(ApplicationLevel.L1_ETAD, ApplicationLevel.valueOf("L1_ETAD"));
        assertEquals(ApplicationLevel.L2, ApplicationLevel.valueOf("L2"));
        assertEquals(ApplicationLevel.L0_SEGMENT, ApplicationLevel.valueOf("L0_SEGMENT"));
        assertEquals(ApplicationLevel.S3_L0, ApplicationLevel.valueOf("S3_L0"));
        assertEquals(ApplicationLevel.S3_L1, ApplicationLevel.valueOf("S3_L1"));
        assertEquals(ApplicationLevel.S3_L2, ApplicationLevel.valueOf("S3_L2"));
        assertEquals(ApplicationLevel.S3_PDU, ApplicationLevel.valueOf("S3_PDU"));
        assertEquals(ApplicationLevel.S3_SYN, ApplicationLevel.valueOf("S3_SYN"));
        assertEquals(ApplicationLevel.SPP_MBU, ApplicationLevel.valueOf("SPP_MBU"));
        assertEquals(ApplicationLevel.SPP_OBS, ApplicationLevel.valueOf("SPP_OBS"));
    }
}

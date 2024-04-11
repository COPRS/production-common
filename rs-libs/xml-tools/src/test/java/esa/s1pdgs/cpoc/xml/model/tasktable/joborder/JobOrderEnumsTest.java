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

package esa.s1pdgs.cpoc.xml.model.tasktable.joborder;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import esa.s1pdgs.cpoc.xml.model.joborder.enums.JobOrderDestination;
import esa.s1pdgs.cpoc.xml.model.joborder.enums.JobOrderFileNameType;

public class JobOrderEnumsTest {

    @Test
    public void testEnumJobOrderFileNameType() {
        assertEquals(4, JobOrderFileNameType.values().length);
        assertEquals(JobOrderFileNameType.PHYSICAL,
                JobOrderFileNameType.valueOf("PHYSICAL"));
        assertEquals(JobOrderFileNameType.DIRECTORY,
                JobOrderFileNameType.valueOf("DIRECTORY"));
        assertEquals(JobOrderFileNameType.REGEXP,
                JobOrderFileNameType.valueOf("REGEXP"));
        assertEquals(JobOrderFileNameType.BLANK,
                JobOrderFileNameType.valueOf("BLANK"));

        assertEquals("", JobOrderFileNameType.BLANK.getValue());
        assertEquals("Physical", JobOrderFileNameType.PHYSICAL.getValue());
        assertEquals("Directory", JobOrderFileNameType.DIRECTORY.getValue());
        assertEquals("Regexp", JobOrderFileNameType.REGEXP.getValue());
    }

    @Test
    public void testEnumJobOrderDestination() {
        assertEquals(2, JobOrderDestination.values().length);
        assertEquals(JobOrderDestination.DB, JobOrderDestination.valueOf("DB"));
        assertEquals(JobOrderDestination.PROC,
                JobOrderDestination.valueOf("PROC"));
    }
}

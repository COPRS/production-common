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

package esa.s1pdgs.cpoc.ipf.execution.worker.config;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.common.ApplicationLevel;

/**
 * Check the application properties
 * @author Viveris Technologies
 *
 */
@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class ApplicationPropertiesTest {
    
    /**
     * Properties to test
     */
    @Autowired
    private ApplicationProperties properties;

    /**
     * Test the properties
     */
    @Test
    public void testLoadProperties() {
        assertEquals(ApplicationLevel.L0, properties.getLevel());
        assertEquals(1800, properties.getTmProcAllTasksS());
        assertEquals(600, properties.getTmProcOneTaskS());
        assertEquals(300, properties.getTmProcStopS());
        assertEquals(60, properties.getTmProcCheckStopS());
        assertEquals(20, properties.getSizeBatchUpload());
        assertEquals(5, properties.getSizeBatchDownload());
        assertEquals(12, properties.getWapNbMaxLoop());
        assertEquals(10, properties.getWapTempoS());
        assertEquals("/data/localWD", properties.getWorkingDir());
    }
}

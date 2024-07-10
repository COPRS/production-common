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

package esa.s1pdgs.cpoc.preparation.worker.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;

import esa.s1pdgs.cpoc.appcatalog.AppDataJobGeneration;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobGenerationState;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class AppDataJobGenerationTest {

    /**
     * Test constructors
     */
    @Test
    public void testConstructors() {
        AppDataJobGeneration obj = new AppDataJobGeneration();

        // check default constructor
        assertEquals(AppDataJobGenerationState.INITIAL, obj.getState());
        assertNotNull(obj.getCreationDate());
        assertNull(obj.getLastUpdateDate());

        obj.setTaskTable("task-table-1");
        obj.setState(AppDataJobGenerationState.READY);
        obj.setCreationDate(new Date());
        obj.setLastUpdateDate(new Date());

        // check setters
        assertEquals(AppDataJobGenerationState.READY, obj.getState());
        assertNotNull(obj.getCreationDate());
        assertNotNull(obj.getLastUpdateDate());
        assertEquals("task-table-1", obj.getTaskTable());

        // check toString
        String str = obj.toString();
        assertTrue(str.contains("state: READY"));
        assertTrue(str.contains("taskTable: task-table-1"));
        assertTrue(str.contains("creationDate: "));
        assertTrue(str.contains("lastUpdateDate: "));
    }

    /**
     * Check equals and hascode methods
     */
    @Test
    public void testEquals() {
        EqualsVerifier.forClass(AppDataJobGeneration.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }

}

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

package esa.s1pdgs.cpoc.ipf.execution.worker.job.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.ipf.execution.worker.job.process.TaskResult;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the object TaskResult
 * 
 * @author Viveris Technologies
 */
public class TaskResultTest {

    /**
     * Test constructors
     */
    @Test
    public void testConstructors() {
        TaskResult obj = new TaskResult("bin", 17);
        assertEquals("bin", obj.getBinary());
        assertEquals(17, obj.getExitCode());
    }

    /**
     * Test to string
     */
    @Test
    public void testToString() {
        TaskResult obj = new TaskResult("bin", 17);
        String str = obj.toString();
        assertTrue(str.contains("binary: bin"));
        assertTrue(str.contains("exitCode: 17"));
    }

    /**
     * Check equals and hascode methods
     */
    @Test
    public void testEquals() {
        EqualsVerifier.forClass(TaskResult.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }

}

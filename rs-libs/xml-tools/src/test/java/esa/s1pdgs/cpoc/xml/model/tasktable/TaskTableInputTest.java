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

package esa.s1pdgs.cpoc.xml.model.tasktable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableInput;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableInputAlternative;
import esa.s1pdgs.cpoc.xml.model.tasktable.enums.TaskTableInputMode;
import esa.s1pdgs.cpoc.xml.model.tasktable.enums.TaskTableMandatoryEnum;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * @author Viveris Technologies
 */
public class TaskTableInputTest {

    /**
     * Check equals and hascode methods
     */
    @Test
    public void equalsDtoTaskTableInput() {
        EqualsVerifier.forClass(TaskTableInput.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
    }

    /**
     * Check constructors and setters
     */
    @Test
    public void testBasic() {
        TaskTableInput obj = new TaskTableInput(
                TaskTableInputMode.NON_SLICING, TaskTableMandatoryEnum.YES);
        assertEquals(TaskTableInputMode.NON_SLICING, obj.getMode());
        assertEquals(TaskTableMandatoryEnum.YES, obj.getMandatory());
        assertEquals(0, obj.getAlternatives().size());
        obj.setId("identifier");
        obj.addAlternative(new TaskTableInputAlternative());
        assertEquals(1, obj.getAlternatives().size());
        assertEquals("identifier", obj.getId());
        assertNull(obj.getReference());
        assertEquals("identifier", obj.toLogMessage());

        obj = new TaskTableInput();
        assertEquals(TaskTableInputMode.BLANK, obj.getMode());
        assertEquals(TaskTableMandatoryEnum.NO, obj.getMandatory());
        assertEquals(0, obj.getAlternatives().size());

        obj = new TaskTableInput("refer");
        assertEquals("refer", obj.getReference());
        assertEquals("refer", obj.toLogMessage());
        assertNull(obj.getId());
    }

}

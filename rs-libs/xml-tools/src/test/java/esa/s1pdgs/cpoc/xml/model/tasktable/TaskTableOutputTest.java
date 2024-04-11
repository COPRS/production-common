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

import org.junit.Test;

import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableOuput;
import esa.s1pdgs.cpoc.xml.model.tasktable.enums.TaskTableFileNameType;
import esa.s1pdgs.cpoc.xml.model.tasktable.enums.TaskTableMandatoryEnum;
import esa.s1pdgs.cpoc.xml.model.tasktable.enums.TaskTableOutputDestination;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * @author Viveris Technologies
 */
public class TaskTableOutputTest {

    /**
     * Check equals and hascode methods
     */
    @Test
    public void equalsDtoTaskTableOuput() {
        EqualsVerifier.forClass(TaskTableOuput.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }

    /**
     * Check constructors and setters
     */
    @Test
    public void testBasic() {
        TaskTableOuput obj = new TaskTableOuput(
                TaskTableOutputDestination.PROC, TaskTableMandatoryEnum.YES,
                "dft", TaskTableFileNameType.REGEXP);
        assertEquals(TaskTableOutputDestination.PROC, obj.getDestination());
        assertEquals(TaskTableMandatoryEnum.YES, obj.getMandatory());
        assertEquals(TaskTableFileNameType.REGEXP, obj.getFileNameType());
        assertEquals("dft", obj.getType());

        obj = new TaskTableOuput();
        assertEquals(TaskTableOutputDestination.BLANK, obj.getDestination());
        assertEquals(TaskTableMandatoryEnum.NO, obj.getMandatory());
        assertEquals(TaskTableFileNameType.BLANK, obj.getFileNameType());

        obj.setMandatory(TaskTableMandatoryEnum.YES);
        obj.setType("v2");
        obj.setDestination(TaskTableOutputDestination.DB);
        obj.setFileNameType(TaskTableFileNameType.PHYSICAL);
        assertEquals(TaskTableOutputDestination.DB, obj.getDestination());
        assertEquals(TaskTableMandatoryEnum.YES, obj.getMandatory());
        assertEquals(TaskTableFileNameType.PHYSICAL, obj.getFileNameType());
        assertEquals("v2", obj.getType());
    }

}

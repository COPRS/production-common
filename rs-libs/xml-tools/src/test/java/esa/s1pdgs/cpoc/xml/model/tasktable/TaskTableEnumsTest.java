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

import esa.s1pdgs.cpoc.xml.model.tasktable.enums.TaskTableFileNameType;
import esa.s1pdgs.cpoc.xml.model.tasktable.enums.TaskTableInputMode;
import esa.s1pdgs.cpoc.xml.model.tasktable.enums.TaskTableInputOrigin;
import esa.s1pdgs.cpoc.xml.model.tasktable.enums.TaskTableOutputDestination;
import esa.s1pdgs.cpoc.xml.model.tasktable.enums.TaskTableTestEnum;

public class TaskTableEnumsTest {

    @Test
    public void testEnumTaskTableFileNameType() {
        assertEquals(4, TaskTableFileNameType.values().length);
        assertEquals(TaskTableFileNameType.PHYSICAL,
                TaskTableFileNameType.valueOf("PHYSICAL"));
        assertEquals(TaskTableFileNameType.DIRECTORY,
                TaskTableFileNameType.valueOf("DIRECTORY"));
        assertEquals(TaskTableFileNameType.REGEXP,
                TaskTableFileNameType.valueOf("REGEXP"));
        assertEquals(TaskTableFileNameType.BLANK,
                TaskTableFileNameType.valueOf("BLANK"));
    }

    @Test
    public void testEnumTaskTableTestEnum() {
        assertEquals(3, TaskTableTestEnum.values().length);
        assertEquals(TaskTableTestEnum.YES, TaskTableTestEnum.valueOf("YES"));
        assertEquals(TaskTableTestEnum.NO, TaskTableTestEnum.valueOf("NO"));
        assertEquals(TaskTableTestEnum.BLANK,
                TaskTableTestEnum.valueOf("BLANK"));
    }

    @Test
    public void testEnumTaskTableOutputDestination() {
        assertEquals(4, TaskTableOutputDestination.values().length);
        assertEquals(TaskTableOutputDestination.DB,
                TaskTableOutputDestination.valueOf("DB"));
        assertEquals(TaskTableOutputDestination.PROC,
                TaskTableOutputDestination.valueOf("PROC"));
        assertEquals(TaskTableOutputDestination.DBPROC,
                TaskTableOutputDestination.valueOf("DBPROC"));
        assertEquals(TaskTableOutputDestination.BLANK,
                TaskTableOutputDestination.valueOf("BLANK"));
    }

    @Test
    public void testEnumTaskTableInputOrigin() {
        assertEquals(4, TaskTableInputOrigin.values().length);
        assertEquals(TaskTableInputOrigin.DB,
                TaskTableInputOrigin.valueOf("DB"));
        assertEquals(TaskTableInputOrigin.PROC,
                TaskTableInputOrigin.valueOf("PROC"));
        assertEquals(TaskTableInputOrigin.LOG,
                TaskTableInputOrigin.valueOf("LOG"));
        assertEquals(TaskTableInputOrigin.BLANK,
                TaskTableInputOrigin.valueOf("BLANK"));
    }

    @Test
    public void testEnumTaskTableInputMode() {
        assertEquals(7, TaskTableInputMode.values().length);
        assertEquals(TaskTableInputMode.ALWAYS,
                TaskTableInputMode.valueOf("ALWAYS"));
        assertEquals(TaskTableInputMode.SLICING,
                TaskTableInputMode.valueOf("SLICING"));
        assertEquals(TaskTableInputMode.NON_SLICING,
                TaskTableInputMode.valueOf("NON_SLICING"));
        assertEquals(TaskTableInputMode.NRT,
                TaskTableInputMode.valueOf("NRT"));
        assertEquals(TaskTableInputMode.NTC,
                TaskTableInputMode.valueOf("NTC"));
        assertEquals(TaskTableInputMode.STC,
                TaskTableInputMode.valueOf("STC"));
        assertEquals(TaskTableInputMode.BLANK,
                TaskTableInputMode.valueOf("BLANK"));
    }
}

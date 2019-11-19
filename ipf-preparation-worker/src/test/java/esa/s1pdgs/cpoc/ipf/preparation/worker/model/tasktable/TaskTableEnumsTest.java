package esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.enums.TaskTableFileNameType;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.enums.TaskTableInputMode;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.enums.TaskTableInputOrigin;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.enums.TaskTableOutputDestination;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.enums.TaskTableTestEnum;

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
        assertEquals(3, TaskTableOutputDestination.values().length);
        assertEquals(TaskTableOutputDestination.DB,
                TaskTableOutputDestination.valueOf("DB"));
        assertEquals(TaskTableOutputDestination.PROC,
                TaskTableOutputDestination.valueOf("PROC"));
        assertEquals(TaskTableOutputDestination.BLANK,
                TaskTableOutputDestination.valueOf("BLANK"));
    }

    @Test
    public void testEnumTaskTableInputOrigin() {
        assertEquals(3, TaskTableInputOrigin.values().length);
        assertEquals(TaskTableInputOrigin.DB,
                TaskTableInputOrigin.valueOf("DB"));
        assertEquals(TaskTableInputOrigin.PROC,
                TaskTableInputOrigin.valueOf("PROC"));
        assertEquals(TaskTableInputOrigin.BLANK,
                TaskTableInputOrigin.valueOf("BLANK"));
    }

    @Test
    public void testEnumTaskTableInputMode() {
        assertEquals(4, TaskTableInputMode.values().length);
        assertEquals(TaskTableInputMode.ALWAYS,
                TaskTableInputMode.valueOf("ALWAYS"));
        assertEquals(TaskTableInputMode.SLICING,
                TaskTableInputMode.valueOf("SLICING"));
        assertEquals(TaskTableInputMode.NON_SLICING,
                TaskTableInputMode.valueOf("NON_SLICING"));
        assertEquals(TaskTableInputMode.BLANK,
                TaskTableInputMode.valueOf("BLANK"));
    }
}

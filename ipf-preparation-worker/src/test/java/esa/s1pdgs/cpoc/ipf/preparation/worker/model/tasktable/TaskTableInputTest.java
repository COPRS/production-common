package esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableInput;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableInputAlternative;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.enums.TaskTableInputMode;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.enums.TaskTableMandatoryEnum;
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

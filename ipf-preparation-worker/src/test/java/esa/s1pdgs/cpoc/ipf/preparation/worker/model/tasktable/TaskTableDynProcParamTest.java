package esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableDynProcParam;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * 
 * @author Viveris Technologies
 *
 */
public class TaskTableDynProcParamTest {

    /**
     * Check equals and hascode methods
     */
    @Test
    public void equalsDtoTaskTableDynProcParam() {
        EqualsVerifier.forClass(TaskTableDynProcParam.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
    }
    
    /**
     * Check constructors and setters
     */
    @Test
    public void testBasic() {
        TaskTableDynProcParam obj = new TaskTableDynProcParam("file", "type", "dft");
        assertEquals("file", obj.getName());
        assertEquals("type", obj.getType());
        assertEquals("dft", obj.getDefaultValue());
        
        obj = new TaskTableDynProcParam();
        obj.setName("file2");
        obj.setType("v2");
        obj.setDefaultValue("tutu");
        assertEquals("file2", obj.getName());
        assertEquals("v2", obj.getType());
        assertEquals("tutu", obj.getDefaultValue());
    }

}

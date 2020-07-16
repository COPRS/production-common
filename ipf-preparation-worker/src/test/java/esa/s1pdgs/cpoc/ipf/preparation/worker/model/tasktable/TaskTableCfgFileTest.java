package esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableCfgFile;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * 
 * @author Viveris Technologies
 *
 */
public class TaskTableCfgFileTest {

    /**
     * Check equals and hascode methods
     */
    @Test
    public void equalsDtoTaskTableCfgFile() {
        EqualsVerifier.forClass(TaskTableCfgFile.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
    }
    
    /**
     * Check constructors and setters
     */
    @Test
    public void testBasic() {
        TaskTableCfgFile obj = new TaskTableCfgFile("file", "v1");
        assertEquals("file", obj.getFileName());
        assertEquals("v1", obj.getVersion());
        
        obj = new TaskTableCfgFile();
        obj.setFileName("file2");
        obj.setVersion("v2");
        assertEquals("file2", obj.getFileName());
        assertEquals("v2", obj.getVersion());
    }

}

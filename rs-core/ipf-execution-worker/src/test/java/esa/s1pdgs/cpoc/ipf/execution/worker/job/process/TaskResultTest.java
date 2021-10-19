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

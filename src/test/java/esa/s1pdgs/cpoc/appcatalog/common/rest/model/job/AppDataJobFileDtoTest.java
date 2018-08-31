package esa.s1pdgs.cpoc.appcatalog.common.rest.model.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * 
 * @author Viveris Technologies
 *
 */
public class AppDataJobFileDtoTest {

    /**
     * Test constructors
     */
    @Test
    public void testConstructors() {
        // Check default constructor and setters
        AppDataJobFileDto obj = new AppDataJobFileDto("file");
        assertEquals("file", obj.getFilename());
        assertNull(obj.getKeyObs());
        obj.setKeyObs("key-obs");
        assertEquals("file", obj.getFilename());
        assertEquals("key-obs", obj.getKeyObs());
        
        // Check constrcutor will all fields
        obj = new AppDataJobFileDto("file2", "key-obs2");

        // check toString
        String str = obj.toString();
        assertTrue(str.contains("filename: file2"));
        assertTrue(str.contains("keyObs: key-obs2"));
    }

    /**
     * Check equals and hascode methods
     */
    @Test
    public void testEquals() {
        EqualsVerifier.forClass(AppDataJobFileDto.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }

}

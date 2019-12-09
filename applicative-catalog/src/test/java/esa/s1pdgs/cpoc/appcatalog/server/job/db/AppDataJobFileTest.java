package esa.s1pdgs.cpoc.appcatalog.server.job.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.appcatalog.AppDataJobFile;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class AppDataJobFileTest {

    /**
     * Test constructors
     */
    @Test
    public void testConstructors() {
        AppDataJobFile obj = new AppDataJobFile();
        assertNull(obj.getFilename());
        assertNull(obj.getKeyObs());

        obj = new AppDataJobFile("file-name");
        assertEquals("file-name", obj.getFilename());
        assertNull(obj.getKeyObs());

        obj = new AppDataJobFile("file-name", "key1");
        assertEquals("file-name", obj.getFilename());
        assertEquals("key1", obj.getKeyObs());

        // check setters
        obj.setFilename("tutu");
        obj.setKeyObs("toto");
        assertEquals("tutu", obj.getFilename());
        assertEquals("toto", obj.getKeyObs());

        // check toString
        String str = obj.toString();
        assertTrue(str.contains("filename: tutu"));
        assertTrue(str.contains("keyObs: toto"));
    }

    /**
     * Check equals and hascode methods
     */
    @Test
    public void testEquals() {
        EqualsVerifier.forClass(AppDataJobFile.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }

}

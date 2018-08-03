package esa.s1pdgs.cpoc.ingestor.files.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.common.FileExtension;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the object FileDescriptor
 * 
 * @author Cyrielle Gailliard
 */
public class FileDescriptorTest {

    private FileDescriptor desc;

    /**
     * Set attributes values
     */
    private void buildFileDescriptor() {
        desc = new FileDescriptor();
        desc.setRelativePath("relative-path");
        desc.setProductName("product-name");
        desc.setKeyObjectStorage("key-obs");
        desc.setHasToBePublished(false);
        desc.setProductType(EdrsSessionFileType.SESSION);
        desc.setChannel(15);
        desc.setExtension(FileExtension.DAT);
        desc.setMissionId("mission");
        desc.setSatelliteId("sat");
    }

    /**
     * Test getters, setters and constructors
     */
    @Test
    public void testGettersSettersConstructors() {
        desc = new FileDescriptor();
        assertNull(desc.getRelativePath());
        assertNull(desc.getProductName());
        assertNull(desc.getKeyObjectStorage());
        assertTrue(desc.isHasToBePublished());
        assertNull(desc.getProductType());
        assertEquals(-1, desc.getChannel());
        assertEquals(FileExtension.UNKNOWN, desc.getExtension());
        assertNull(desc.getMissionId());
        assertNull(desc.getSatelliteId());

        this.buildFileDescriptor();
        assertEquals("relative-path", desc.getRelativePath());
        assertEquals("product-name", desc.getProductName());
        assertEquals("key-obs", desc.getKeyObjectStorage());
        assertFalse(desc.isHasToBePublished());
        assertEquals(EdrsSessionFileType.SESSION, desc.getProductType());
        assertEquals(15, desc.getChannel());
        assertEquals(FileExtension.DAT, desc.getExtension());
        assertEquals("mission", desc.getMissionId());
        assertEquals("sat", desc.getSatelliteId());
    }

    @Test
    public void testToString() {
        this.buildFileDescriptor();

        String str = desc.toString();
        assertTrue(str.contains("relativePath: relative-path"));
        assertTrue(str.contains("productName: product-name"));
        assertTrue(str.contains("keyObjectStorage: key-obs"));
        assertTrue(str.contains("hasToBePublished: false"));
        assertTrue(str.contains("productType: SESSION"));
        assertTrue(str.contains("channel: 15"));
        assertTrue(str.contains("extension: DAT"));
        assertTrue(str.contains("missionId: mission"));
        assertTrue(str.contains("satelliteId: sat"));
    }

    /**
     * Check equals and hashcode methods
     */
    @Test
    public void checkEquals() {
        EqualsVerifier.forClass(FileDescriptor.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }

}

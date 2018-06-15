package fr.viveris.s1pdgs.libs.obs_sdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Tests the OBS objects
 * 
 * @author Viveris Technologies
 */
public class ObsObjectTest {

    /**
     * Test the enum ObsFamily
     */
    @Test
    public void testObsFamilyValueOf() {
        assertEquals(ObsFamily.AUXILIARY_FILE,
                ObsFamily.valueOf("AUXILIARY_FILE"));
    }

    // ---------------------------------------------------
    // ObsObject
    // ---------------------------------------------------

    /**
     * Check constructors / getters / setters for ObsObject
     */
    @Test
    public void obsObjTest() {
        ObsObject obj = new ObsObject("key-t", ObsFamily.L0_ACN);
        assertEquals("key-t", obj.getKey());
        assertEquals(ObsFamily.L0_ACN, obj.getFamily());

        obj.setKey("2eme-key");
        assertEquals("2eme-key", obj.getKey());

        obj.setFamily(ObsFamily.AUXILIARY_FILE);
        assertEquals(ObsFamily.AUXILIARY_FILE, obj.getFamily());
    }

    /**
     * Check the toString function for ObjObject
     */
    @Test
    public void obsObjTestToString() {
        ObsObject obj = new ObsObject("key-t", ObsFamily.L0_ACN);
        String str = obj.toString();
        assertTrue(str.contains("key: key-t"));
        assertTrue(str.contains("family: L0_ACN"));
    }

    /**
     * Check the equals and hashCode methods for ObsObject
     */
    @Test
    public void obsObjCheckEqualsAndHash() {
        EqualsVerifier.forClass(ObsObject.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }

    // ---------------------------------------------------
    // ObsDownloadObject
    // ---------------------------------------------------

    /**
     * Check constructors / getters / setters for ObsDownloadObject
     */
    @Test
    public void obsDwObjTest() {
        ObsDownloadObject obj =
                new ObsDownloadObject("key-t", ObsFamily.L0_ACN, "targetDir-t");
        assertEquals("key-t", obj.getKey());
        assertEquals(ObsFamily.L0_ACN, obj.getFamily());
        assertEquals("targetDir-t", obj.getTargetDir());
        assertFalse(obj.isIgnoreFolders());

        obj.setKey("2eme-key");
        assertEquals("2eme-key", obj.getKey());

        obj.setFamily(ObsFamily.AUXILIARY_FILE);
        assertEquals(ObsFamily.AUXILIARY_FILE, obj.getFamily());

        obj.setTargetDir("2eme-local-path");
        assertEquals("2eme-local-path", obj.getTargetDir());

        obj.setIgnoreFolders(true);
        assertTrue(obj.isIgnoreFolders());
    }

    /**
     * Check the toString function for ObsDownloadObject
     */
    @Test
    public void obsDownObjTestToString() {
        ObsDownloadObject obj = new ObsDownloadObject("key-t",
                ObsFamily.EDRS_SESSION, "local-path-t");
        String str = obj.toString();
        assertTrue(str.contains("key: key-t"));
        assertTrue(str.contains("family: EDRS_SESSION"));
        assertTrue(str.contains("targetDir: local-path-t"));
    }

    /**
     * Check the ignoreFodlers intialization
     */
    @Test
    public void obsDownObjTestIgnoreFolders() {
        ObsDownloadObject obj = new ObsDownloadObject("key-t",
                ObsFamily.EDRS_SESSION, "local-path-t");
        assertTrue(obj.isIgnoreFolders());

        obj = new ObsDownloadObject("key-t", ObsFamily.AUXILIARY_FILE,
                "local-path-t");
        assertFalse(obj.isIgnoreFolders());

        obj = new ObsDownloadObject("key-t", ObsFamily.L0_PRODUCT,
                "local-path-t");
        assertFalse(obj.isIgnoreFolders());

        obj = new ObsDownloadObject("key-t", ObsFamily.L0_ACN, "local-path-t");
        assertFalse(obj.isIgnoreFolders());

        obj = new ObsDownloadObject("key-t", ObsFamily.L1_PRODUCT,
                "local-path-t");
        assertFalse(obj.isIgnoreFolders());

        obj = new ObsDownloadObject("key-t", ObsFamily.L1_ACN, "local-path-t");
        assertFalse(obj.isIgnoreFolders());
    }

    /**
     * Check the equals and hashCode methods for ObsDownloadObject
     */
    @Test
    public void obsDownObjCheckEqualsAndHash() {
        EqualsVerifier.forClass(ObsDownloadObject.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }

    // ---------------------------------------------------
    // ObsUploadObject
    // ---------------------------------------------------

    /**
     * Check constructors / getters / setters for ObsUploadObject
     */
    @Test
    public void obsUpObjTest() {
        File file = new File("./test");
        ObsUploadObject obj =
                new ObsUploadObject("key-t", ObsFamily.L0_ACN, file);
        assertEquals("key-t", obj.getKey());
        assertEquals(ObsFamily.L0_ACN, obj.getFamily());
        assertEquals(file, obj.getFile());

        obj.setKey("2eme-key");
        assertEquals("2eme-key", obj.getKey());

        obj.setFamily(ObsFamily.AUXILIARY_FILE);
        assertEquals(ObsFamily.AUXILIARY_FILE, obj.getFamily());

        File file2 = new File("./test2");
        obj.setFile(file2);
        assertEquals(file2, obj.getFile());
    }

    /**
     * Check the toString function for ObsUploadObject
     */
    @Test
    public void obsUpObjTestToString() {
        File file = new File("./test");
        ObsUploadObject obj =
                new ObsUploadObject("key-t", ObsFamily.EDRS_SESSION, file);
        String str = obj.toString();
        assertTrue(str.contains("key: key-t"));
        assertTrue(str.contains("family: EDRS_SESSION"));
        assertTrue(str.contains("file: " + file));
    }

    /**
     * Check the equals and hashCode methods for ObsUploadObject
     */
    @Test
    public void obsUpObjCheckEqualsAndHash() {
        EqualsVerifier.forClass(ObsUploadObject.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }

    // ---------------------------------------------------
    // ObsException
    // ---------------------------------------------------

    /**
     * Check constructors / getters / setters for ObsUploadObject
     */
    @Test
    public void obsServiceExceptionTest() {

        ObsServiceException error = new ObsServiceException("error message");
        assertNull(error.getCause());
        assertEquals("error message", error.getMessage());

        ObsServiceException error2 = new ObsServiceException("error 2 message",
                new Exception("cause error"));
        assertNotNull(error2.getCause());
        assertEquals("cause error", error2.getCause().getMessage());
        assertEquals("error 2 message", error2.getMessage());
    }

    /**
     * Check constructors / getters / setters for ObsUploadObject
     */
    @Test
    public void sdkServiceExceptionTest() {

        SdkClientException error = new SdkClientException("error message");
        assertNull(error.getCause());
        assertEquals("error message", error.getMessage());

        SdkClientException error2 = new SdkClientException("error 2 message",
                new Exception("cause error"));
        assertNotNull(error2.getCause());
        assertEquals("cause error", error2.getCause().getMessage());
        assertEquals("error 2 message", error2.getMessage());
    }
}

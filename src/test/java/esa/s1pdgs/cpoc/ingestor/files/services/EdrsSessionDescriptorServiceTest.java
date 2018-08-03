package esa.s1pdgs.cpoc.ingestor.files.services;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import esa.s1pdgs.cpoc.common.errors.processing.IngestorFilePathException;
import esa.s1pdgs.cpoc.ingestor.FileUtils;

/**
 * Test the FileDescriptorService for auxiliary files
 * 
 * @author Cyrielle Gailliard
 */
public class EdrsSessionDescriptorServiceTest {

    /**
     * Wanted family
     */
    protected static final String FAMILY = "SESSION";

    /**
     * Service to test
     */
    private EdrsSessionFileDescriptorService service;

    /**
     * To check the raised custom exceptions
     */
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Initialization
     */
    @Before
    public void init() {
        service = new EdrsSessionFileDescriptorService(
                FileUtils.TEST_DIR_ABS_PATH_SEP);
    }

    /**
     * Check given str matches with pattern of the service
     * 
     * @param str
     */
    private void checkMatchOk(String str) {
        Matcher matcher = service.getPattern().matcher(str);
        assertTrue(str + " should match with pattern", matcher.matches());
    }

    /**
     * Check given str does not match with pattern of the service
     * 
     * @param str
     */
    private void checkMatchKo(String str) {
        Matcher matcher = service.getPattern().matcher(str);
        assertFalse(str + " should not match with pattern", matcher.matches());
    }

    /**
     * Test getters and constructors
     */
    @Test
    public void testGettersContructor() {
        assertEquals(FAMILY, service.getFamily());
        assertEquals(FileUtils.TEST_DIR_ABS_PATH_SEP, service.getDirectory());
    }

    /**
     * Test the pattern of the service
     */
    @Test
    public void testPattern() {
        checkMatchOk(
                "S1A/707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00001.raw");
        checkMatchOk(
                "S1B/707000180/ch02/DCS_02_L20171109175634707000180_ch1_DSIB.xml");

        checkMatchKo("S1B");
        checkMatchKo("S1B/");
        checkMatchKo("S1B/707000180");
        checkMatchKo("S1B/707000180/");
        checkMatchKo("S1B/707000180/ch01");
        checkMatchKo("S1B/707000180/ch01/");
        checkMatchKo(
                "S1B/707000180/ch01/DCS_02_L20171109175634707000181_ch1_DSIB.xml");
        checkMatchKo(
                "S1B/707000180/ch03/DCS_02_L20171109175634707000180_ch1_DSIB.xml");
    }

    /**
     * Test buildDescriptor when relative path does not match the pattern
     */
    @Test
    public void testBuildDescriptorWhenNoMatch()
            throws IngestorFilePathException {
        thrown.expect(IngestorFilePathException.class);
        thrown.expect(
                hasProperty("path", is(FileUtils.RPATH_SESSION_NO_MATCH)));
        thrown.expect(hasProperty("family", is(FAMILY)));
        service.buildDescriptor(FileUtils.RPATH_SESSION_NO_MATCH);
    }

    /**
     * Test buildDescriptor when relative path matches the pattern
     */
    @Test
    public void testBuildDescriptor() throws IngestorFilePathException {

        assertEquals("Invalid file descriptor for RPATH_SESSION_RAW",
                FileUtils.getFileDescriptorForEdrsSession(
                        FileUtils.RPATH_SESSION_RAW),
                service.buildDescriptor(FileUtils.RPATH_SESSION_RAW));
        assertEquals("Invalid file descriptor for RPATH_SESSION_XML",
                FileUtils.getFileDescriptorForEdrsSession(
                        FileUtils.RPATH_SESSION_XML),
                service.buildDescriptor(FileUtils.RPATH_SESSION_XML));
    }

    /**
     * Test buildDescriptor when relative path contains iif
     */
    @Test
    public void testBuildDescriptorForIif() throws IngestorFilePathException {
        thrown.expect(IngestorFilePathException.class);
        thrown.expect(hasProperty("path", is(FileUtils.RPATH_SESSION_XML_IIF)));
        thrown.expect(hasProperty("family", is(FAMILY)));
        thrown.expectMessage(containsString("IIF file"));
        service.buildDescriptor(FileUtils.RPATH_SESSION_XML_IIF);
    }

}

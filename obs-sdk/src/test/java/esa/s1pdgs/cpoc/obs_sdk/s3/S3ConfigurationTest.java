package esa.s1pdgs.cpoc.obs_sdk.s3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.amazonaws.services.s3.AmazonS3;

import esa.s1pdgs.cpoc.obs_sdk.ObsFamily;
import esa.s1pdgs.cpoc.obs_sdk.ObsServiceException;
import esa.s1pdgs.cpoc.obs_sdk.s3.S3Configuration;

/**
 * Test the Amazon S3 configuration
 * @author Viveris Technologies
 *
 */
public class S3ConfigurationTest {

    /**
     * Check the configuration load
     * @throws ObsServiceException
     */
    @Test
    public void testLoadProperties() throws ObsServiceException {
        S3Configuration config = new S3Configuration();
        
        assertEquals("RU7QIAHIFSM4MHPXJUAM", config.getConfiguration()
                .getString(S3Configuration.USER_ID));
        assertEquals("Hii85WDNXQyt21YzScOhcMhlt2MRKJ4ReBGeaHqI", config.getConfiguration()
                .getString(S3Configuration.USER_SECRET));
        assertEquals("http://oss.eu-west-0.prod-cloud-ocb.orange-business.com/", config.getConfiguration()
                .getString(S3Configuration.ENDPOINT));
        assertEquals("eu-west-0", config.getConfiguration()
                .getString(S3Configuration.ENDPOINT_REGION));
        

        assertEquals("auxiliary-files", config.getConfiguration()
                .getString(S3Configuration.BCK_AUX_FILES));
        assertEquals("session-files", config.getConfiguration()
                .getString(S3Configuration.BCK_EDRS_SESSIONS));
        assertEquals("l0-slices", config.getConfiguration()
                .getString(S3Configuration.BCK_L0_SLICES));
        assertEquals("acns", config.getConfiguration()
                .getString(S3Configuration.BCK_L0_ACNS));
        assertEquals("l1-slices", config.getConfiguration()
                .getString(S3Configuration.BCK_L1_SLICES));
        assertEquals("l1-acns", config.getConfiguration()
                .getString(S3Configuration.BCK_L1_ACNS));

        assertEquals("10", config.getConfiguration()
                .getString(S3Configuration.TM_S_SHUTDOWN));
        assertEquals("15", config.getConfiguration()
                .getString(S3Configuration.TM_S_DOWN_EXEC));
        assertEquals("20", config.getConfiguration()
                .getString(S3Configuration.TM_S_UP_EXEC));
    }
    
    /**
     * Check that bucket returned according the family is right
     * @throws ObsServiceException
     */
    @Test
    public void testBucketForFamily() throws ObsServiceException {
        S3Configuration config = new S3Configuration();
        
        assertEquals("auxiliary-files", config.getBucketForFamily(ObsFamily.AUXILIARY_FILE));
        assertEquals("session-files", config.getBucketForFamily(ObsFamily.EDRS_SESSION));
        assertEquals("l0-slices", config.getBucketForFamily(ObsFamily.L0_SLICE));
        assertEquals("acns", config.getBucketForFamily(ObsFamily.L0_ACN));
        assertEquals("l1-slices", config.getBucketForFamily(ObsFamily.L1_SLICE));
        assertEquals("l1-acns", config.getBucketForFamily(ObsFamily.L1_ACN));
    }
    
    /**
     * Test get bucket when family is invalid
     */
    @Test(expected = ObsServiceException.class)
    public void testBucketForNullFamily() throws ObsServiceException {
        S3Configuration config = new S3Configuration();
        config.getBucketForFamily(ObsFamily.UNKNOWN);
    }
    
    /**
     * Test s3 client
     * @throws ObsServiceException
     */
    @Test
    public void testDefaultS3Client() throws ObsServiceException {
        S3Configuration config = new S3Configuration();
        AmazonS3 s3client = config.defaultS3Client();
        
        assertTrue(s3client.doesBucketExistV2("l1-slices"));
    }
    
    /**
     * Test nominal getIntOfConfiguration
     * @throws ObsServiceException 
     */
    @Test
    public void testNominalgetIntOfConfiguration() throws ObsServiceException {
        S3Configuration config = new S3Configuration();
        assertEquals(10, config.getIntOfConfiguration(S3Configuration.TM_S_SHUTDOWN));
        assertEquals(15, config.getIntOfConfiguration(S3Configuration.TM_S_DOWN_EXEC));
        assertEquals(20, config.getIntOfConfiguration(S3Configuration.TM_S_UP_EXEC));
    }
}

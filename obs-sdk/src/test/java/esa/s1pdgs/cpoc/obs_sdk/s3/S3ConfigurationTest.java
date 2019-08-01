package esa.s1pdgs.cpoc.obs_sdk.s3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.amazonaws.services.s3.AmazonS3;

import esa.s1pdgs.cpoc.common.ProductFamily;
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
        
        assertEquals("RU7QIAHIFSM4MHPXJUAM", config.getStringOfConfiguration(S3Configuration.USER_ID));
        assertEquals("Hii85WDNXQyt21YzScOhcMhlt2MRKJ4ReBGeaHqI", config.getStringOfConfiguration(S3Configuration.USER_SECRET));
        assertEquals("http://oss.eu-west-0.prod-cloud-ocb.orange-business.com/", config.getStringOfConfiguration(S3Configuration.ENDPOINT));
        assertEquals("eu-west-0", config.getStringOfConfiguration(S3Configuration.ENDPOINT_REGION));
        assertEquals("auxiliary-files", config.getStringOfConfiguration("bucket.auxiliary-file"));
        assertEquals("session-files", config.getStringOfConfiguration("bucket.edrs-session"));
        assertEquals("l0-slices", config.getStringOfConfiguration("bucket.l0-slice"));
        assertEquals("l0-acns", config.getStringOfConfiguration("bucket.l0-acn"));
        assertEquals("l1-slices", config.getStringOfConfiguration("bucket.l1-slice"));
        assertEquals("l1-acns", config.getStringOfConfiguration("bucket.l1-acn"));

        assertEquals("10", config.getStringOfConfiguration(S3Configuration.TM_S_SHUTDOWN));
        assertEquals("15", config.getStringOfConfiguration(S3Configuration.TM_S_DOWN_EXEC));
        assertEquals("20", config.getStringOfConfiguration(S3Configuration.TM_S_UP_EXEC));
    }
    
    /**
     * Check that bucket returned according the family is right
     * @throws ObsServiceException
     */
    @Test
    public void testBucketForFamily() throws ObsServiceException {
        S3Configuration config = new S3Configuration();
        
        assertEquals("auxiliary-files", config.getBucketForFamily(ProductFamily.AUXILIARY_FILE));
        assertEquals("session-files", config.getBucketForFamily(ProductFamily.EDRS_SESSION));
        assertEquals("l0-slices", config.getBucketForFamily(ProductFamily.L0_SLICE));
        assertEquals("l0-acns", config.getBucketForFamily(ProductFamily.L0_ACN));
        assertEquals("l1-slices", config.getBucketForFamily(ProductFamily.L1_SLICE));
        assertEquals("l1-acns", config.getBucketForFamily(ProductFamily.L1_ACN));
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

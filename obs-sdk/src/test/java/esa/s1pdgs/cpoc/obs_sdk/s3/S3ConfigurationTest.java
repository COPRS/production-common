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
        
        assertEquals("MEF5PCBPTXHKOUTCKW3G", config.getConfiguration()
                .getString(S3Configuration.USER_ID));
        assertEquals("4YiRYBN6HulfbA6uP7eZdhlm9Jb5JVYm18byMGJo", config.getConfiguration()
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
    
    // LS: Test does not run as the AWS key is invalid: 
    /*
     * 
     * com.amazonaws.services.s3.model.AmazonS3Exception: The AWS Access Key Id you provided does not exist in our records. (Service: Amazon S3; Status Code: 403; Error Code: InvalidAccessKeyId; Request ID: 00750000016AB655E06861B3AFB89A89; S3 Extended Request ID: SA8Z3oRbJb2Qjh8eEYdmvogLnWLFGQrLyD5pa/vMlJS3OZlo0OcX2E4XEca93TPK), S3 Extended Request ID: SA8Z3oRbJb2Qjh8eEYdmvogLnWLFGQrLyD5pa/vMlJS3OZlo0OcX2E4XEca93TPK
	at com.amazonaws.http.AmazonHttpClient$RequestExecutor.handleErrorResponse(AmazonHttpClient.java:1639)
	at com.amazonaws.http.AmazonHttpClient$RequestExecutor.executeOneRequest(AmazonHttpClient.java:1304)
	at com.amazonaws.http.AmazonHttpClient$RequestExecutor.executeHelper(AmazonHttpClient.java:1056)
	at com.amazonaws.http.AmazonHttpClient$RequestExecutor.doExecute(AmazonHttpClient.java:743)
	at com.amazonaws.http.AmazonHttpClient$RequestExecutor.executeWithTimer(AmazonHttpClient.java:717)
	at com.amazonaws.http.AmazonHttpClient$RequestExecutor.execute(AmazonHttpClient.java:699)
	at com.amazonaws.http.AmazonHttpClient$RequestExecutor.access$500(AmazonHttpClient.java:667)
	at com.amazonaws.http.AmazonHttpClient$RequestExecutionBuilderImpl.execute(AmazonHttpClient.java:649)
	at com.amazonaws.http.AmazonHttpClient.execute(AmazonHttpClient.java:513)
	at com.amazonaws.services.s3.AmazonS3Client.invoke(AmazonS3Client.java:4325)
	at com.amazonaws.services.s3.AmazonS3Client.invoke(AmazonS3Client.java:4272)
	at com.amazonaws.services.s3.AmazonS3Client.getAcl(AmazonS3Client.java:3477)
	at com.amazonaws.services.s3.AmazonS3Client.getBucketAcl(AmazonS3Client.java:1171)
	at com.amazonaws.services.s3.AmazonS3Client.getBucketAcl(AmazonS3Client.java:1161)
	at com.amazonaws.services.s3.AmazonS3Client.doesBucketExistV2(AmazonS3Client.java:1296)
	at esa.s1pdgs.cpoc.obs_sdk.s3.S3ConfigurationTest.testDefaultS3Client(S3ConfigurationTest.java:94)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:498)
	at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:50)
	at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
	at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:47)
	at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)
	at org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:325)
	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:78)
	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:57)
	at org.junit.runners.ParentRunner$3.run(ParentRunner.java:290)
	at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:71)
	at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:288)
	at org.junit.runners.ParentRunner.access$000(ParentRunner.java:58)
	at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:268)
	at org.junit.runners.ParentRunner.run(ParentRunner.java:363)
	at org.eclipse.jdt.internal.junit4.runner.JUnit4TestReference.run(JUnit4TestReference.java:89)
	at org.eclipse.jdt.internal.junit.runner.TestExecution.run(TestExecution.java:41)
	at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:541)
	at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:763)
	at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.run(RemoteTestRunner.java:463)
	at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.main(RemoteTestRunner.java:209)
	
     * 
     */
    //@Test
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

package esa.s1pdgs.cpoc.obs_sdk.s3;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.amazonaws.services.s3.model.BucketLifecycleConfiguration;
import com.amazonaws.services.s3.model.SetBucketLifecycleConfigurationRequest;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.obs_sdk.FileObsUploadObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsConfigurationProperties;
import esa.s1pdgs.cpoc.obs_sdk.ObsDownloadObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsEmptyFileException;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsObjectMetadata;
import esa.s1pdgs.cpoc.obs_sdk.ObsValidationException;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;
import esa.s1pdgs.cpoc.obs_sdk.StreamObsUploadObject;
import esa.s1pdgs.cpoc.obs_sdk.report.ReportingProductFactory;
import esa.s1pdgs.cpoc.report.ReportingFactory;

@Ignore
@RunWith(SpringRunner.class)
@TestPropertySource("classpath:obs-aws-s3.properties")
@ContextConfiguration(classes = {ObsConfigurationProperties.class})
public class S3ObsClientIT {

    private static final Logger LOG = LogManager.getLogger(S3ObsClientIT.class);

    public final static ProductFamily auxiliaryFiles = ProductFamily.AUXILIARY_FILE;
    public final static String auxiliaryFilesBucketName = "werum-ut-auxiliary-files";
    public final static String testFilePrefix1 = "abc/def/";
    public final static String testFilePrefix5mb = "xyz/";
    public final static String testFileName1 = "testfile1.txt";
    public final static String testFileName2 = "testfile2.txt";
    public final static String testFileName5mb = "random-5mb.bin";
    public final static String testUnexptectedFileName = "unexpected.txt";
    public final static String testDirectoryName = "testdir";
    public final static File testFile1 = getResource("/" + testFileName1);
    public final static File testFile2 = getResource("/" + testFileName2);
    public final static File testFile5mb = getResource("/" + testFileName5mb);
    public final static File testDirectory = getResource("/" + testDirectoryName);

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Autowired
    private ObsConfigurationProperties configuration;

    private S3ObsClient uut;

    public static File getResource(final String fileName) {
        try {
            return new File(S3ObsClientIT.class.getResource(fileName).toURI());
        } catch (final URISyntaxException e) {
            throw new RuntimeException("Could not get resource");
        }
    }

    @Before
    public void setUp() throws SdkClientException {
        LOG.info("running with endpoint " + configuration.getEndpoint());

        uut = (S3ObsClient) new S3ObsClient.Factory().newObsClient(configuration, new ReportingProductFactory());

        // prepare environment
        if (!uut.bucketExists(auxiliaryFiles)) {
            uut.createBucket(auxiliaryFiles);
        }

        if (uut.exists(new ObsObject(auxiliaryFiles, testFileName1))) {
            uut.s3Services.s3client.deleteObject(auxiliaryFilesBucketName, testFileName1);
        }

        if (uut.exists(new ObsObject(auxiliaryFiles, testFileName2))) {
            uut.s3Services.s3client.deleteObject(auxiliaryFilesBucketName, testFileName2);
        }

        if (uut.exists(new ObsObject(auxiliaryFiles, testFilePrefix5mb + testFileName5mb))) {
            uut.s3Services.s3client.deleteObject(auxiliaryFilesBucketName, testFilePrefix5mb + testFileName5mb);
        }

        if (uut.exists(new ObsObject(auxiliaryFiles, testFilePrefix1 + testFileName1))) {
            uut.s3Services.s3client.deleteObject(auxiliaryFilesBucketName, testFilePrefix1 + testFileName1);
        }

        if (uut.exists(new ObsObject(auxiliaryFiles, testFilePrefix1 + testFileName2))) {
            uut.s3Services.s3client.deleteObject(auxiliaryFilesBucketName, testFilePrefix1 + testFileName2);
        }

        if (uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + "/" + testFileName1))) {
            uut.s3Services.s3client.deleteObject(auxiliaryFilesBucketName, testDirectoryName + "/" + testFileName1);
        }

        if (uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + "/" + testFileName2))) {
            uut.s3Services.s3client.deleteObject(auxiliaryFilesBucketName, testDirectoryName + "/" + testFileName2);
        }

        if (uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + "/" + testUnexptectedFileName))) {
            uut.s3Services.s3client.deleteObject(auxiliaryFilesBucketName, testDirectoryName + "/" + testUnexptectedFileName);
        }

        if (uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + ".md5sum"))) {
            uut.s3Services.s3client.deleteObject(auxiliaryFilesBucketName, testDirectoryName + ".md5sum");
        }
    }

    @Test
    public void uploadWithoutPrefixTest() throws Exception {
        // upload
        assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testFileName1)));
        uut.upload(singletonList(new FileObsUploadObject(auxiliaryFiles, testFileName1, testFile1)), ReportingFactory.NULL);
        assertTrue(uut.exists(new ObsObject(auxiliaryFiles, testFileName1)));
    }

    @Test
    public void uploadWithPrefixTest() throws Exception {
        // upload
        assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testFilePrefix1 + testFileName1)));
        uut.upload(singletonList(new FileObsUploadObject(auxiliaryFiles, testFilePrefix1 + testFileName1, testFile1)), ReportingFactory.NULL);
        assertTrue(uut.exists(new ObsObject(auxiliaryFiles, testFilePrefix1 + testFileName1)));
        uut.validate(new FileObsUploadObject(auxiliaryFiles, testFilePrefix1 + testFileName1, testFile1));
    }

    @Test
    public void uploadWithPrefixAsFileInputStreamTest() throws IOException, SdkClientException, AbstractCodedException, ObsEmptyFileException, ObsValidationException, URISyntaxException {
        long contentLength = testFile5mb.length();
        URL res = getClass().getResource("/" + testFileName5mb);
        String absolutePath = Paths.get(res.toURI()).toFile().getAbsolutePath();
        try(InputStream in = new FileInputStream(absolutePath)) {
            // upload
            assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testFilePrefix5mb + testFileName5mb)));
            uut.uploadStreams(singletonList(new StreamObsUploadObject(auxiliaryFiles, testFilePrefix5mb + testFileName5mb, in, contentLength)), ReportingFactory.NULL);
            assertTrue(uut.exists(new ObsObject(auxiliaryFiles, testFilePrefix5mb + testFileName5mb)));
            uut.validate(new StreamObsUploadObject(auxiliaryFiles, testFilePrefix5mb + testFileName5mb, in, contentLength));
        }
    }

    @Test
    public void uploadWithPrefixAsBufferedInputStreamTest() throws IOException, SdkClientException, AbstractCodedException, ObsEmptyFileException, ObsValidationException, URISyntaxException {
        long contentLength = testFile5mb.length();
        URL res = getClass().getResource("/" + testFileName5mb);
        String absolutePath = Paths.get(res.toURI()).toFile().getAbsolutePath();
        try(InputStream in = new BufferedInputStream(new FileInputStream(absolutePath))) {
            // upload
            assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testFilePrefix5mb + testFileName5mb)));
            uut.uploadStreams(singletonList(new StreamObsUploadObject(auxiliaryFiles, testFilePrefix5mb + testFileName5mb, in, contentLength)), ReportingFactory.NULL);
            assertTrue(uut.exists(new ObsObject(auxiliaryFiles, testFilePrefix5mb + testFileName5mb)));
            uut.validate(new StreamObsUploadObject(auxiliaryFiles, testFilePrefix5mb + testFileName5mb, in, contentLength));
        }
    }


    @Test
    public void uploadAndValidationOfCompleteDirectoryTest() throws SdkClientException, AbstractCodedException, ObsValidationException, ObsEmptyFileException {
        // upload directory
        assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + "/" + testFileName1)));
        assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + "/" + testFileName2)));
        assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + "/" + testUnexptectedFileName)));
        assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + ".md5sum")));
        uut.upload(singletonList(new FileObsUploadObject(auxiliaryFiles, testDirectoryName, testDirectory)), ReportingFactory.NULL);
        assertTrue(uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + "/" + testFileName1)));
        assertTrue(uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + "/" + testFileName2)));
        assertTrue(uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + ".md5sum")));

        // validate complete directory
        uut.validate(new ObsObject(auxiliaryFiles, testDirectoryName));
    }

    @Test
    public void uploadAndValidationOfDirectoryWithUnexpectedObejectTest() throws SdkClientException, AbstractCodedException, ObsValidationException, ObsEmptyFileException {
        // upload directory
        assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + "/" + testFileName1)));
        assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + "/" + testFileName2)));
        assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + "/" + testUnexptectedFileName)));
        assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + ".md5sum")));
        uut.upload(singletonList(new FileObsUploadObject(auxiliaryFiles, testDirectoryName, testDirectory)), ReportingFactory.NULL);
        assertTrue(uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + "/" + testFileName1)));
        assertTrue(uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + "/" + testFileName2)));
        assertTrue(uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + ".md5sum")));

        // upload unexpected object
        uut.upload(singletonList(new FileObsUploadObject(auxiliaryFiles, testDirectoryName + "/" + testUnexptectedFileName, testFile1)), ReportingFactory.NULL);
        assertTrue(uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + "/" + testUnexptectedFileName)));

        // validate directory with unexpected object
        exception.expect(ObsValidationException.class);
        uut.validate(new ObsObject(auxiliaryFiles, testDirectoryName));
    }

    @Test
    public void uploadAndValidationOfIncompleteDirectoryTest() throws SdkClientException, AbstractCodedException, ObsValidationException, ObsEmptyFileException {
        // upload
        assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + "/" + testFileName1)));
        assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + "/" + testFileName2)));
        assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + "/" + testUnexptectedFileName)));
        assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + ".md5sum")));
        uut.upload(singletonList(new FileObsUploadObject(auxiliaryFiles, testDirectoryName, testDirectory)), ReportingFactory.NULL);
        assertTrue(uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + "/" + testFileName1)));
        assertTrue(uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + "/" + testFileName2)));
        assertTrue(uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + ".md5sum")));

        // remove object from directory
        uut.s3Services.s3client.deleteObject(auxiliaryFilesBucketName, testDirectoryName + "/" + testFileName1);
        assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + "/" + testFileName1)));

        // validate incomplete directory
        exception.expect(ObsValidationException.class);
        exception.expectMessage("Object not found: " + testDirectoryName + "/" + testFileName1 + " of family " + auxiliaryFiles);
        uut.validate(new ObsObject(auxiliaryFiles, testDirectoryName));
    }

    @Test
    public void uploadAndValidationOfDirectoryWithWrongChecksumTest() throws SdkClientException, AbstractCodedException, ObsValidationException, ObsEmptyFileException {
        // upload
        assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + "/" + testFileName1)));
        assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + "/" + testFileName2)));
        assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + "/" + testUnexptectedFileName)));
        assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + ".md5sum")));
        uut.upload(singletonList(new FileObsUploadObject(auxiliaryFiles, testDirectoryName, testDirectory)), ReportingFactory.NULL);
        assertTrue(uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + "/" + testFileName1)));
        assertTrue(uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + "/" + testFileName2)));
        assertTrue(uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + ".md5sum")));

        // replace object with bad one
        uut.s3Services.s3client.deleteObject(auxiliaryFilesBucketName, testDirectoryName + "/" + testFileName1);
        assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + "/" + testFileName1)));
        uut.s3Services.uploadFile(auxiliaryFilesBucketName, testDirectoryName + "/" + testFileName1, testFile2);
        assertTrue(uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + "/" + testFileName1)));

        // validate wrong checksum situation
        exception.expect(ObsValidationException.class);
        exception.expectMessage("Checksum is wrong for object: " + testDirectoryName + "/" + testFileName1 + " of family " + auxiliaryFiles);
        uut.validate(new ObsObject(auxiliaryFiles, testDirectoryName));
    }

    @Test
    public void uploadAndValidationOfDirectoryWithNonexistentChecksumTest() throws SdkClientException, ObsValidationException {
        // validate not existing checksum file
        assertFalse(uut.exists(new ObsObject(auxiliaryFiles, "not-existing.md5sum")));
        exception.expect(ObsValidationException.class);
        exception.expectMessage("Checksum file not found for: not-existing of family " + auxiliaryFiles);
        uut.validate(new ObsObject(auxiliaryFiles, "not-existing"));
    }

    @Test
    public void deleteWithoutPrefixTest() throws Exception {
        // upload
        assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testFileName1)));
        uut.upload(singletonList(new FileObsUploadObject(auxiliaryFiles, testFileName1, testFile1)), ReportingFactory.NULL);
        assertTrue(uut.exists(new ObsObject(auxiliaryFiles, testFileName1)));

        // delete
        uut.s3Services.s3client.deleteObject(auxiliaryFilesBucketName, testFileName1);
        assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testFileName1)));
    }

    @Test
    public void deleteWithPrefixTest() throws Exception {
        // upload
        assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testFilePrefix1 + testFileName1)));
        uut.upload(singletonList(new FileObsUploadObject(auxiliaryFiles, testFilePrefix1 + testFileName1, testFile1)), ReportingFactory.NULL);
        assertTrue(uut.exists(new ObsObject(auxiliaryFiles, testFilePrefix1 + testFileName1)));

        // delete
        uut.s3Services.s3client.deleteObject(auxiliaryFilesBucketName, testFilePrefix1 + testFileName1);
        assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testFilePrefix1 + testFileName1)));
    }

    @Test
    public void downloadFileWithoutPrefixTest() throws Exception {
        // upload
        assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testFileName1)));
        uut.upload(singletonList(new FileObsUploadObject(auxiliaryFiles, testFileName1, testFile1)), ReportingFactory.NULL);
        assertTrue(uut.exists(new ObsObject(auxiliaryFiles, testFileName1)));

        // single file download
        final String targetDir = Files.createTempDirectory(this.getClass().getCanonicalName() + "-").toString();
        uut.download(singletonList(new ObsDownloadObject(auxiliaryFiles, testFileName1, targetDir)), ReportingFactory.NULL);
        final String send1 = new String(Files.readAllBytes(testFile1.toPath()));
        final String received1 = new String(Files.readAllBytes((new File(targetDir + "/" + testFileName1)).toPath()));
        assertEquals(send1, received1);
    }

    @Test
    public void downloadFileWithPrefixTest() throws Exception {
        // upload
        assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testFilePrefix1 + testFileName1)));
        uut.upload(singletonList(new FileObsUploadObject(auxiliaryFiles, testFilePrefix1 + testFileName1, testFile1)), ReportingFactory.NULL);
        assertTrue(uut.exists(new ObsObject(auxiliaryFiles, testFilePrefix1 + testFileName1)));

        // single file download
        final String targetDir = Files.createTempDirectory(this.getClass().getCanonicalName() + "-").toString();
        uut.download(singletonList(new ObsDownloadObject(auxiliaryFiles, testFilePrefix1 + testFileName1, targetDir)), ReportingFactory.NULL);
        final String send1 = new String(Files.readAllBytes(testFile1.toPath()));
        final String received1 = new String(Files.readAllBytes((new File(targetDir + "/" + testFilePrefix1 + testFileName1)).toPath()));
        assertEquals(send1, received1);
    }

    @Test
    public void downloadOfDirectoryTest() throws IOException, SdkClientException, AbstractCodedException, ObsEmptyFileException {
        // upload
        assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + "/" + testFileName1)));
        assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + "/" + testFileName2)));
        assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + ".md5sum")));
        uut.upload(singletonList(new FileObsUploadObject(auxiliaryFiles, testDirectoryName, testDirectory)), ReportingFactory.NULL);
        assertTrue(uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + "/" + testFileName1)));
        assertTrue(uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + "/" + testFileName2)));
        assertTrue(uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + ".md5sum")));

        // multi file download
        final String targetDir = Files.createTempDirectory(this.getClass().getCanonicalName() + "-").toString();
        uut.download(singletonList(new ObsDownloadObject(auxiliaryFiles, testDirectoryName + "/", targetDir)), ReportingFactory.NULL);

        final String send1 = new String(Files.readAllBytes(new File(testDirectory, testFileName1).toPath()));
        final String received1 = new String(Files.readAllBytes((new File(targetDir + "/" + testDirectoryName + "/" + testFileName1)).toPath()));
        assertEquals(send1, received1);

        final String send2 = new String(Files.readAllBytes(new File(testDirectory, testFileName2).toPath()));
        final String received2 = new String(Files.readAllBytes((new File(targetDir + "/" + testDirectoryName + "/" + testFileName2)).toPath()));
        assertEquals(send2, received2);

        assertFalse(new File(targetDir + "/" + testDirectoryName + ".md5sum").exists());
    }

    @Test
    public void numberOfObjectsWithoutPrefixTest() throws Exception {
        // upload
        assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testFileName1)));
        assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testFileName2)));
        uut.upload(singletonList(new FileObsUploadObject(auxiliaryFiles, testFileName1, testFile1)), ReportingFactory.NULL);
        uut.upload(singletonList(new FileObsUploadObject(auxiliaryFiles, testFileName2, testFile2)), ReportingFactory.NULL);
        assertTrue(uut.exists(new ObsObject(auxiliaryFiles, testFileName1)));
        assertTrue(uut.exists(new ObsObject(auxiliaryFiles, testFileName2)));

        // count
        final int count = uut.s3Services.getNbObjects(auxiliaryFilesBucketName, "");
        assertEquals(2, count);
    }

    @Test
    public void numberOfObjectsWithPrefixTest() throws Exception {
        // upload
        assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testFilePrefix1 + testFileName1)));
        assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testFilePrefix1 + testFileName2)));
        uut.upload(singletonList(new FileObsUploadObject(auxiliaryFiles, testFilePrefix1 + testFileName1, testFile1)), ReportingFactory.NULL);
        uut.upload(singletonList(new FileObsUploadObject(auxiliaryFiles, testFilePrefix1 + testFileName2, testFile2)), ReportingFactory.NULL);
        assertTrue(uut.exists(new ObsObject(auxiliaryFiles, testFilePrefix1 + testFileName1)));
        assertTrue(uut.exists(new ObsObject(auxiliaryFiles, testFilePrefix1 + testFileName2)));

        // count
        final int count = uut.s3Services.getNbObjects(auxiliaryFilesBucketName, testFilePrefix1);
        assertEquals(2, count);
    }

    @Test
    public final void list() throws Exception {
        assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testFilePrefix1 + testFileName1)));
        assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testFilePrefix1 + testFileName2)));
        uut.upload(singletonList(new FileObsUploadObject(auxiliaryFiles, testFilePrefix1 + testFileName1, testFile1)), ReportingFactory.NULL);
        uut.upload(singletonList(new FileObsUploadObject(auxiliaryFiles, testFilePrefix1 + testFileName2, testFile2)), ReportingFactory.NULL);
        assertTrue(uut.exists(new ObsObject(auxiliaryFiles, testFilePrefix1 + testFileName1)));
        assertTrue(uut.exists(new ObsObject(auxiliaryFiles, testFilePrefix1 + testFileName2)));

        final List<String> res = uut.list(auxiliaryFiles, testFilePrefix1);
        assertEquals(Arrays.asList("abc/def/testfile1.txt", "abc/def/testfile2.txt"), res);
    }

    @Test
    public final void getAsStream() throws Exception {
        assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testFilePrefix1 + testFileName1)));
        assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testFilePrefix1 + testFileName2)));
        uut.upload(singletonList(new FileObsUploadObject(auxiliaryFiles, testFilePrefix1 + testFileName1, testFile1)), ReportingFactory.NULL);
        uut.upload(singletonList(new FileObsUploadObject(auxiliaryFiles, testFilePrefix1 + testFileName2, testFile2)), ReportingFactory.NULL);
        assertTrue(uut.exists(new ObsObject(auxiliaryFiles, testFilePrefix1 + testFileName1)));
        assertTrue(uut.exists(new ObsObject(auxiliaryFiles, testFilePrefix1 + testFileName2)));

        final String retrievedTestFile1Content = IOUtils.toString(uut.getAsStream(auxiliaryFiles, "abc/def/testfile1.txt"), StandardCharsets.UTF_8);
        final String retrievedTestFile2Content = IOUtils.toString(uut.getAsStream(auxiliaryFiles, "abc/def/testfile2.txt"), StandardCharsets.UTF_8);

        assertEquals("test", retrievedTestFile1Content);
        assertEquals("test2", retrievedTestFile2Content);
    }

    @Test
    public void testSetExpirationDate() throws SdkClientException {
        removeAllLifecycleRules();

        uut.setExpirationTime(new ObsObject(ProductFamily.AUXILIARY_FILE, "AUX-FILE.DAT"), Instant.now());

        final BucketLifecycleConfiguration lifecycleConfiguration = uut.s3Services.s3client.getBucketLifecycleConfiguration(auxiliaryFilesBucketName);

        //two rules because Dummy rule is still present
        final List<BucketLifecycleConfiguration.Rule> rules = lifecycleConfiguration.getRules().stream().filter(r -> !r.getId().equals("Dummy")).collect(Collectors.toList());
        assertEquals(1, rules.size());
        assertEquals("AUX-FILE.DAT", rules.get(0).getId());
        assertEquals("AUX-FILE.DAT", rules.get(0).getPrefix());
        assertEquals("Enabled", rules.get(0).getStatus());
        assertEquals(Instant.now().truncatedTo(ChronoUnit.DAYS), rules.get(0).getExpirationDate().toInstant());
    }

    private void removeAllLifecycleRules() {
        //store one rule only because it is not possible to delete config with s3Client
        uut.s3Services.s3client.setBucketLifecycleConfiguration(
                new SetBucketLifecycleConfigurationRequest(auxiliaryFilesBucketName, new BucketLifecycleConfiguration().withRules(
                        new BucketLifecycleConfiguration.Rule()
                                .withStatus(BucketLifecycleConfiguration.DISABLED)
                                .withPrefix("Dummy")
                                .withId("Dummy").withExpirationInDays(365))));

        final BucketLifecycleConfiguration config =
                uut.s3Services.s3client.getBucketLifecycleConfiguration(auxiliaryFilesBucketName);

        assertEquals(1, config.getRules().size());
        assertEquals("Dummy", config.getRules().get(0).getId());
        assertEquals("Disabled", config.getRules().get(0).getStatus());
    }

    @Test
    public void testGetMetadata() throws SdkClientException, AbstractCodedException, ObsEmptyFileException {
        Instant justBeforeCreation = Instant.now().minus(Duration.ofMinutes(1));

        final String obsKey = testFilePrefix1 + testFileName1;
        assertFalse(uut.exists(new ObsObject(auxiliaryFiles, obsKey)));
        uut.upload(singletonList(new FileObsUploadObject(auxiliaryFiles, obsKey, testFile1)), ReportingFactory.NULL);
        assertTrue(uut.exists(new ObsObject(auxiliaryFiles, obsKey)));

        Instant justAfterCreation = Instant.now().plus(Duration.ofSeconds(1));

        final ObsObjectMetadata metadata = uut.getMetadata(new ObsObject(ProductFamily.AUXILIARY_FILE, obsKey));

        assertEquals(obsKey, metadata.getKey());
        assertThat(metadata.getLastModified(), is(greaterThan(justBeforeCreation)));
        assertThat(metadata.getLastModified(), is(lessThan(justAfterCreation)));
    }

    @Test
    public void testGetChecksum() throws SdkClientException, AbstractCodedException, ObsEmptyFileException {
        // upload
        assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testFileName1)));
        uut.upload(singletonList(new FileObsUploadObject(auxiliaryFiles, testFileName1, testFile1)), ReportingFactory.NULL);
        assertTrue(uut.exists(new ObsObject(auxiliaryFiles, testFileName1)));

        final String md5 = uut.getChecksum(new ObsObject(auxiliaryFiles, testFileName1));
        assertThat(md5, is(equalTo("98f6bcd4621d373cade4e832627b4f6")));
    }
}

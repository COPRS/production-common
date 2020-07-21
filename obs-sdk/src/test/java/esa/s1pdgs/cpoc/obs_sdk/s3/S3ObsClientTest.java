package esa.s1pdgs.cpoc.obs_sdk.s3;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.obs.ObsException;
import esa.s1pdgs.cpoc.obs_sdk.FileObsUploadObject;
import esa.s1pdgs.cpoc.obs_sdk.Md5;
import esa.s1pdgs.cpoc.obs_sdk.ObsConfigurationProperties;
import esa.s1pdgs.cpoc.obs_sdk.ObsDownloadObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsEmptyFileException;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsObjectMetadata;
import esa.s1pdgs.cpoc.obs_sdk.ObsServiceException;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;
import esa.s1pdgs.cpoc.obs_sdk.StreamObsUploadObject;
import esa.s1pdgs.cpoc.obs_sdk.report.ReportingProductFactory;
import esa.s1pdgs.cpoc.report.ReportingFactory;

/**
 * Test the client Amazon S3
 *
 * @author Viveris Technologies
 */
public class S3ObsClientTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Mock configuration
     */
    @Mock
    private ObsConfigurationProperties configuration;

    /**
     * Mock service
     */
    @Mock
    private S3ObsServices service;

    /**
     *
     */
    @Mock
    private ObjectListing objListing1;

    /**
     *
     */
    @Mock
    private ObjectListing objListing2;

    /**
     * Client to test
     */
    private S3ObsClient client;

    private final Instant expectedLastModified = Instant.now();

    /**
     * Initialization
     *
     * @throws ObsServiceException
     * @throws S3SdkClientException
     */
    @Before
    public void init() throws ObsServiceException, S3SdkClientException {
        // Init mocks
        MockitoAnnotations.initMocks(this);

        // Mock service
        doReturn(true).when(service).exist(Mockito.anyString(),
                eq("key-exist"));
        doReturn(false).when(service).exist(Mockito.anyString(),
                eq("key-not-exist"));
        doReturn(2).when(service).getNbObjects(Mockito.anyString(),
                eq("key-exist"));
        doReturn(0).when(service).getNbObjects(Mockito.anyString(),
                eq("key-not-exist"));
        doReturn(new Md5.Entry("dummy", "dummy", "dummy"))
                .when(service).uploadFile(Mockito.anyString(),
                Mockito.anyString(), any());
        doReturn(new Md5.Entry("dummy", "dummy", "dummy")).when(service).uploadStream(Mockito.anyString(),
                Mockito.anyString(), any(InputStream.class), anyLong());

        final ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setLastModified(new Date(expectedLastModified.toEpochMilli()));
        doReturn(objectMetadata).when(service).getObjectMetadata("auxiliary-files", "test-key");

        // Mock configuration
        doReturn("auxiliary-files").when(configuration)
                .getBucketFor(eq(ProductFamily.AUXILIARY_FILE));
        doReturn("edrs-sessions").when(configuration)
                .getBucketFor(eq(ProductFamily.EDRS_SESSION));
        doReturn("l0-slices").when(configuration)
                .getBucketFor(eq(ProductFamily.L0_SLICE));
        doReturn("l0-acns").when(configuration)
                .getBucketFor(eq(ProductFamily.L0_ACN));
        doReturn("l1-slices").when(configuration)
                .getBucketFor(eq(ProductFamily.L1_SLICE));
        doReturn("l1-acns").when(configuration)
                .getBucketFor(eq(ProductFamily.L1_ACN));
        doReturn("l0-segments").when(configuration)
                .getBucketFor(eq(ProductFamily.L0_SEGMENT));
        doReturn("l0-blanks").when(configuration)
                .getBucketFor(eq(ProductFamily.L0_BLANK));
        doReturn(100).when(configuration).getMaxInputStreamBufferSize();
        doReturn(true).when(configuration).getDisableChunkedEncoding();

        // Build client
        client = new S3ObsClient(configuration, service, new ReportingProductFactory());
    }

    /**
     * Test exist
     *
     * @throws ObsServiceException
     * @throws SdkClientException
     */
    @Test
    public void testExist() throws ObsServiceException, SdkClientException {
        boolean ret = client.exists(new ObsObject(ProductFamily.L0_ACN, "key-exist"));
        assertTrue(ret);
        verify(service, times(1)).exist(eq("l0-acns"), eq("key-exist"));

        ret = client.exists( new ObsObject(ProductFamily.L1_SLICE, "key-not-exist"));
        assertFalse(ret);
        verify(service, times(1)).exist(eq("l1-slices"), eq("key-not-exist"));
    }

    /**
     * Test prefixExist
     *
     * @throws ObsServiceException
     * @throws SdkClientException
     */
    @Test
    public void testPrefixExist() throws ObsServiceException, SdkClientException {
        boolean ret = client.prefixExists(new ObsObject(ProductFamily.L0_SLICE, "key-exist"));
        assertTrue(ret);
        verify(service, times(1)).getNbObjects(eq("l0-slices"),
                eq("key-exist"));

        ret = client.prefixExists(new ObsObject(ProductFamily.L1_SLICE, "key-not-exist"));
        assertFalse(ret);
        verify(service, times(1)).getNbObjects(eq("l1-slices"), eq("key-not-exist"));
    }

    /**
     * Test downloadObject
     * @throws ObsServiceException
     * @throws SdkClientException
     */
    @Test
    public void testDownloadObject() throws ObsServiceException, SdkClientException {
        client.downloadObject(new ObsDownloadObject(ProductFamily.AUXILIARY_FILE, "key-exist", "target-dir"));
        verify(service, times(1)).downloadObjectsWithPrefix(eq("auxiliary-files"),
                eq("key-exist"), eq("target-dir"), eq(false));

        client.downloadObject(new ObsDownloadObject(ProductFamily.EDRS_SESSION, "key-not-exist", "target-dir"));
        verify(service, times(1)).downloadObjectsWithPrefix(eq("edrs-sessions"),
                eq("key-not-exist"), eq("target-dir"), eq(true));
    }

    /**
     * Test uploadObject when directory
     * @throws ObsServiceException
     * @throws SdkClientException
     * @throws ObsException
     */
    @Test
    public void testUploadObjectDirectory() throws ObsServiceException, SdkClientException, ObsException {
        client.uploadObject(new FileObsUploadObject(ProductFamily.L0_ACN, "key-exist", new File("target")));

        verify(service, times(1)).uploadDirectory(eq("l0-acns"),
                eq("key-exist"), eq(new File("target")));
        verify(service, times(1)).uploadFile(Mockito.anyString(),
                Mockito.anyString(), any()); // for the 1st md5sum

        client.uploadObject(new FileObsUploadObject(ProductFamily.L0_ACN, "key-not-exist", new File("target")));
        verify(service, times(1)).uploadDirectory(eq("l0-acns"),
                eq("key-not-exist"), eq(new File("target")));
        verify(service, times(2)).uploadFile(Mockito.anyString(),
                Mockito.anyString(), any()); // for the 2nd md5sum
    }

    /**
     * Test uploadObject when file
     * @throws ObsServiceException
     * @throws SdkClientException
     * @throws ObsException
     */
    @Test
    public void testUploadObjectFile() throws ObsServiceException, SdkClientException, ObsException {
        client.uploadObject(new FileObsUploadObject(ProductFamily.L0_ACN, "key-exist", new File("pom.xml")));
        verify(service, times(1))
                .uploadFile(eq("l0-acns"), eq("key-exist"), eq(new File("pom.xml")));
        verify(service, never()).uploadDirectory(Mockito.anyString(),
                Mockito.anyString(), any());
    }

    @Test
    public void testUploadStream() throws IOException, ObsServiceException, S3SdkClientException {
        try(InputStream in = getClass().getResourceAsStream("/testfile1.txt")) {
            client.uploadObject(new StreamObsUploadObject(ProductFamily.L0_ACN, "key-exist", in, 100));
        }

        verify(service, times(1))
                .uploadStream(eq("l0-acns"), eq("key-exist"), any(InputStream.class), anyLong());
    }

    @Test
    public void testUploadStreamFileTooBigToBuffer() throws IOException, ObsServiceException, S3SdkClientException {
        thrown.expect(S3ObsServiceException.class);
        thrown.expectMessage("Actual content length 110 is greater than max allowed input stream buffer size 100");

        try(InputStream in = getClass().getResourceAsStream("/testfile2.txt")) {
            client.uploadObject(new StreamObsUploadObject(ProductFamily.L0_ACN, "key-exist", in, 110));
        }
    }

    @Test
    public void testUploadStreamFileNoBufferForFileInputStream() throws IOException, ObsServiceException, S3SdkClientException {
        try(FileInputStream in = new FileInputStream(getClass().getResource("/testfile2.txt").getFile())) {
            client.uploadObject(new StreamObsUploadObject(ProductFamily.L0_ACN, "key-exist", in, 110));
        }

        verify(service, times(1))
                .uploadStream(eq("l0-acns"), eq("key-exist"), any(InputStream.class), anyLong());
    }

    @Test
    public void testUploadStreamNoContent() throws IOException, ObsServiceException, S3SdkClientException, AbstractCodedException, ObsEmptyFileException {
        thrown.expect(ObsEmptyFileException.class);
        thrown.expectMessage("key-exist");

        try (InputStream in = getClass().getResourceAsStream("/testfile1.txt")) {
            client.uploadStreams(Collections.singletonList(new StreamObsUploadObject(ProductFamily.L0_ACN, "key-exist", in, 0)), ReportingFactory.NULL);
        }

        verify(service, times(0))
                .uploadStream(any(), any(), any(InputStream.class), anyLong());
    }

    @Test
    public void testSetExpirationTime() throws ObsServiceException {
        final Instant expirationTime = Instant.now();
        client.setExpirationTime(new ObsObject(ProductFamily.AUXILIARY_FILE, "test-key"), expirationTime);
        verify(service, times(1)).setExpirationTime(
                "auxiliary-files",
                "test-key",
                expirationTime);
    }

    @Test
    public void testGetMetadata() throws ObsServiceException {
        final ObsObjectMetadata metadata = client.getMetadata(new ObsObject(ProductFamily.AUXILIARY_FILE, "test-key"));
        verify(service, times(1)).getObjectMetadata("auxiliary-files", "test-key");

        assertEquals(expectedLastModified, metadata.getLastModified());
        assertEquals("test-key", metadata.getKey());
    }

    @Test
    public void testGetListOfObjectsOfTimeFrameOfFamilyOneExists() throws ObsServiceException, SdkClientException {

        final Date timeFrameBegin = Date.from(Instant.parse("2020-01-01T00:00:00Z"));
        final Date timeFrameEnd = Date.from(Instant.parse("2020-01-03T00:00:00Z"));
        final Date obj1Date = Date.from(Instant.parse("2020-01-02T00:00:00Z"));

        final S3ObjectSummary obj1 = new S3ObjectSummary();
        obj1.setKey("obj1");
        obj1.setLastModified(obj1Date);

        final List<S3ObjectSummary> objSums = new ArrayList<>();
        objSums.add(obj1);

        doReturn(objSums).when(objListing1).getObjectSummaries();
        doReturn(false).when(objListing1).isTruncated();
        doReturn(objListing1).when(service).listObjectsFromBucket("l0-slices");

        final List<ObsObject> returnedObjs = client.getObsObjectsOfFamilyWithinTimeFrame(ProductFamily.L0_SLICE, timeFrameBegin, timeFrameEnd);

        assertEquals(1, returnedObjs.size());
        assertEquals("obj1", returnedObjs.get(0).getKey());
        verify(service, times(1)).listObjectsFromBucket("l0-slices");
        verify(service, never()).listNextBatchOfObjectsFromBucket(Mockito.anyString(), any());
    }

    @Test
    public void testGetListOfObjectsOfTimeFrameOfFamilyNoneExists() throws ObsServiceException, SdkClientException {

        final Date timeFrameBegin = Date.from(Instant.parse("2020-01-01T00:00:00Z"));
        final Date timeFrameEnd = Date.from(Instant.parse("2020-01-03T00:00:00Z"));
        final Date obj1Date = Date.from(Instant.parse("2020-01-04T00:00:00Z"));

        final S3ObjectSummary obj1 = new S3ObjectSummary();
        obj1.setKey("obj1");
        obj1.setLastModified(obj1Date);

        final List<S3ObjectSummary> objSums = new ArrayList<>();
        objSums.add(obj1);

        doReturn(objSums).when(objListing1).getObjectSummaries();
        doReturn(false).when(objListing1).isTruncated();
        doReturn(objListing1).when(service).listObjectsFromBucket("l0-slices");

        final List<ObsObject> returnedObjs = client.getObsObjectsOfFamilyWithinTimeFrame(ProductFamily.L0_SLICE, timeFrameBegin, timeFrameEnd);

        assertEquals(0, returnedObjs.size());
        verify(service, times(1)).listObjectsFromBucket("l0-slices");
        verify(service, never()).listNextBatchOfObjectsFromBucket(Mockito.anyString(), any());
    }

    @Test
    public void testGetListOfObjectsOfTimeFrameOfFamilyWithTruncatedList()
            throws ObsServiceException, SdkClientException {

        final Date timeFrameBegin = Date.from(Instant.parse("2020-01-01T00:00:00Z"));
        final Date timeFrameEnd = Date.from(Instant.parse("2020-01-03T00:00:00Z"));

        final Date obj1Date = Date.from(Instant.parse("2020-01-02T00:00:00Z"));
        final Date obj2Date = Date.from(Instant.parse("2020-01-04T00:00:00Z"));
        final Date obj3Date = Date.from(Instant.parse("2020-01-02T03:00:00Z"));

        final S3ObjectSummary obj1 = new S3ObjectSummary();
        obj1.setKey("obj1");
        obj1.setLastModified(obj1Date);

        final S3ObjectSummary obj2 = new S3ObjectSummary();
        obj2.setKey("obj2");
        obj2.setLastModified(obj2Date);

        final S3ObjectSummary obj3 = new S3ObjectSummary();
        obj3.setKey("obj3");
        obj3.setLastModified(obj3Date);

        final List<S3ObjectSummary> objSums1 = new ArrayList<>();
        objSums1.add(obj1);
        objSums1.add(obj2);

        final List<S3ObjectSummary> objSums2 = new ArrayList<>();
        objSums2.add(obj3);

        doReturn(objSums1).when(objListing1).getObjectSummaries();
        doReturn(true).when(objListing1).isTruncated();
        doReturn(objListing1).when(service).listObjectsFromBucket("l0-slices");

        doReturn(objSums2).when(objListing2).getObjectSummaries();
        doReturn(false).when(objListing2).isTruncated();
        doReturn(objListing2).when(service).listNextBatchOfObjectsFromBucket("l0-slices", objListing1);

        final List<ObsObject> returnedObjs = client.getObsObjectsOfFamilyWithinTimeFrame(ProductFamily.L0_SLICE, timeFrameBegin, timeFrameEnd);

        assertEquals(2, returnedObjs.size());
        assertEquals("obj1", returnedObjs.get(0).getKey());
        assertEquals("obj3", returnedObjs.get(1).getKey());
        verify(service, times(1)).listObjectsFromBucket(Mockito.anyString());
        verify(service, times(1)).listNextBatchOfObjectsFromBucket(Mockito.anyString(), any());
    }

    @Test
    public void testExistsValidArgumentAssertion() throws AbstractCodedException {
        assertThatThrownBy(() -> client.exists(null)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid object: null");
        assertThatThrownBy(() -> client.exists(new ObsObject(null, "key"))).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid product family: null");
        assertThatThrownBy(() -> client.exists(new ObsObject(ProductFamily.AUXILIARY_FILE, null))).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid key: null");
        assertThatThrownBy(() -> client.exists(new ObsObject(ProductFamily.AUXILIARY_FILE, ""))).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid key (empty)");
    }

    @Test
    public void testPrefixExistsValidArgumentAssertion() throws AbstractCodedException {
        assertThatThrownBy(() -> client.prefixExists(null)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid object: null");
        assertThatThrownBy(() -> client.prefixExists(new ObsObject(null, "key"))).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid product family: null");
        assertThatThrownBy(() -> client.prefixExists(new ObsObject(ProductFamily.AUXILIARY_FILE, null))).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid key: null");
        assertThatThrownBy(() -> client.prefixExists(new ObsObject(ProductFamily.AUXILIARY_FILE, ""))).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid key (empty)");
    }

    @Test
    public void testGetObsObjectsOfFamilyWithinTimeFrameValidArgumentAssertion() throws AbstractCodedException {
        assertThatThrownBy(() -> client.getObsObjectsOfFamilyWithinTimeFrame(null, new Date(), new Date())).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid product family: null");
        assertThatThrownBy(() -> client.getObsObjectsOfFamilyWithinTimeFrame(ProductFamily.AUXILIARY_FILE, null, new Date())).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid date: null");
        assertThatThrownBy(() -> client.getObsObjectsOfFamilyWithinTimeFrame(ProductFamily.AUXILIARY_FILE, new Date(), null)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid date: null");
    }

    @Test
    public void testMoveValidArgumentAssertion() throws AbstractCodedException {
        assertThatThrownBy(() -> client.move(null, ProductFamily.AUXILIARY_FILE)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid object: null");
        assertThatThrownBy(() -> client.move(new ObsObject(null, "key"), ProductFamily.AUXILIARY_FILE)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid product family: null");
        assertThatThrownBy(() -> client.move(new ObsObject(ProductFamily.AUXILIARY_FILE, null), ProductFamily.AUXILIARY_FILE)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid key: null");
        assertThatThrownBy(() -> client.move(new ObsObject(ProductFamily.AUXILIARY_FILE, ""), ProductFamily.AUXILIARY_FILE)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid key (empty)");
        assertThatThrownBy(() -> client.move(new ObsObject(ProductFamily.AUXILIARY_FILE, "key"), null)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid product family: null");
    }

    @Test
    public void testGetAllAsInputStreamValidArgumentAssertion() throws AbstractCodedException {
        assertThatThrownBy(() -> client.getAllAsInputStream(null, "prefix")).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid product family: null");
        assertThatThrownBy(() -> client.getAllAsInputStream(ProductFamily.AUXILIARY_FILE, null)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid prefix: null");
        assertThatThrownBy(() -> client.getAllAsInputStream(ProductFamily.AUXILIARY_FILE, "")).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid prefix (empty)");
    }

}

package fr.viveris.s1pdgs.level0.wrapper.services.s3;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import fr.viveris.s1pdgs.level0.wrapper.model.ProductFamily;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.AbstractCodedException;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.ObjectStorageException;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.ObsParallelAccessException;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.ObsUnknownObjectException;
import fr.viveris.s1pdgs.level0.wrapper.model.s3.S3DownloadFile;
import fr.viveris.s1pdgs.level0.wrapper.model.s3.S3UploadFile;
import fr.viveris.s1pdgs.libs.obs_sdk.ObsClient;
import fr.viveris.s1pdgs.libs.obs_sdk.ObsDownloadObject;
import fr.viveris.s1pdgs.libs.obs_sdk.ObsFamily;
import fr.viveris.s1pdgs.libs.obs_sdk.ObsObject;
import fr.viveris.s1pdgs.libs.obs_sdk.ObsServiceException;
import fr.viveris.s1pdgs.libs.obs_sdk.ObsUploadObject;
import fr.viveris.s1pdgs.libs.obs_sdk.SdkClientException;

/**
 * Test the ObsService
 * 
 * @author Viveris Technologies
 */
public class ObsServiceTest {

    /**
     * Mock OBS client
     */
    @Mock
    private ObsClient client;

    /**
     * Service to test
     */
    private ObsService service;

    /**
     * To check the raised custom exceptions
     */
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Initialization
     * 
     * @throws SdkClientException
     * @throws ObsServiceException
     */
    @Before
    public void init() throws ObsServiceException, SdkClientException {
        MockitoAnnotations.initMocks(this);

        doThrow(new ObsServiceException("error 1 message")).when(client)
                .doesObjectExist(Mockito.eq(
                        new ObsObject("error-key", ObsFamily.AUXILIARY_FILE)));
        doThrow(new SdkClientException("error 2 message")).when(client)
                .doesObjectExist(Mockito.eq(
                        new ObsObject("error-key", ObsFamily.EDRS_SESSION)));
        doReturn(true).when(client).doesObjectExist(Mockito
                .eq(new ObsObject("test-key", ObsFamily.AUXILIARY_FILE)));
        doReturn(false).when(client).doesObjectExist(
                Mockito.eq(new ObsObject("test-key", ObsFamily.EDRS_SESSION)));

        doThrow(new ObsServiceException("error 1 message")).when(client)
                .uploadObject(Mockito.eq(new ObsUploadObject("error-key",
                        ObsFamily.AUXILIARY_FILE, new File("pom.xml"))));
        doThrow(new SdkClientException("error 2 message")).when(client)
                .uploadObject(Mockito.eq(new ObsUploadObject("error-key",
                        ObsFamily.EDRS_SESSION, new File("pom.xml"))));
        doReturn(2).when(client)
                .uploadObject(Mockito.eq(new ObsUploadObject("test-key",
                        ObsFamily.AUXILIARY_FILE, new File("pom.xml"))));
        doReturn(1).when(client)
                .uploadObject(Mockito.eq(new ObsUploadObject("test-key",
                        ObsFamily.EDRS_SESSION, new File("pom.xml"))));

        doThrow(new ObsServiceException("error 1 message")).when(client)
                .downloadObject(Mockito.eq(new ObsDownloadObject("error-key",
                        ObsFamily.AUXILIARY_FILE, "test/")));
        doThrow(new SdkClientException("error 2 message")).when(client)
                .downloadObject(Mockito.eq(new ObsDownloadObject("error-key",
                        ObsFamily.EDRS_SESSION, "test/")));
        doReturn(0).when(client)
                .downloadObject(Mockito.eq(new ObsDownloadObject("test-key",
                        ObsFamily.AUXILIARY_FILE, "test/")));
        doReturn(1).when(client)
                .downloadObject(Mockito.eq(new ObsDownloadObject("test-key",
                        ObsFamily.EDRS_SESSION, "test/")));
        doReturn(2).when(client).downloadObject(
                Mockito.eq(new ObsDownloadObject("test-key/key2",
                        ObsFamily.EDRS_SESSION, "test/")));

        service = new ObsService(client);
    }

    /**
     * Test getObsFamily
     */
    @Test
    public void testGetObsFamily() {
        assertEquals(ObsFamily.AUXILIARY_FILE,
                service.getObsFamily(ProductFamily.CONFIG));
        assertEquals(ObsFamily.EDRS_SESSION,
                service.getObsFamily(ProductFamily.RAW));
        assertEquals(ObsFamily.UNKNOWN,
                service.getObsFamily(ProductFamily.BLANK));
        assertEquals(ObsFamily.L0_ACN,
                service.getObsFamily(ProductFamily.L0_ACN));
        assertEquals(ObsFamily.L0_PRODUCT,
                service.getObsFamily(ProductFamily.L0_PRODUCT));
        assertEquals(ObsFamily.UNKNOWN,
                service.getObsFamily(ProductFamily.L0_REPORT));
        assertEquals(ObsFamily.L1_ACN,
                service.getObsFamily(ProductFamily.L1_ACN));
        assertEquals(ObsFamily.L1_PRODUCT,
                service.getObsFamily(ProductFamily.L1_PRODUCT));
        assertEquals(ObsFamily.UNKNOWN,
                service.getObsFamily(ProductFamily.L1_REPORT));
        assertEquals(ObsFamily.UNKNOWN,
                service.getObsFamily(ProductFamily.JOB));
    }

    /**
     * Test exist when client raise ObsServiceException
     * 
     * @throws ObjectStorageException
     */
    @Test
    public void testExistWhenException() throws ObjectStorageException {
        thrown.expect(ObjectStorageException.class);
        thrown.expect(hasProperty("key", is("error-key")));
        thrown.expect(hasProperty("family", is(ProductFamily.CONFIG)));
        thrown.expectMessage("error 1 message");
        thrown.expectCause(isA(ObsServiceException.class));

        service.exist(ProductFamily.CONFIG, "error-key");
    }

    /**
     * Test exist when client raise ObsServiceException
     * 
     * @throws ObjectStorageException
     */
    @Test
    public void testExistWhenException2() throws ObjectStorageException {
        thrown.expect(ObjectStorageException.class);
        thrown.expect(hasProperty("key", is("error-key")));
        thrown.expect(hasProperty("family", is(ProductFamily.RAW)));
        thrown.expectMessage("error 2 message");
        thrown.expectCause(isA(SdkClientException.class));

        service.exist(ProductFamily.RAW, "error-key");
    }

    /**
     * Test nominal case of exists
     * 
     * @throws ObjectStorageException
     * @throws ObsServiceException
     * @throws SdkClientException
     */
    @Test
    public void testNominalExist() throws ObjectStorageException,
            ObsServiceException, SdkClientException {
        boolean ret = service.exist(ProductFamily.CONFIG, "test-key");
        assertTrue(ret);
        verify(client, times(1)).doesObjectExist(Mockito
                .eq(new ObsObject("test-key", ObsFamily.AUXILIARY_FILE)));

        ret = service.exist(ProductFamily.RAW, "test-key");
        assertFalse(ret);
        verify(client, times(1)).doesObjectExist(
                Mockito.eq(new ObsObject("test-key", ObsFamily.EDRS_SESSION)));
    }

    /**
     * Test exist when client raise ObsServiceException
     * 
     * @throws ObjectStorageException
     */
    @Test
    public void testUploadFileWhenException() throws ObjectStorageException {
        thrown.expect(ObjectStorageException.class);
        thrown.expect(hasProperty("key", is("error-key")));
        thrown.expect(hasProperty("family", is(ProductFamily.CONFIG)));
        thrown.expectMessage("error 1 message");
        thrown.expectCause(isA(ObsServiceException.class));

        service.uploadFile(ProductFamily.CONFIG, "error-key",
                new File("pom.xml"));
    }

    /**
     * Test exist when client raise ObsServiceException
     * 
     * @throws ObjectStorageException
     */
    @Test
    public void testUploadFileWhenException2() throws ObjectStorageException {
        thrown.expect(ObjectStorageException.class);
        thrown.expect(hasProperty("key", is("error-key")));
        thrown.expect(hasProperty("family", is(ProductFamily.RAW)));
        thrown.expectMessage("error 2 message");
        thrown.expectCause(isA(SdkClientException.class));

        service.uploadFile(ProductFamily.RAW, "error-key", new File("pom.xml"));
    }

    /**
     * Test nominal case of exists
     * 
     * @throws ObjectStorageException
     * @throws ObsServiceException
     * @throws SdkClientException
     */
    @Test
    public void testNominalUpload() throws ObjectStorageException,
            ObsServiceException, SdkClientException {
        service.uploadFile(ProductFamily.CONFIG, "test-key",
                new File("pom.xml"));
        verify(client, times(1))
                .uploadObject(Mockito.eq(new ObsUploadObject("test-key",
                        ObsFamily.AUXILIARY_FILE, new File("pom.xml"))));

        service.uploadFile(ProductFamily.RAW, "test-key", new File("pom.xml"));
        verify(client, times(1))
                .uploadObject(Mockito.eq(new ObsUploadObject("test-key",
                        ObsFamily.EDRS_SESSION, new File("pom.xml"))));
    }

    /**
     * Test downloadFile when client raise ObsServiceException
     * 
     * @throws ObjectStorageException
     * @throws ObsUnknownObjectException
     */
    @Test
    public void testDownloadFileWhenException()
            throws ObjectStorageException, ObsUnknownObjectException {
        thrown.expect(ObjectStorageException.class);
        thrown.expect(hasProperty("key", is("error-key")));
        thrown.expect(hasProperty("family", is(ProductFamily.CONFIG)));
        thrown.expectMessage("error 1 message");
        thrown.expectCause(isA(ObsServiceException.class));

        service.downloadFile(ProductFamily.CONFIG, "error-key", "test/");
    }

    /**
     * Test downloadFile when client raise ObsServiceException
     * 
     * @throws ObjectStorageException
     * @throws ObsUnknownObjectException
     */
    @Test
    public void testDownloadFileWhenException2()
            throws ObjectStorageException, ObsUnknownObjectException {
        thrown.expect(ObjectStorageException.class);
        thrown.expect(hasProperty("key", is("error-key")));
        thrown.expect(hasProperty("family", is(ProductFamily.RAW)));
        thrown.expectMessage("error 2 message");
        thrown.expectCause(isA(SdkClientException.class));

        service.downloadFile(ProductFamily.RAW, "error-key", "test/");
    }

    /**
     * Test downloadFile when client raise ObsServiceException
     * 
     * @throws ObjectStorageException
     * @throws ObsUnknownObjectException
     */
    @Test
    public void testDownloadFileWhenUnknown()
            throws ObjectStorageException, ObsUnknownObjectException {
        thrown.expect(ObsUnknownObjectException.class);
        thrown.expect(hasProperty("key", is("test-key")));
        thrown.expect(hasProperty("family", is(ProductFamily.CONFIG)));

        service.downloadFile(ProductFamily.CONFIG, "test-key", "test/");
    }

    /**
     * Test nominal case of downloadFile
     * 
     * @throws ObjectStorageException
     * @throws ObsServiceException
     * @throws SdkClientException
     * @throws ObsUnknownObjectException
     */
    @Test
    public void testNominalDownload() throws ObjectStorageException,
            ObsServiceException, SdkClientException, ObsUnknownObjectException {
        File upload1 =
                service.downloadFile(ProductFamily.RAW, "test-key", "test/");
        verify(client, times(1))
                .downloadObject(Mockito.eq(new ObsDownloadObject("test-key",
                        ObsFamily.EDRS_SESSION, "test/")));
        assertEquals("test-key", upload1.getName());
        assertEquals("test", upload1.getParentFile().getName());

        File upload2 = service.downloadFile(ProductFamily.RAW, "test-key/key2",
                "test/");
        verify(client, times(1)).downloadObject(
                Mockito.eq(new ObsDownloadObject("test-key/key2",
                        ObsFamily.EDRS_SESSION, "test/")));
        assertEquals("key2", upload2.getName());
        assertEquals("test", upload2.getParentFile().getName());
    }

    /**
     * Test the download per batch when an exception occurred
     * 
     * @throws ObsServiceException
     * @throws SdkClientException
     * @throws AbstractCodedException
     */
    @Test
    public void testDownloadBatchWhenException() throws ObsServiceException,
            SdkClientException, AbstractCodedException {
        doThrow(new SdkClientException("error 2 message")).when(client)
                .downloadObjects(Mockito.any(), Mockito.anyBoolean());

        List<S3DownloadFile> filesToDownload = new ArrayList<>();
        filesToDownload.add(
                new S3DownloadFile(ProductFamily.L0_ACN, "key1", "target1"));
        filesToDownload.add(new S3DownloadFile(ProductFamily.L0_PRODUCT, "key2",
                "target1"));
        filesToDownload.add(
                new S3DownloadFile(ProductFamily.L1_ACN, "key3", "target2"));

        thrown.expect(ObsParallelAccessException.class);
        thrown.expectMessage("error 2 message");
        thrown.expectCause(isA(SdkClientException.class));

        service.downloadFilesPerBatch(filesToDownload);
    }

    /**
     * Test the download per batch
     * 
     * @throws ObsServiceException
     * @throws SdkClientException
     * @throws AbstractCodedException
     */
    @Test
    public void testDownloadBatch() throws ObsServiceException,
            SdkClientException, AbstractCodedException {

        doNothing().when(client).downloadObjects(Mockito.any(),
                Mockito.anyBoolean());

        List<S3DownloadFile> filesToDownload = new ArrayList<>();
        filesToDownload.add(new S3DownloadFile(ProductFamily.L0_PRODUCT, "key1",
                "target1"));
        filesToDownload.add(new S3DownloadFile(ProductFamily.L1_PRODUCT, "key2",
                "target1"));
        filesToDownload.add(new S3DownloadFile(ProductFamily.L1_PRODUCT, "key3",
                "target2"));
        List<ObsDownloadObject> objects = new ArrayList<>();
        objects.add(
                new ObsDownloadObject("key1", ObsFamily.L0_PRODUCT, "target1"));
        objects.add(
                new ObsDownloadObject("key2", ObsFamily.L1_PRODUCT, "target1"));
        objects.add(
                new ObsDownloadObject("key3", ObsFamily.L1_PRODUCT, "target2"));

        service.downloadFilesPerBatch(filesToDownload);

        verify(client, only()).downloadObjects(Mockito.eq(objects),
                Mockito.eq(true));
    }

    /**
     * Test the upload per batch when an exception occurred
     * 
     * @throws ObsServiceException
     * @throws SdkClientException
     * @throws AbstractCodedException
     */
    @Test
    public void testUploadBatchWhenException() throws ObsServiceException,
            SdkClientException, AbstractCodedException {
        doThrow(new SdkClientException("error 2 message")).when(client)
                .uploadObjects(Mockito.any(), Mockito.anyBoolean());

        List<S3UploadFile> filesToDownload = new ArrayList<>();
        filesToDownload.add(new S3UploadFile(ProductFamily.L0_ACN, "key1",
                new File("target1")));
        filesToDownload.add(new S3UploadFile(ProductFamily.L0_PRODUCT, "key2",
                new File("target1")));
        filesToDownload.add(new S3UploadFile(ProductFamily.L1_ACN, "key3",
                new File("target2")));

        thrown.expect(ObsParallelAccessException.class);
        thrown.expectMessage("error 2 message");
        thrown.expectCause(isA(SdkClientException.class));

        service.uploadFilesPerBatch(filesToDownload);
    }

    /**
     * Test the upload per batch
     * 
     * @throws ObsServiceException
     * @throws SdkClientException
     * @throws AbstractCodedException
     */
    @Test
    public void testUploadBatch() throws ObsServiceException,
            SdkClientException, AbstractCodedException {

        doNothing().when(client).uploadObjects(Mockito.any(),
                Mockito.anyBoolean());

        List<S3UploadFile> filesToDownload = new ArrayList<>();
        filesToDownload.add(new S3UploadFile(ProductFamily.L0_PRODUCT, "key1",
                new File("target1")));
        filesToDownload.add(new S3UploadFile(ProductFamily.L1_PRODUCT, "key2",
                new File("target1")));
        filesToDownload.add(new S3UploadFile(ProductFamily.L1_PRODUCT, "key3",
                new File("target2")));
        List<ObsUploadObject> objects = new ArrayList<>();
        objects.add(new ObsUploadObject("key1", ObsFamily.L0_PRODUCT,
                new File("target1")));
        objects.add(new ObsUploadObject("key2", ObsFamily.L1_PRODUCT,
                new File("target1")));
        objects.add(new ObsUploadObject("key3", ObsFamily.L1_PRODUCT,
                new File("target2")));

        service.uploadFilesPerBatch(filesToDownload);

        verify(client, only()).uploadObjects(Mockito.eq(objects),
                Mockito.eq(true));
    }
}

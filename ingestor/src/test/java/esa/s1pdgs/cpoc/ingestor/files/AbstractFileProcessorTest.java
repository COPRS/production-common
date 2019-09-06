package esa.s1pdgs.cpoc.ingestor.files;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.Message;

import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiPublicationError;
import esa.s1pdgs.cpoc.common.errors.obs.ObsException;
import esa.s1pdgs.cpoc.common.errors.processing.IngestorFilePathException;
import esa.s1pdgs.cpoc.common.errors.processing.IngestorIgnoredFileException;
import esa.s1pdgs.cpoc.ingestor.FileUtils;
import esa.s1pdgs.cpoc.ingestor.files.model.FileDescriptor;
import esa.s1pdgs.cpoc.ingestor.files.services.AbstractFileDescriptorService;
import esa.s1pdgs.cpoc.ingestor.kafka.PublicationServices;
import esa.s1pdgs.cpoc.ingestor.status.AppStatus;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsServiceException;
import esa.s1pdgs.cpoc.obs_sdk.ObsUploadObject;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;

public class AbstractFileProcessorTest {

    /**
     * Amazon S3 service for configuration files
     */
    @Mock
    private ObsClient obsClient;

    /**
     * KAFKA producer on the topic "metadata"
     */
    @Mock
    private PublicationServices<String> publisher;

    /**
     * Builder of file descriptors
     */
    @Mock
    private AbstractFileDescriptorService extractor;
    

    /**
     * Application status
     */
    @Mock
    private AppStatus appStatus;

    /**
     * Service to test
     */
    private FileProcessorImpl service;

    /**
     * Message file of test
     */
    @Mock
    private Message<File> message;

    /**
     * File test
     */
    private File file = new File("/tmp/test.xml");
    
    private File backupFile = new File("/tmp/bkp/test.xml");

    /**
     * To check the raised custom exceptions
     */
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Initialization
     * 
     * @throws IOException
     */
    @Before
    public void init() throws IOException {
        MockitoAnnotations.initMocks(this);

        file.createNewFile();
        
        Files.createDirectory(backupFile.getParentFile().toPath());
        
        doReturn(file).when(message).getPayload();

        service = new FileProcessorImpl(obsClient, publisher, extractor, appStatus, file.getParent(), backupFile.getParent());
    }

    /**
     * Cleaning
     */
    @After
    public void clean() {
        if (file.exists()) {
            file.delete();
        }
        
        if (backupFile.exists()) {
        	backupFile.delete();
        }
        
        
        if (backupFile.getParentFile().exists())
        {
        	backupFile.getParentFile().delete();
        }
    }

    /**
     * Test that directory are completely ignored
     */
    @Test
    public void testProcessWhenDirectory() {
        doReturn(FileUtils.TEST_DIR).when(message).getPayload();
        service.processFile(message);
        verifyZeroInteractions(obsClient);
        verifyZeroInteractions(publisher);
        verifyZeroInteractions(extractor);
        assertTrue(file.exists());
        assertFalse(backupFile.exists());
    }

    /**
     * Test when extraction failed due to file ignored
     * 
     * @throws ObsException
     * @throws IngestorFilePathException
     * @throws IngestorIgnoredFileException
     */
    @Test
    public void testProcessWhenExtractionSendIngestorFilePathException()
            throws ObsException, IngestorFilePathException,
            IngestorIgnoredFileException {
        IngestorFilePathException exc = new IngestorFilePathException("path",
                "family", "error message");
        doThrow(exc).when(extractor).extractDescriptor(Mockito.any());

        service.processFile(message);

        verify(extractor, times(1)).extractDescriptor(Mockito.eq(file));
        verifyZeroInteractions(obsClient);
        verifyZeroInteractions(publisher);
        assertFalse(file.exists());
        assertTrue(backupFile.exists());
    }

    /**
     * Test when extraction failed due to file ignored
     * 
     * @throws ObsException
     * @throws IngestorFilePathException
     * @throws IngestorIgnoredFileException
     */
    @Test
    public void testProcessWhenExtractionSendIngestorIgnoredFileException()
            throws ObsException, IngestorFilePathException,
            IngestorIgnoredFileException {
        IngestorIgnoredFileException exc =
                new IngestorIgnoredFileException("ignored-name");
        doThrow(exc).when(extractor).extractDescriptor(Mockito.any());

        service.processFile(message);

        verify(extractor, times(1)).extractDescriptor(Mockito.eq(file));
        verifyZeroInteractions(obsClient);
        verifyZeroInteractions(publisher);
        assertFalse(file.exists());
        assertTrue(backupFile.exists());
    }

    @Test
    public void testProcessWhenFileAlreadyExist() throws ObsException,
            IngestorFilePathException, IngestorIgnoredFileException, ObsServiceException, SdkClientException {
        FileDescriptor desc = getFileDescriptor(true);
        doReturn(desc).when(extractor).extractDescriptor(Mockito.any());
        doReturn(true).when(obsClient).exists(Mockito.any());

        service.processFile(message);

        verify(extractor, times(1)).extractDescriptor(Mockito.eq(file));
        verify(obsClient, times(1)).exists(Mockito.eq(new ObsObject(ProductFamily.BLANK, desc.getKeyObjectStorage())));
        verifyZeroInteractions(publisher);
        assertFalse(file.exists());
        assertTrue(backupFile.exists());
    }

    @Test
    public void testProcessWhenObsError() throws SdkClientException, AbstractCodedException {
        FileDescriptor desc = getFileDescriptor(true);
        doReturn(desc).when(extractor).extractDescriptor(Mockito.any());

        SdkClientException exc = new SdkClientException("error");
        doThrow(exc).when(obsClient).exists(Mockito.any());

        service.processFile(message);

        verify(extractor, times(1)).extractDescriptor(Mockito.eq(file));
        verify(obsClient, times(1)).exists(Mockito.eq(new ObsObject(ProductFamily.BLANK, desc.getKeyObjectStorage())));
        verify(obsClient, never()).upload(Mockito.anyList());
        verifyZeroInteractions(publisher);
        assertFalse(file.exists());
        assertTrue(backupFile.exists());
    }

    @SuppressWarnings("unchecked")
	@Test
    public void testProcessWhenObsError2() throws SdkClientException, AbstractCodedException {
        FileDescriptor desc = getFileDescriptor(true);
        doReturn(desc).when(extractor).extractDescriptor(Mockito.any());
        doReturn(false).when(obsClient).exists(Mockito.any());

        ObsException exc = new ObsException(ProductFamily.BLANK, "key-obs",
                new Exception("cause"));
        doThrow(exc).when(obsClient).upload(Mockito.anyList());

        service.processFile(message);

        verify(extractor, times(1)).extractDescriptor(Mockito.eq(file));
        verify(obsClient, times(1)).exists(Mockito.eq(new ObsObject(ProductFamily.BLANK, desc.getKeyObjectStorage())));
        verify(obsClient, times(1)).upload((List<ObsUploadObject>) ArgumentMatchers.argThat(s -> ((List<ObsUploadObject>) s).contains(
        		new ObsUploadObject(ProductFamily.BLANK, desc.getKeyObjectStorage(), file))));
        verifyZeroInteractions(publisher);
        assertFalse(file.exists());
        assertTrue(backupFile.exists());
    }
    
    @Test
    public void testProcessWhenBackupError() throws IOException, ObsServiceException, SdkClientException, AbstractCodedException {
        FileDescriptor desc = getFileDescriptor(true);
        doReturn(desc).when(extractor).extractDescriptor(Mockito.any());

        SdkClientException exc = new SdkClientException("error");
        doThrow(exc).when(obsClient).exists(Mockito.any());

        service = new FileProcessorImpl(obsClient, publisher, extractor, appStatus, file.getParent(), "/");
        service.processFile(message);

        verify(extractor, times(1)).extractDescriptor(Mockito.eq(file));
        verify(obsClient, times(1)).exists(Mockito.eq(new ObsObject(ProductFamily.BLANK, desc.getKeyObjectStorage())));;
        verify(obsClient, never()).upload(Mockito.anyList());
        verifyZeroInteractions(publisher);
        assertTrue(file.exists());
        assertFalse(backupFile.exists());
    }

    @SuppressWarnings("unchecked")
	@Test
    public void testProcessWhenNoNeedPublication() throws ObsServiceException, SdkClientException, AbstractCodedException {
        FileDescriptor desc = getFileDescriptor(false);
        doReturn(desc).when(extractor).extractDescriptor(Mockito.any());
        doReturn(false).when(obsClient).exists(Mockito.any());
        doNothing().when(obsClient).upload(Mockito.anyList());

        service.processFile(message);

        verify(extractor, times(1)).extractDescriptor(Mockito.eq(file));
        verify(obsClient, times(1)).exists(Mockito.eq(new ObsObject(ProductFamily.BLANK, desc.getKeyObjectStorage())));
        verify(obsClient, times(1)).upload((List<ObsUploadObject>) ArgumentMatchers.argThat(s -> ((List<ObsUploadObject>) s).contains(
        		new ObsUploadObject(ProductFamily.BLANK, desc.getKeyObjectStorage(), file))));
        verifyZeroInteractions(publisher);
        assertFalse(file.exists());
        assertFalse(backupFile.exists());
    }
    

    @SuppressWarnings("unchecked")
	@Test
    public void testProcessWhenNeedPublication()
            throws ObsServiceException, SdkClientException, AbstractCodedException {
        FileDescriptor desc = getFileDescriptor(true);
        doReturn(desc).when(extractor).extractDescriptor(Mockito.any());
        doReturn(false).when(obsClient).exists(Mockito.any());
        doNothing().when(obsClient).upload(Mockito.anyList());

        service.processFile(message);

        verify(extractor, times(1)).extractDescriptor(Mockito.eq(file));
        verify(obsClient, times(1)).exists(Mockito.eq(new ObsObject(ProductFamily.BLANK, desc.getKeyObjectStorage())));
        verify(obsClient, times(1)).upload((List<ObsUploadObject>) ArgumentMatchers.argThat(s -> ((List<ObsUploadObject>) s).contains(
        		new ObsUploadObject(ProductFamily.BLANK, desc.getKeyObjectStorage(), file))));
        verify(publisher, times(1)).send(Mockito.eq(desc.getProductName()));
        assertFalse(file.exists());
        assertFalse(backupFile.exists());
    }

    private FileDescriptor getFileDescriptor(boolean published) {
        FileDescriptor desc = new FileDescriptor();
        desc.setProductName("product-name");
        desc.setKeyObjectStorage("key-obs");
        desc.setChannel(15);
        desc.setProductType(EdrsSessionFileType.RAW);
        desc.setMissionId("mission");
        desc.setSatelliteId("sat");
        desc.setHasToBePublished(published);
        return desc;
    }

}

/**
 * Implementation of fileprocessor for tests
 */
class FileProcessorImpl extends AbstractFileProcessor<String> {

    /**
     * @param obsClient
     * @param publisher
     * @param extractor
     */
    public FileProcessorImpl(final ObsClient obsClient,
            final PublicationServices<String> publisher,
            final AbstractFileDescriptorService extractor,
            final AppStatus appStatus,
            final String pickupDirectory,
            final String backupDirectory) {
        super(obsClient, publisher, extractor, ProductFamily.BLANK, appStatus, pickupDirectory, backupDirectory);
    }

    /**
     * 
     */
    @Override
    protected String buildDto(final FileDescriptor descriptor) {
        return descriptor.getProductName();
    }

}

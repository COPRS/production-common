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

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.Message;

import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiPublicationError;
import esa.s1pdgs.cpoc.common.errors.obs.ObsException;
import esa.s1pdgs.cpoc.common.errors.processing.IngestorFilePathException;
import esa.s1pdgs.cpoc.common.errors.processing.IngestorIgnoredFileException;
import esa.s1pdgs.cpoc.ingestor.FileUtils;
import esa.s1pdgs.cpoc.ingestor.files.model.FileDescriptor;
import esa.s1pdgs.cpoc.ingestor.files.services.AbstractFileDescriptorService;
import esa.s1pdgs.cpoc.ingestor.kafka.PublicationServices;
import esa.s1pdgs.cpoc.ingestor.obs.ObsService;

public class AbstractFileProcessorTest {

    /**
     * Amazon S3 service for configuration files
     */
    @Mock
    private ObsService obsService;

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
    private File file = new File("test.xml");

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

        doReturn(file).when(message).getPayload();

        service = new FileProcessorImpl(obsService, publisher, extractor);
    }

    /**
     * Cleaning
     */
    @After
    public void clean() {
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * Test that directory are completely ignored
     */
    @Test
    public void testProcessWhenDirectory() {
        doReturn(FileUtils.TEST_DIR).when(message).getPayload();
        service.processFile(message);
        verifyZeroInteractions(obsService);
        verifyZeroInteractions(publisher);
        verifyZeroInteractions(extractor);
        assertTrue(file.exists());
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
        verifyZeroInteractions(obsService);
        verifyZeroInteractions(publisher);
        assertFalse(file.exists());
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
        verifyZeroInteractions(obsService);
        verifyZeroInteractions(publisher);
        assertFalse(file.exists());
    }

    @Test
    public void testProcessWhenFileAlreadyExist() throws ObsException,
            IngestorFilePathException, IngestorIgnoredFileException {
        FileDescriptor desc = getFileDescriptor(true);
        doReturn(desc).when(extractor).extractDescriptor(Mockito.any());
        doReturn(true).when(obsService).exist(Mockito.any(),
                Mockito.anyString());

        service.processFile(message);

        verify(extractor, times(1)).extractDescriptor(Mockito.eq(file));
        verify(obsService, times(1)).exist(Mockito.eq(ProductFamily.BLANK),
                Mockito.eq(desc.getKeyObjectStorage()));
        verifyZeroInteractions(publisher);
        assertFalse(file.exists());
    }

    @Test
    public void testProcessWhenObsError() throws ObsException,
            IngestorFilePathException, IngestorIgnoredFileException {
        FileDescriptor desc = getFileDescriptor(true);
        doReturn(desc).when(extractor).extractDescriptor(Mockito.any());

        ObsException exc = new ObsException(ProductFamily.BLANK, "key-obs",
                new Exception("cause"));
        doThrow(exc).when(obsService).exist(Mockito.any(), Mockito.anyString());

        service.processFile(message);

        verify(extractor, times(1)).extractDescriptor(Mockito.eq(file));
        verify(obsService, times(1)).exist(Mockito.eq(ProductFamily.BLANK),
                Mockito.eq(desc.getKeyObjectStorage()));
        verify(obsService, never()).uploadFile(Mockito.any(),
                Mockito.anyString(), Mockito.any());
        verifyZeroInteractions(publisher);
        assertTrue(file.exists());
    }

    @Test
    public void testProcessWhenObsError2() throws ObsException,
            IngestorFilePathException, IngestorIgnoredFileException {
        FileDescriptor desc = getFileDescriptor(true);
        doReturn(desc).when(extractor).extractDescriptor(Mockito.any());
        doReturn(false).when(obsService).exist(Mockito.any(),
                Mockito.anyString());

        ObsException exc = new ObsException(ProductFamily.BLANK, "key-obs",
                new Exception("cause"));
        doThrow(exc).when(obsService).uploadFile(Mockito.any(),
                Mockito.anyString(), Mockito.any());

        service.processFile(message);

        verify(extractor, times(1)).extractDescriptor(Mockito.eq(file));
        verify(obsService, times(1)).exist(Mockito.eq(ProductFamily.BLANK),
                Mockito.eq(desc.getKeyObjectStorage()));
        verify(obsService, times(1)).uploadFile(Mockito.eq(ProductFamily.BLANK),
                Mockito.eq(desc.getKeyObjectStorage()), Mockito.eq(file));
        verifyZeroInteractions(publisher);
        assertTrue(file.exists());
    }

    @Test
    public void testProcessWhenNoNeedPublication() throws ObsException,
            IngestorFilePathException, IngestorIgnoredFileException {
        FileDescriptor desc = getFileDescriptor(false);
        doReturn(desc).when(extractor).extractDescriptor(Mockito.any());
        doReturn(false).when(obsService).exist(Mockito.any(),
                Mockito.anyString());
        doNothing().when(obsService).uploadFile(Mockito.any(),
                Mockito.anyString(), Mockito.any());

        service.processFile(message);

        verify(extractor, times(1)).extractDescriptor(Mockito.eq(file));
        verify(obsService, times(1)).exist(Mockito.eq(ProductFamily.BLANK),
                Mockito.eq(desc.getKeyObjectStorage()));
        verify(obsService, times(1)).uploadFile(Mockito.eq(ProductFamily.BLANK),
                Mockito.eq(desc.getKeyObjectStorage()), Mockito.eq(file));
        verifyZeroInteractions(publisher);
        assertFalse(file.exists());
    }

    @Test
    public void testProcessWhenNeedPublication()
            throws ObsException, IngestorFilePathException,
            IngestorIgnoredFileException, MqiPublicationError {
        FileDescriptor desc = getFileDescriptor(true);
        doReturn(desc).when(extractor).extractDescriptor(Mockito.any());
        doReturn(false).when(obsService).exist(Mockito.any(),
                Mockito.anyString());
        doNothing().when(obsService).uploadFile(Mockito.any(),
                Mockito.anyString(), Mockito.any());

        service.processFile(message);

        verify(extractor, times(1)).extractDescriptor(Mockito.eq(file));
        verify(obsService, times(1)).exist(Mockito.eq(ProductFamily.BLANK),
                Mockito.eq(desc.getKeyObjectStorage()));
        verify(obsService, times(1)).uploadFile(Mockito.eq(ProductFamily.BLANK),
                Mockito.eq(desc.getKeyObjectStorage()), Mockito.eq(file));
        verify(publisher, times(1)).send(Mockito.eq(desc.getProductName()));
        assertFalse(file.exists());
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
     * @param obsService
     * @param publisher
     * @param extractor
     */
    public FileProcessorImpl(final ObsService obsService,
            final PublicationServices<String> publisher,
            final AbstractFileDescriptorService extractor) {
        super(obsService, publisher, extractor, ProductFamily.BLANK);
    }

    /**
     * 
     */
    @Override
    protected String buildDto(final FileDescriptor descriptor) {
        return descriptor.getProductName();
    }

}

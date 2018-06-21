package fr.viveris.s1pdgs.level0.wrapper.services.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import fr.viveris.s1pdgs.level0.wrapper.TestUtils;
import fr.viveris.s1pdgs.level0.wrapper.controller.dto.JobOutputDto;
import fr.viveris.s1pdgs.level0.wrapper.model.ProductFamily;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.AbstractCodedException;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.KafkaSendException;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.UnknownFamilyException;
import fr.viveris.s1pdgs.level0.wrapper.model.kafka.FileQueueMessage;
import fr.viveris.s1pdgs.level0.wrapper.model.kafka.ObsQueueMessage;
import fr.viveris.s1pdgs.level0.wrapper.model.s3.S3UploadFile;
import fr.viveris.s1pdgs.level0.wrapper.services.kafka.OutputProcuderFactory;
import fr.viveris.s1pdgs.level0.wrapper.services.s3.ObsService;

/**
 * Test the output processor
 * 
 * @author Viveris TEchnologies
 */
public class OutputProcessorTest {

    /**
     * Test directory
     */
    private static final String PATH_DIRECTORY_TEST = "./test/outputs/";

    /**
     * Processor to test
     */
    private OutputProcessor processor;

    /**
     * OBS service
     */
    @Mock
    private ObsService obsService;

    /**
     * Output producer factory for message queue system
     */
    @Mock
    private OutputProcuderFactory procuderFactory;

    /**
     * List of outputs in job
     */
    private List<JobOutputDto> authorizedOutputs;

    /**
     * Product outputs to publish
     */
    private List<S3UploadFile> uploadBatch;
    private List<ObsQueueMessage> outputToPublish;

    /**
     * Reports output to publish
     */
    private List<FileQueueMessage> reportToPublish;

    /**
     * Initialization
     * 
     * @throws AbstractCodedException
     */
    @Before
    public void init() throws AbstractCodedException {
        // Init mocks
        MockitoAnnotations.initMocks(this);

        // Objects
        authorizedOutputs = new ArrayList<>();
        authorizedOutputs.add(TestUtils.buildProductOutputDto(
                PATH_DIRECTORY_TEST + "^.*SM_RAW__0S.*$"));
        authorizedOutputs.add(TestUtils.buildProductOutputDto(
                PATH_DIRECTORY_TEST + "^.*IW_RAW__0S.*$"));
        authorizedOutputs.add(TestUtils
                .buildAcnOutputDto(PATH_DIRECTORY_TEST + "^.*SM_RAW__0A.*$"));
        authorizedOutputs.add(TestUtils
                .buildAcnOutputDto(PATH_DIRECTORY_TEST + "^.*IW_RAW__0A.*$"));
        authorizedOutputs.add(TestUtils
                .buildAcnOutputDto(PATH_DIRECTORY_TEST + "^.*EW_RAW__0A.*$"));
        authorizedOutputs.add(TestUtils.buildReportOutputDto(
                PATH_DIRECTORY_TEST + "^S1[A|B|_]_OPER_REP_PASS.*.EOF$"));
        authorizedOutputs.add(TestUtils
                .buildReportOutputDto(PATH_DIRECTORY_TEST + "^.*report.*$"));
        authorizedOutputs.add(TestUtils
                .buildBlankOutputDto(PATH_DIRECTORY_TEST + "^.*BLANK.*$"));

        // Outputs product
        uploadBatch = new ArrayList<>();
        uploadBatch.add(new S3UploadFile(ProductFamily.L0_PRODUCT, "o1",
                new File("o1")));
        uploadBatch.add(
                new S3UploadFile(ProductFamily.L1_ACN, "o2", new File("o2")));
        uploadBatch.add(
                new S3UploadFile(ProductFamily.L0_ACN, "o3", new File("o3")));
        outputToPublish = new ArrayList<>();
        outputToPublish
                .add(new ObsQueueMessage(ProductFamily.L0_PRODUCT, "p1", "o1"));
        outputToPublish
                .add(new ObsQueueMessage(ProductFamily.L1_ACN, "p2", "o2"));
        outputToPublish
                .add(new ObsQueueMessage(ProductFamily.L0_ACN, "p3", "o3"));

        // Outputs report
        reportToPublish = new ArrayList<>();
        reportToPublish.add(new FileQueueMessage(ProductFamily.L0_REPORT, "p1",
                new File("p1")));
        reportToPublish.add(new FileQueueMessage(ProductFamily.L1_REPORT, "p2",
                new File("p2")));
        reportToPublish.add(new FileQueueMessage(ProductFamily.L0_REPORT, "p3",
                new File("p3")));

        processor = new OutputProcessor(obsService, procuderFactory,
                PATH_DIRECTORY_TEST, authorizedOutputs,
                PATH_DIRECTORY_TEST + "outputs.list", 2, "MONITOR");

        // Mocks
        doNothing().when(obsService).uploadFilesPerBatch(Mockito.any());
        doNothing().when(procuderFactory)
                .sendOutput(Mockito.any(ObsQueueMessage.class));
        doNothing().when(procuderFactory)
                .sendOutput(Mockito.any(FileQueueMessage.class));
    }

    /**
     * TEst the file name extraction from the list file
     * 
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testExtractFiles() throws NoSuchMethodException,
            SecurityException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        Method method = processor.getClass().getDeclaredMethod("extractFiles");
        method.setAccessible(true);

        List<String> result = (List) method.invoke(processor);
        assertEquals(9, result.size());
        assertEquals(
                "NRT/S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.ISIP",
                result.get(0));
        assertEquals(
                "NRT/S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.ISIP",
                result.get(1));
        assertEquals("NRT/report.xml", result.get(2));
        assertEquals("report_1.xml", result.get(3));
        assertEquals("report_2.xml", result.get(4));
        assertEquals("S1A_BLANK_FILE.EOF", result.get(5));
    }

    /**
     * Test getProductName
     * 
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    @Test
    public void testGetProductName() throws NoSuchMethodException,
            SecurityException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        Method method = processor.getClass().getDeclaredMethod("getProductName",
                String.class);
        method.setAccessible(true);

        String str = (String) method.invoke(processor, "NRT/file.xml");
        assertEquals("file.xml", str);

        str = (String) method.invoke(processor, "file2.xml");
        assertEquals("file2.xml", str);

        str = (String) method.invoke(processor, "NRT/DIR/file2.xml");
        assertEquals("DIR/file2.xml", str);

        str = (String) method.invoke(processor,
                "NRT/file2." + OutputProcessor.EXT_ISIP.toLowerCase());
        assertEquals("file2." + OutputProcessor.EXT_SAFE, str);
    }

    /**
     * Test getProductName
     * 
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    @Test
    public void testGetFilePath() throws NoSuchMethodException,
            SecurityException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        Method method = processor.getClass().getDeclaredMethod("getFilePath",
                String.class, String.class);
        method.setAccessible(true);

        String str =
                (String) method.invoke(processor, "NRT/file.xml", "file.xml");
        assertEquals(PATH_DIRECTORY_TEST + "NRT/file.xml", str);

        str = (String) method.invoke(processor, "file2.xml", "file2.xml");
        assertEquals(PATH_DIRECTORY_TEST + "file2.xml", str);

        str = (String) method.invoke(processor,
                "NRT/file2." + OutputProcessor.EXT_ISIP,
                "file2." + OutputProcessor.EXT_SAFE);
        assertEquals(
                PATH_DIRECTORY_TEST + "NRT/file2." + OutputProcessor.EXT_ISIP
                        + File.separator + "file2." + OutputProcessor.EXT_SAFE,
                str);
    }

    /**
     * Test getProductName
     * 
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    @Test
    public void testGetMatchOutput() throws NoSuchMethodException,
            SecurityException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        Method method = processor.getClass().getDeclaredMethod("getMatchOutput",
                String.class);
        method.setAccessible(true);

        JobOutputDto dto = (JobOutputDto) method.invoke(processor,
                "S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE");
        assertNotNull(dto);

        dto = (JobOutputDto) method.invoke(processor, "report.xml");
        assertNotNull(dto);

        dto = (JobOutputDto) method.invoke(processor,
                OutputProcessor.NOT_KEY_OBS);
        assertNull(dto);
    }

    /**
     * TEst sort outputs
     * 
     * @throws UnknownFamilyException
     */
    @Test
    public void testSortOutputs() throws UnknownFamilyException {
        List<S3UploadFile> uploadBatch = new ArrayList<>();
        List<ObsQueueMessage> outputToPublish = new ArrayList<>();
        List<FileQueueMessage> reportToPublish = new ArrayList<>();
        List<String> lines = new ArrayList<>();
        lines.add(
                "NRT/S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.ISIP");
        lines.add(
                "NRT/S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.ISIP");
        lines.add("NRT/report.xml");
        lines.add("not_match");
        lines.add("S1A_report_1.xml");
        lines.add("S1A_report_3.xml");
        lines.add("S1A_BLANK_FILE.SAFE");

        processor.sortOutputs(lines, uploadBatch, outputToPublish,
                reportToPublish);

        // Check products
        assertEquals(2, uploadBatch.size());
        assertEquals(new S3UploadFile(ProductFamily.L0_ACN,
                "S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE",
                new File(PATH_DIRECTORY_TEST
                        + "NRT/S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.ISIP"
                        + File.separator
                        + "S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE")),
                uploadBatch.get(0));

        assertEquals(2, outputToPublish.size());
        assertEquals(new ObsQueueMessage(ProductFamily.L0_PRODUCT,
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE"),
                outputToPublish.get(1));

        // Check report
        assertEquals(3, reportToPublish.size());
        assertEquals(
                new FileQueueMessage(ProductFamily.L0_REPORT,
                        "S1A_report_3.xml",
                        new File(PATH_DIRECTORY_TEST + "S1A_report_3.xml")),
                reportToPublish.get(2));
    }

    /**
     * TEst when an output match a family not considered
     * 
     * @throws UnknownFamilyException
     */
    @Test(expected = UnknownFamilyException.class)
    public void testSortOutputsWithInvalidFamily()
            throws UnknownFamilyException {
        authorizedOutputs.add(
                new JobOutputDto("RAW", PATH_DIRECTORY_TEST + "^.*RAW.*$"));
        List<S3UploadFile> uploadBatch = new ArrayList<>();
        List<ObsQueueMessage> outputToPublish = new ArrayList<>();
        List<FileQueueMessage> reportToPublish = new ArrayList<>();
        List<String> lines = new ArrayList<>();
        lines.add(
                "NRT/S1A_I_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.ISIP");
        lines.add(
                "NRT/S1A_I_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.ISIP");
        lines.add("NRT/report.xml");
        lines.add("not_match");
        lines.add("S1A_report_1.xml");
        lines.add("S1A_report_3.xml");
        lines.add("S1A_BLANK_FILE.SAFE");

        processor.sortOutputs(lines, uploadBatch, outputToPublish,
                reportToPublish);
    }

    /**
     * Test the publication of the reports
     * 
     * @throws AbstractCodedException
     */
    @Test
    public void testProcessReports() throws AbstractCodedException {

        processor.processReports(new ArrayList<>());
        verify(procuderFactory, times(0))
                .sendOutput(Mockito.any(FileQueueMessage.class));

        processor.processReports(reportToPublish);

        verify(procuderFactory, times(3))
                .sendOutput(Mockito.any(FileQueueMessage.class));
        verify(procuderFactory, times(1))
                .sendOutput(Mockito.eq(reportToPublish.get(0)));
        verify(procuderFactory, times(1))
                .sendOutput(Mockito.eq(reportToPublish.get(1)));
        verify(procuderFactory, times(1))
                .sendOutput(Mockito.eq(reportToPublish.get(2)));
    }

    /**
     * Test the publication of the reports
     * 
     * @throws AbstractCodedException
     */
    @Test
    public void testProcessReportsWhenKAfkaException()
            throws AbstractCodedException {
        doThrow(new KafkaSendException("topic", "dto", "name", "message",
                new IllegalArgumentException("cause"))).when(procuderFactory)
                        .sendOutput(Mockito.eq(reportToPublish.get(0)));

        processor.processReports(reportToPublish);

        verify(procuderFactory, times(3))
                .sendOutput(Mockito.any(FileQueueMessage.class));
        verify(procuderFactory, times(1))
                .sendOutput(Mockito.eq(reportToPublish.get(0)));
        verify(procuderFactory, times(1))
                .sendOutput(Mockito.eq(reportToPublish.get(1)));
        verify(procuderFactory, times(1))
                .sendOutput(Mockito.eq(reportToPublish.get(2)));
    }

    @Test
    public void testPublishAccordingUploadFiles1() throws Exception {
        Method method = getMethodForPublishAccodingUpload();

        method.invoke(processor, 2, "", new ArrayList<>());
        verify(procuderFactory, times(0))
                .sendOutput(Mockito.any(ObsQueueMessage.class));

        method.invoke(processor, 2, "o2", outputToPublish);
        verify(procuderFactory, times(1))
                .sendOutput(Mockito.any(ObsQueueMessage.class));
        verify(procuderFactory, times(1)).sendOutput(Mockito
                .eq(new ObsQueueMessage(ProductFamily.L0_PRODUCT, "p1", "o1")));
        assertEquals(2, outputToPublish.size());
    }

    @Test
    public void testPublishAccordingUploadFiles2() throws Exception {
        Method method = getMethodForPublishAccodingUpload();

        method.invoke(processor, 2, "", new ArrayList<>());
        verify(procuderFactory, times(0))
                .sendOutput(Mockito.any(ObsQueueMessage.class));

        method.invoke(processor, 2, OutputProcessor.NOT_KEY_OBS,
                outputToPublish);
        verify(procuderFactory, times(3))
                .sendOutput(Mockito.any(ObsQueueMessage.class));
        assertEquals(0, outputToPublish.size());
    }

    @Test
    public void testPublishAccordingUploadFilesWhenKafkaError() throws Exception {
        doThrow(new KafkaSendException("topic", "dto", "name", "message",
                new IllegalArgumentException("cause"))).when(procuderFactory)
                        .sendOutput(Mockito.eq(outputToPublish.get(0)));
        
        Method method = getMethodForPublishAccodingUpload();

        method.invoke(processor, 2, "", new ArrayList<>());
        verify(procuderFactory, times(0))
                .sendOutput(Mockito.any(ObsQueueMessage.class));

        method.invoke(processor, 2, OutputProcessor.NOT_KEY_OBS,
                outputToPublish);
        verify(procuderFactory, times(3))
                .sendOutput(Mockito.any(ObsQueueMessage.class));
        assertEquals(0, outputToPublish.size());
    }

    private Method getMethodForPublishAccodingUpload()
            throws NoSuchMethodException, SecurityException {
        Method method = processor.getClass().getDeclaredMethod(
                "publishAccordingUploadFiles", double.class, String.class,
                List.class);
        method.setAccessible(true);
        return method;
    }

    @Test
    public void testProcessProducts() throws AbstractCodedException {
        processor.processProducts(uploadBatch, outputToPublish);

        // check publication
        verify(procuderFactory, times(3))
                .sendOutput(Mockito.any(ObsQueueMessage.class));

        // check OBS service
        verify(obsService, times(2)).uploadFilesPerBatch(Mockito.any());
        List<S3UploadFile> batch1 = uploadBatch.subList(0, 2);
        verify(obsService, times(1)).uploadFilesPerBatch(Mockito.eq(batch1));
        List<S3UploadFile> batch2 = uploadBatch.subList(2, 3);
        verify(obsService, times(1)).uploadFilesPerBatch(Mockito.eq(batch2));
    }

    @Test
    public void testProcessOutputs() throws AbstractCodedException {
        processor.processOutput();

        // check publication
        verify(procuderFactory, times(4))
                .sendOutput(Mockito.any(ObsQueueMessage.class));
        verify(procuderFactory, times(3))
                .sendOutput(Mockito.any(FileQueueMessage.class));

        // check OBS service
        verify(obsService, times(2)).uploadFilesPerBatch(Mockito.any());
    }
}

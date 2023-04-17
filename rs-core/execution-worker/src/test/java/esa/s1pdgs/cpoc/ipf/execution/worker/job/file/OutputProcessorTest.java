package esa.s1pdgs.cpoc.ipf.execution.worker.job.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.util.StreamUtils;

import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.CommonConfigurationProperties;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiPublicationError;
import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.common.utils.Streams;
import esa.s1pdgs.cpoc.ipf.execution.worker.TestUtils;
import esa.s1pdgs.cpoc.ipf.execution.worker.config.ApplicationProperties;
import esa.s1pdgs.cpoc.ipf.execution.worker.job.model.mqi.FileQueueMessage;
import esa.s1pdgs.cpoc.ipf.execution.worker.job.model.mqi.ObsQueueMessage;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobOutputDto;
import esa.s1pdgs.cpoc.obs_sdk.FileObsUploadObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingUtils;

/**
 * Test the output processor
 * 
 * @author Viveris TEchnologies
 */
public class OutputProcessorTest {
	
	private final File tmpDir = FileUtils.createTmpDir();

    /**
     * Test directory
     */
    private final String PATH_DIRECTORY_TEST = tmpDir.getPath();

    /**
     * Processor to test
     */
    private OutputProcessor processor;
    private OutputProcessor processorWithWildcardList;

    /**
     * OBS service
     */
    @Mock
    private ObsClient obsClient;
    
    @Mock
    private ApplicationProperties properties;
    
    private CommonConfigurationProperties commonProperties;

    /**
     * List of outputs in job
     */
    private IpfExecutionJob inputMessage;
    private List<LevelJobOutputDto> authorizedOutputs;

    /**
     * Product outputs to publish
     */
    private List<FileObsUploadObject> uploadBatch;
    private List<ObsQueueMessage> outputToPublish;

    /**
     * Reports output to publish
     */
    private List<FileQueueMessage> reportToPublish;

    private final Reporting reporting = ReportingUtils.newReportingBuilder(MissionId.S1).newReporting("TestOutputHandling");
    
    /**
     * Initialization
     * 
     * @throws AbstractCodedException
     */
    @Before
    public void init() throws Exception {
        // Init mocks
        MockitoAnnotations.initMocks(this);

        // Objects
        inputMessage = new IpfExecutionJob(ProductFamily.L0_JOB, "product-name", "FAST24",
                        PATH_DIRECTORY_TEST, "job-order", "FAST24", new UUID(23L, 42L));
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
        authorizedOutputs.add(TestUtils.buildProductOutputDto(
                PATH_DIRECTORY_TEST + "^.*GP_RAW__0S.*$"));
        authorizedOutputs.add(TestUtils.buildReportOutputDto(
                PATH_DIRECTORY_TEST + "^S1[A|B|_]_OPER_REP_PASS.*.EOF$"));
        authorizedOutputs.add(TestUtils
                .buildReportOutputDto(PATH_DIRECTORY_TEST + "^.*report.*$"));
        authorizedOutputs.add(TestUtils
                .buildBlankOutputDto(PATH_DIRECTORY_TEST + "^.*BLANK.*$"));
        authorizedOutputs.add(TestUtils.buildL1ProductOutputDto(
                PATH_DIRECTORY_TEST + "^.*SM_SLC__1S.*$"));
        authorizedOutputs.add(TestUtils.buildL1ProductOutputDto(
                PATH_DIRECTORY_TEST + "^.*IW_SLC__1S.*$"));
        authorizedOutputs.add(TestUtils
                .buildL1AcnOutputDto(PATH_DIRECTORY_TEST + "^.*SM_SLC__1A.*$"));
        authorizedOutputs.add(TestUtils
                .buildL1AcnOutputDto(PATH_DIRECTORY_TEST + "^.*IW_SLC__1A.*$"));
        authorizedOutputs.add(TestUtils
                .buildL1AcnOutputDto(PATH_DIRECTORY_TEST + "^.*EW_SLC__1A.*$"));
        authorizedOutputs.add(TestUtils
                .buildL1ReportOutputDto(PATH_DIRECTORY_TEST + "^.*l1_rep.*$"));
        authorizedOutputs.add(TestUtils
                .buildSegmentReportOutputDto(PATH_DIRECTORY_TEST + "^.*l0_segment_rep.*$"));
        inputMessage.setOutputs(authorizedOutputs);

        // Outputs product
        uploadBatch = new ArrayList<>();
        uploadBatch.add(new FileObsUploadObject(ProductFamily.L0_SLICE, "o1",
                new File("o1")));
        uploadBatch.add(
                new FileObsUploadObject(ProductFamily.L1_ACN, "o2", new File("o2")));
        uploadBatch.add(
                new FileObsUploadObject(ProductFamily.L0_ACN, "o3", new File("o3")));
        outputToPublish = new ArrayList<>();
        outputToPublish
                .add(new ObsQueueMessage(ProductFamily.L0_SLICE, "p1", "o1", "FAST"));
        outputToPublish
                .add(new ObsQueueMessage(ProductFamily.L1_ACN, "p2", "o2", "FAST"));
        outputToPublish
                .add(new ObsQueueMessage(ProductFamily.L0_ACN, "p3", "o3", "FAST"));

        // Outputs report
        reportToPublish = new ArrayList<>();
        reportToPublish.add(new FileQueueMessage(ProductFamily.L0_REPORT, "p1",
                new File("p1")));
        reportToPublish.add(new FileQueueMessage(ProductFamily.L1_REPORT, "p2",
                new File("p2")));
        reportToPublish.add(new FileQueueMessage(ProductFamily.L0_REPORT, "p3",
                new File("p3")));

        doReturn(true).when(properties).isChangeIsipToSafe();
        
        commonProperties = new CommonConfigurationProperties();
		commonProperties.setRsChainName("test-chain");
		commonProperties.setRsChainVersion("0.0.1");
        
        processor =
                new OutputProcessor(obsClient, inputMessage,
                        PATH_DIRECTORY_TEST + "/outputs.list", 2, "MONITOR", ApplicationLevel.L0, properties, commonProperties);

        processorWithWildcardList =
                new OutputProcessor(obsClient, inputMessage,
                        "*.list", 2, "MONITOR", ApplicationLevel.S3_L0, properties, commonProperties);
        
        // Mocks
        doNothing().when(obsClient).upload(Mockito.any(), Mockito.any());

        try (final InputStream in = Streams.getInputStream("outputs.list");
        	 final OutputStream out = new BufferedOutputStream(new FileOutputStream(new File(tmpDir, "outputs.list")))) {
        	StreamUtils.copy(in, out);
        }        
    }
    
    @After
    public final void tearDown() throws Exception {
    	FileUtils.delete(tmpDir.getPath());
    }

    /**
     * TEst the file name extraction from the list file
     * 
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws InternalErrorException 
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testExtractFiles() throws NoSuchMethodException,
            SecurityException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, InternalErrorException {
    	
    	OutputUtils outputUtils = new OutputUtils(properties, "");
        final List<String> result = outputUtils.extractFiles(PATH_DIRECTORY_TEST + "/outputs.list", inputMessage.getWorkDirectory());
    	
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
     * TEst the file name extraction from the list file, when a wildcard is provided
     * 
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws InternalErrorException 
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testExtractFilesWithWildcard() throws NoSuchMethodException,
            SecurityException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, InternalErrorException {
        
        OutputUtils outputUtils = new OutputUtils(properties, "");
        final List<String> result = outputUtils.extractFiles("*.list", inputMessage.getWorkDirectory());
        
        
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
        
        OutputUtils outputUtils = new OutputUtils(properties, "");

        String str = outputUtils.getProductName("NRT/file.xml");
        assertEquals("file.xml", str);

        str = outputUtils.getProductName("file2.xml");
        assertEquals("file2.xml", str);

        str = outputUtils.getProductName("NRT/DIR/file2.xml");
        assertEquals("DIR/file2.xml", str);

        str = outputUtils.getProductName(
                "NRT/file2." + OutputUtils.EXT_ISIP.toLowerCase());
        assertEquals("file2." + OutputUtils.EXT_SAFE, str);
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
        final Method method = processor.getClass().getDeclaredMethod("getFilePath",
                String.class, String.class);
        method.setAccessible(true);

        String str =
                (String) method.invoke(processor, "NRT/file.xml", "file.xml");
        assertEquals(PATH_DIRECTORY_TEST + "NRT/file.xml", str);

        str = (String) method.invoke(processor, "file2.xml", "file2.xml");
        assertEquals(PATH_DIRECTORY_TEST + "file2.xml", str);

        str = (String) method.invoke(processor,
                "NRT/file2." + OutputUtils.EXT_ISIP,
                "file2." + OutputUtils.EXT_SAFE);
        assertEquals(
                PATH_DIRECTORY_TEST + "NRT/file2." + OutputUtils.EXT_ISIP
                        + File.separator + "file2." + OutputUtils.EXT_SAFE,
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
        final Method method = processor.getClass().getDeclaredMethod("getMatchOutput",
                String.class);
        method.setAccessible(true);

        LevelJobOutputDto dto = (LevelJobOutputDto) method.invoke(processor,
                "S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE");
        assertNotNull(dto);

        dto = (LevelJobOutputDto) method.invoke(processor, "report.xml");
        assertNotNull(dto);

        dto = (LevelJobOutputDto) method.invoke(processor,
                OutputProcessor.NOT_KEY_OBS);
        assertNull(dto);
    }

    /**
     * TEst sort outputs
     * @throws AbstractCodedException 
     */
    @Test
    public void testSortOutputsForL0() throws AbstractCodedException {
        final List<FileObsUploadObject> uploadBatch = new ArrayList<>();
        final List<ObsQueueMessage> outputToPublish = new ArrayList<>();
        final List<FileQueueMessage> reportToPublish = new ArrayList<>();
        final List<String> lines = new ArrayList<>();
        lines.add(
                "FAST24/S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.ISIP");
        lines.add(
                "NRT/S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.ISIP");
        lines.add(
                "FAST24/S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DC.ISIP");
        lines.add(
                "NRT/S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B2.ISIP");
        lines.add("NRT/report.xml");
        lines.add("not_match");
        lines.add("S1A_report_1.xml");
        lines.add("S1A_report_3.xml");
        lines.add("S1A_BLANK_FILE.SAFE");

        processor.sortOutputs(lines, uploadBatch, outputToPublish,
                reportToPublish, reporting);

        // Check products
        assertEquals(4, uploadBatch.size());
        assertEquals(new FileObsUploadObject(ProductFamily.L0_ACN,
                "S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE",
                new File(PATH_DIRECTORY_TEST
                        + "FAST24/S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.ISIP"
                        + File.separator
                        + "S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE")),
                uploadBatch.get(0));

        assertEquals(4, outputToPublish.size());
        assertEquals(new ObsQueueMessage(ProductFamily.L0_ACN,
                "S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE",
                "S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE", "FAST24"),
                outputToPublish.get(0));
//        assertEquals(new ObsQueueMessage(ProductFamily.L0_SLICE,
//                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
//                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE", "NRT"),
//                outputToPublish.get(1));
        assertEquals(new ObsQueueMessage(ProductFamily.L0_SEGMENT,
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DC.SAFE",
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DC.SAFE", "FAST24"),
                outputToPublish.get(2));
        assertEquals(new ObsQueueMessage(ProductFamily.L0_ACN,
                "S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B2.SAFE",
                "S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B2.SAFE", "FAST24"),
                outputToPublish.get(3));

        // Check report
        assertEquals(3, reportToPublish.size());
        assertEquals(
                new FileQueueMessage(ProductFamily.L0_REPORT,
                        "S1A_report_3.xml",
                        new File(PATH_DIRECTORY_TEST + "S1A_report_3.xml")),
                reportToPublish.get(2));
    }

    /**
     * TEst sort outputs
     * @throws AbstractCodedException 
     */
    @Test
    public void testSortOutputsForLOSegmentFast() throws AbstractCodedException {
        processor =
                new OutputProcessor(obsClient, inputMessage,
                        PATH_DIRECTORY_TEST + "outputs.list", 2, "MONITOR", ApplicationLevel.L0_SEGMENT, properties, commonProperties);
        
        final List<FileObsUploadObject> uploadBatch = new ArrayList<>();
        final List<ObsQueueMessage> outputToPublish = new ArrayList<>();
        final List<FileQueueMessage> reportToPublish = new ArrayList<>();
        final List<String> lines = new ArrayList<>();
        lines.add(
                "FAST24/S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.ISIP");
        lines.add(
                "NRT/S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.ISIP");
        lines.add(
                "FAST24/S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DC.ISIP");
        lines.add(
                "NRT/S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B2.ISIP");
        lines.add(
                "FAST24/S1A_GP_RAW__0SDV_20171213T121123_20171213T121947_019684_021735_51B2.ISIP");
        lines.add("NRT/report.xml");
        lines.add("not_match");
        lines.add("S1A_report_1.xml");
        lines.add("S1A_l0_segment_rep_3.xml");
        lines.add("S1A_BLANK_FILE.SAFE");
  
        processor.sortOutputs(lines, uploadBatch, outputToPublish,
                reportToPublish, reporting);

        System.out.print("===="+uploadBatch);
        // Check products
        assertEquals(5, uploadBatch.size());
        assertEquals(new FileObsUploadObject(ProductFamily.L0_ACN,
                "S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE",
                new File(PATH_DIRECTORY_TEST
                        + "FAST24/S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.ISIP"
                        + File.separator
                        + "S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE")),
                uploadBatch.get(0));
        assertEquals(new FileObsUploadObject(ProductFamily.L0_SLICE,
                "S1A_GP_RAW__0SDV_20171213T121123_20171213T121947_019684_021735_51B2.SAFE",
                new File(PATH_DIRECTORY_TEST
                        + "FAST24/S1A_GP_RAW__0SDV_20171213T121123_20171213T121947_019684_021735_51B2.ISIP"
                        + File.separator
                        + "S1A_GP_RAW__0SDV_20171213T121123_20171213T121947_019684_021735_51B2.SAFE")),
                uploadBatch.get(4));

        assertEquals(5, outputToPublish.size());
        assertEquals(new ObsQueueMessage(ProductFamily.L0_ACN,
                "S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE",
                "S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE", "FAST24"),
                outputToPublish.get(0));
        assertEquals(new ObsQueueMessage(ProductFamily.L0_SLICE,
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE", "FAST24"),
                outputToPublish.get(1));
        assertEquals(new ObsQueueMessage(ProductFamily.L0_SLICE,
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DC.SAFE",
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DC.SAFE", "FAST24"),
                outputToPublish.get(2));
        assertEquals(new ObsQueueMessage(ProductFamily.L0_ACN,
                "S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B2.SAFE",
                "S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B2.SAFE", "FAST24"),
                outputToPublish.get(3));
        assertEquals(new ObsQueueMessage(ProductFamily.L0_SLICE,
                "S1A_GP_RAW__0SDV_20171213T121123_20171213T121947_019684_021735_51B2.SAFE",
                "S1A_GP_RAW__0SDV_20171213T121123_20171213T121947_019684_021735_51B2.SAFE", "FAST24"),
                outputToPublish.get(4));

        // Check report
        assertEquals(3, reportToPublish.size());
        assertEquals(
                new FileQueueMessage(ProductFamily.L0_SEGMENT_REPORT,
                        "S1A_l0_segment_rep_3.xml",
                        new File(PATH_DIRECTORY_TEST + "S1A_l0_segment_rep_3.xml")),
                reportToPublish.get(2));
    }

    /**
     * TEst sort outputs
     * @throws AbstractCodedException 
     */
    @Test
    public void testSortOutputsForL1Nrt() throws AbstractCodedException {
        inputMessage.setProductProcessMode("NRT");
        processor =
                new OutputProcessor(obsClient, inputMessage,
                        PATH_DIRECTORY_TEST + "outputs.list", 2, "MONITOR", ApplicationLevel.L1, properties, commonProperties);
        
        final List<FileObsUploadObject> uploadBatch = new ArrayList<>();
        final List<ObsQueueMessage> outputToPublish = new ArrayList<>();
        final List<FileQueueMessage> reportToPublish = new ArrayList<>();
        final List<String> lines = new ArrayList<>();
        lines.add(
                "FAST24/S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.ISIP");
        lines.add(
                "NRT/S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.ISIP");
        lines.add(
                "FAST24/S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DC.ISIP");
        lines.add(
                "NRT/S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B2.ISIP");
        lines.add("NRT/report.xml");
        lines.add("not_match");
        lines.add("S1A_report_1.xml");
        lines.add("S1A_report_3.xml");
        lines.add("S1A_BLANK_FILE.SAFE");

        processor.sortOutputs(lines, uploadBatch, outputToPublish,
                reportToPublish, reporting);

        // Check products
        assertEquals(4, uploadBatch.size());
        assertEquals(new FileObsUploadObject(ProductFamily.L0_ACN,
                "S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE",
                new File(PATH_DIRECTORY_TEST
                        + "FAST24/S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.ISIP"
                        + File.separator
                        + "S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE")),
                uploadBatch.get(0));

        assertEquals(4, outputToPublish.size());
        assertEquals(new ObsQueueMessage(ProductFamily.L0_ACN,
                "S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE",
                "S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE", "NRT"),
                outputToPublish.get(0));
        assertEquals(new ObsQueueMessage(ProductFamily.L0_SLICE,
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE", "NRT"),
                outputToPublish.get(1));
        assertEquals(new ObsQueueMessage(ProductFamily.L0_SLICE,
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DC.SAFE",
                "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DC.SAFE", "NRT"),
                outputToPublish.get(2));
        assertEquals(new ObsQueueMessage(ProductFamily.L0_ACN,
                "S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B2.SAFE",
                "S1A_IW_RAW__0ADV_20171213T121123_20171213T121947_019684_021735_51B2.SAFE", "NRT"),
                outputToPublish.get(3));

        // Check report
        assertEquals(3, reportToPublish.size());
        assertEquals(
                new FileQueueMessage(ProductFamily.L0_REPORT,
                        "S1A_report_3.xml",
                        new File(PATH_DIRECTORY_TEST + "S1A_report_3.xml")),
                reportToPublish.get(2));
    }

    /**
     * TEst sort outputs
     * @throws AbstractCodedException 
     */
    @Test
    public void testSortOutputsForL1RealOutputs() throws AbstractCodedException {
        processor =
                new OutputProcessor(obsClient, inputMessage,
                        PATH_DIRECTORY_TEST + "outputs.list", 2, "MONITOR", ApplicationLevel.L1, properties, commonProperties);
        
        final List<FileObsUploadObject> uploadBatch = new ArrayList<>();
        final List<ObsQueueMessage> outputToPublish = new ArrayList<>();
        final List<FileQueueMessage> reportToPublish = new ArrayList<>();
        final List<String> lines = new ArrayList<>();
        lines.add(
                "FAST24/S1A_IW_SLC__1ADV_20171213T121123_20171213T121947_019684_021735_51B1.ISIP");
        lines.add(
                "NRT/S1A_IW_SLC__1SDV_20171213T121623_20171213T121656_019684_021735_C6DB.ISIP");
        lines.add(
                "FAST24/S1A_IW_SLC__1SDV_20171213T121623_20171213T121656_019684_021735_C6DC.ISIP");
        lines.add(
                "NRT/S1A_IW_SLC__1ADV_20171213T121123_20171213T121947_019684_021735_51B2.ISIP");
        lines.add("NRT/l1_rep.xml");
        lines.add("not_match");
        lines.add("S1A_l1_rep_1.xml");
        lines.add("S1A_l1_rep_3.xml");
        lines.add("S1A_BLANK_FILE.SAFE");

        processor.sortOutputs(lines, uploadBatch, outputToPublish,
                reportToPublish, reporting);

        // Check products
        assertEquals(4, uploadBatch.size());
        assertEquals(new FileObsUploadObject(ProductFamily.L1_ACN,
                "S1A_IW_SLC__1ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE",
                new File(PATH_DIRECTORY_TEST
                        + "FAST24/S1A_IW_SLC__1ADV_20171213T121123_20171213T121947_019684_021735_51B1.ISIP"
                        + File.separator
                        + "S1A_IW_SLC__1ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE")),
                uploadBatch.get(0));

        assertEquals(4, outputToPublish.size());
        assertEquals(new ObsQueueMessage(ProductFamily.L1_ACN,
                "S1A_IW_SLC__1ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE",
                "S1A_IW_SLC__1ADV_20171213T121123_20171213T121947_019684_021735_51B1.SAFE", "FAST24"),
                outputToPublish.get(0));
        assertEquals(new ObsQueueMessage(ProductFamily.L1_SLICE,
                "S1A_IW_SLC__1SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
                "S1A_IW_SLC__1SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE", "FAST24"),
                outputToPublish.get(1));
        assertEquals(new ObsQueueMessage(ProductFamily.L1_SLICE,
                "S1A_IW_SLC__1SDV_20171213T121623_20171213T121656_019684_021735_C6DC.SAFE",
                "S1A_IW_SLC__1SDV_20171213T121623_20171213T121656_019684_021735_C6DC.SAFE", "FAST24"),
                outputToPublish.get(2));
        assertEquals(new ObsQueueMessage(ProductFamily.L1_ACN,
                "S1A_IW_SLC__1ADV_20171213T121123_20171213T121947_019684_021735_51B2.SAFE",
                "S1A_IW_SLC__1ADV_20171213T121123_20171213T121947_019684_021735_51B2.SAFE", "FAST24"),
                outputToPublish.get(3));

        // Check report
        assertEquals(3, reportToPublish.size());
        assertEquals(
                new FileQueueMessage(ProductFamily.L1_REPORT,
                        "S1A_l1_rep_3.xml",
                        new File(PATH_DIRECTORY_TEST + "S1A_l1_rep_3.xml")),
                reportToPublish.get(2));
    }

	
	@Test
	public final void testIsPartial() throws Exception {
		final File testManifest = new File(
				getClass().getClassLoader()
					.getResource("S1A_RF_RAW__0SVH_20200120T110827_20200120T110827_030883_038B52_18C1.SAFE/manifest.safe")
					.toURI()
		);
		assertEquals(true, testManifest.exists());
		
		assertEquals(true, OutputProcessor.isPartial(testManifest.getParentFile()));		
	}
}

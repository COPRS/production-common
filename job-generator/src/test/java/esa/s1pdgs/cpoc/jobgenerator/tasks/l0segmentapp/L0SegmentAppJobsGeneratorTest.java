package esa.s1pdgs.cpoc.jobgenerator.tasks.l0segmentapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import esa.s1pdgs.cpoc.appcatalog.client.job.AbstractAppCatalogJobService;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobDto;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobProductDto;
import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.errors.processing.JobGenInputsMissingException;
import esa.s1pdgs.cpoc.common.errors.processing.JobGenMetadataException;
import esa.s1pdgs.cpoc.jobgenerator.config.JobGeneratorSettings;
import esa.s1pdgs.cpoc.jobgenerator.config.JobGeneratorSettings.WaitTempo;
import esa.s1pdgs.cpoc.jobgenerator.config.ProcessSettings;
import esa.s1pdgs.cpoc.jobgenerator.model.JobGeneration;
import esa.s1pdgs.cpoc.jobgenerator.model.joborder.JobOrder;
import esa.s1pdgs.cpoc.jobgenerator.model.joborder.JobOrderProcParam;
import esa.s1pdgs.cpoc.jobgenerator.model.metadata.LevelSegmentMetadata;
import esa.s1pdgs.cpoc.jobgenerator.model.tasktable.TaskTable;
import esa.s1pdgs.cpoc.jobgenerator.service.XmlConverter;
import esa.s1pdgs.cpoc.jobgenerator.service.metadata.MetadataService;
import esa.s1pdgs.cpoc.jobgenerator.service.mqi.OutputProducerFactory;
import esa.s1pdgs.cpoc.jobgenerator.tasks.JobsGeneratorFactory;
import esa.s1pdgs.cpoc.jobgenerator.utils.TestL0SegmentUtils;
import esa.s1pdgs.cpoc.jobgenerator.utils.TestL0Utils;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelSegmentDto;

public class L0SegmentAppJobsGeneratorTest {

    @Mock
    private XmlConverter xmlConverter;

    @Mock
    private MetadataService metadataService;

    @Mock
    private ProcessSettings l0ProcessSettings;

    @Mock
    private JobGeneratorSettings jobGeneratorSettings;

    @Mock
    private OutputProducerFactory JobsSender;

    @Mock
    private AbstractAppCatalogJobService<LevelSegmentDto> appDataService;

    private TaskTable expectedTaskTable;

    private L0SegmentAppJobsGenerator generator;

    private JobGeneration<LevelSegmentDto> job;

    @Mock
    private LevelJobDto mockJobDto;
    
    private LevelJobDto publishedJob;

    /**
     * Test set up
     * 
     * @throws Exception
     */
    @Before
    public void init() throws Exception {

        AppDataJobDto<LevelSegmentDto> appDataJob =
                TestL0SegmentUtils.buildAppData();
        job = new JobGeneration<>(appDataJob, "TaskTable.L0ASP.xml");

        // Retrieve task table from the XML converter
        // TODO replace by L0_ASP
        expectedTaskTable = TestL0Utils.buildTaskTableAIOP();

        // Mcokito
        MockitoAnnotations.initMocks(this);
        this.mockProcessSettings();
        this.mockJobGeneratorSettings();
        this.mockXmlConverter();
        this.mockMetadataService();
        this.mockKafkaSender();

        JobsGeneratorFactory factory = new JobsGeneratorFactory(
                l0ProcessSettings, jobGeneratorSettings, xmlConverter,
                metadataService, JobsSender);
        generator = (L0SegmentAppJobsGenerator) factory
                .createJobGeneratorForL0Segment(new File(
                        "./test/data/l0_segment_config/task_tables/TaskTable.AIOP.xml"),
                        appDataService);
    }

    private void mockProcessSettings() {
        Mockito.doAnswer(i -> {
            Map<String, String> r = new HashMap<String, String>(2);
            return r;
        }).when(l0ProcessSettings).getParams();
        Mockito.doAnswer(i -> {
            Map<String, String> r = new HashMap<String, String>(5);
            r.put("SM_RAW__0S", "^S1[A-B]_S[1-6]_RAW__0S.*$");
            r.put("AN_RAW__0S", "^S1[A-B]_N[1-6]_RAW__0S.*$");
            r.put("ZS_RAW__0S", "^S1[A-B]_N[1-6]_RAW__0S.*$");
            r.put("REP_L0PSA_", "^S1[A|B|_]_OPER_REP_ACQ.*$");
            r.put("REP_EFEP_", "^S1[A|B|_]_OPER_REP_PASS.*.EOF$");
            return r;
        }).when(l0ProcessSettings).getOutputregexps();
        Mockito.doAnswer(i -> {
            return ApplicationLevel.L0_SEGMENT;
        }).when(l0ProcessSettings).getLevel();
        Mockito.doAnswer(i -> {
            return "hostname";
        }).when(l0ProcessSettings).getHostname();
    }

    private void mockJobGeneratorSettings() {
        Mockito.doAnswer(i -> {
            Map<String, ProductFamily> r =
                    new HashMap<String, ProductFamily>(20);
            String families =
                    "MPL_ORBPRE:AUXILIARY_FILE||MPL_ORBSCT:AUXILIARY_FILE||AUX_OBMEMC:AUXILIARY_FILE||AUX_CAL:AUXILIARY_FILE||AUX_PP1:AUXILIARY_FILE||AUX_INS:AUXILIARY_FILE||AUX_RESORB:AUXILIARY_FILE||AUX_RES:AUXILIARY_FILE";
            if (!StringUtils.isEmpty(families)) {
                String[] paramsTmp = families.split("\\|\\|");
                for (int k = 0; k < paramsTmp.length; k++) {
                    if (!StringUtils.isEmpty(paramsTmp[k])) {
                        String[] tmp = paramsTmp[k].split(":", 2);
                        if (tmp.length == 2) {
                            r.put(tmp[0], ProductFamily.fromValue(tmp[1]));
                        }
                    }
                }
            }
            return r;
        }).when(jobGeneratorSettings).getInputfamilies();
        Mockito.doAnswer(i -> {
            Map<String, ProductFamily> r = new HashMap<>();
            r.put("", ProductFamily.L0_REPORT);
            r.put("", ProductFamily.L0_ACN);
            return r;
        }).when(jobGeneratorSettings).getOutputfamilies();
        Mockito.doAnswer(i -> {
            return "L0_PRODUCT";
        }).when(jobGeneratorSettings).getDefaultfamily();
        Mockito.doAnswer(i -> {
            return 2;
        }).when(jobGeneratorSettings).getMaxnumberofjobs();
        Mockito.doAnswer(i -> {
            return new WaitTempo(2000, 10);
        }).when(jobGeneratorSettings).getWaitprimarycheck();
        Mockito.doAnswer(i -> {
            return new WaitTempo(10000, 3);
        }).when(jobGeneratorSettings).getWaitmetadatainput();
    }

    private void mockXmlConverter() {
        try {
            Mockito.when(
                    xmlConverter.convertFromXMLToObject(Mockito.anyString()))
                    .thenReturn(expectedTaskTable);
            Mockito.when(
                    xmlConverter.convertFromObjectToXMLString(Mockito.any()))
                    .thenReturn(null);
        } catch (IOException | JAXBException e1) {
            fail("BuildTaskTableException raised: " + e1.getMessage());
        }
    }

    private void mockMetadataService() throws JobGenMetadataException {
        // One polarisation, several segments, complete
        doReturn(new LevelSegmentMetadata(
                "S1A_WV_RAW__0SSV_20180913T234452_20180913T235538_023686_011111_1BDE.SAFE",
                "WV_RAW__0S",
                "S1A_WV_RAW__0SSV_20180913T234452_20180913T235538_023686_011111_1BDE.SAFE",
                "2018-09-13T23:44:52.000000Z", "2018-09-13T23:55:38.000000Z",
                "SV", "FULL", "011111")).when(metadataService).getLevelSegment(
                        Mockito.eq(ProductFamily.L0_SEGMENT), Mockito.eq(
                                "S1A_WV_RAW__0SSV_20180913T234452_20180913T235538_023686_011111_1BDE.SAFE"));
        // One polarisation, several segments, complete
        doReturn(new LevelSegmentMetadata(
                "S1A_WV_RAW__0SSV_20180913T234452_20180913T235538_023686_0294FC_1BDE.SAFE",
                "WV_RAW__0S",
                "S1A_WV_RAW__0SSV_20180913T234452_20180913T235538_023686_0294FC_1BDE.SAFE",
                "2018-09-13T23:44:52.000000Z", "2018-09-13T23:55:38.000000Z",
                "SV", "START", "0294FC")).when(metadataService).getLevelSegment(
                        Mockito.eq(ProductFamily.L0_SEGMENT), Mockito.eq(
                                "S1A_WV_RAW__0SSV_20180913T234452_20180913T235538_023686_0294FC_1BDE.SAFE"));
        doReturn(new LevelSegmentMetadata(
                "S1A_WV_RAW__0SSV_20180913T235533_20180913T235555_023686_0294FC_1BDE.SAFE",
                "WV_RAW__0S",
                "S1A_WV_RAW__0SSV_20180913T235533_20180913T235555_023686_0294FC_1BDE.SAFE",
                "2018-09-13T23:55:33.000000Z", "2018-09-13T23:55:55.000000Z",
                "SV", "END", "0294FC")).when(metadataService).getLevelSegment(
                        Mockito.eq(ProductFamily.L0_SEGMENT), Mockito.eq(
                                "S1A_WV_RAW__0SSV_20180913T235533_20180913T235555_023686_0294FC_1BDE.SAFE"));
        // 2 polarisation, one segment, complete
        doReturn(new LevelSegmentMetadata(
                "S1A_IW_RAW__0SHV_20180913T235533_20180913T235555_023686_000001_1BDE.SAFE",
                "IW_RAW__0S",
                "S1A_IW_RAW__0SHV_20180913T235533_20180913T235555_023686_000001_1BDE.SAFE",
                "2018-09-13T23:55:33.000000Z", "2018-09-13T23:55:55.000000Z",
                "HV", "FULL", "000001")).when(metadataService).getLevelSegment(
                        Mockito.eq(ProductFamily.L0_SEGMENT), Mockito.eq(
                                "S1A_IW_RAW__0SHV_20180913T235533_20180913T235555_023686_000001_1BDE.SAFE"));
        doReturn(new LevelSegmentMetadata(
                "S1A_IW_RAW__0SHH_20180913T235533_20180913T235555_023686_000001_1BDE.SAFE",
                "IW_RAW__0S",
                "S1A_IW_RAW__0SHH_20180913T235533_20180913T235555_023686_000001_1BDE.SAFE",
                "2018-09-13T23:55:33.000000Z", "2018-09-13T23:55:55.000000Z",
                "HH", "FULL", "000001")).when(metadataService).getLevelSegment(
                        Mockito.eq(ProductFamily.L0_SEGMENT), Mockito.eq(
                                "S1A_IW_RAW__0SHH_20180913T235533_20180913T235555_023686_000001_1BDE.SAFE"));
        // 2 polarisation, only one segment, incomplete
        doReturn(new LevelSegmentMetadata(
                "S1A_IW_RAW__0SVH_20180913T235533_20180913T235555_023686_000002_1BDE.SAFE",
                "IW_RAW__0S",
                "S1A_IW_RAW__0SVH_20180913T235533_20180913T235555_023686_000002_1BDE.SAFE",
                "2018-09-13T23:55:33.000000Z", "2018-09-13T23:55:55.000000Z",
                "VH", "FULL", "000002")).when(metadataService).getLevelSegment(
                        Mockito.eq(ProductFamily.L0_SEGMENT), Mockito.eq(
                                "S1A_IW_RAW__0SVH_20180913T235533_20180913T235555_023686_000002_1BDE.SAFE"));
        // 2 polarisation, one segment, incomplete
        doReturn(new LevelSegmentMetadata(
                "S1A_IW_RAW__0SHV_20180913T235533_20180913T235555_023686_000005_1BDE.SAFE",
                "IW_RAW__0S",
                "S1A_IW_RAW__0SHV_20180913T235533_20180913T235555_023686_000005_1BDE.SAFE",
                "2019-01-21T05:43:26.195854Z", "2019-01-21T05:47:18.047569Z",
                "HV", "START", "000001")).when(metadataService).getLevelSegment(
                        Mockito.eq(ProductFamily.L0_SEGMENT), Mockito.eq(
                                "S1A_IW_RAW__0SHV_20180913T235533_20180913T235555_023686_000005_1BDE.SAFE"));
        doReturn(new LevelSegmentMetadata(
                "S1A_IW_RAW__0SHV_20180913T235650_20180913T235801_023686_000005_1BDE.SAFE",
                "IW_RAW__0S",
                "S1A_IW_RAW__0SHV_20180913T235650_20180913T235801_023686_000005_1BDE.SAFE",
                "2019-01-21T05:57:07.384773Z", "2019-01-21T05:57:26.287514Z",
                "HV", "END", "000001")).when(metadataService).getLevelSegment(
                        Mockito.eq(ProductFamily.L0_SEGMENT), Mockito.eq(
                                "S1A_IW_RAW__0SHV_20180913T235650_20180913T235801_023686_000005_1BDE.SAFE"));
        doReturn(new LevelSegmentMetadata(
                "S1A_IW_RAW__0SHH_20180913T235533_20180913T235555_023686_000005_1BDE.SAFE",
                "IW_RAW__0S",
                "S1A_IW_RAW__0SHH_20180913T235533_20180913T235555_023686_000005_1BDE.SAFE",
                "2019-01-21T05:43:26.195854Z", "2019-01-21T05:47:18.047569Z",
                "HH", "START", "000001")).when(metadataService).getLevelSegment(
                        Mockito.eq(ProductFamily.L0_SEGMENT), Mockito.eq(
                                "S1A_IW_RAW__0SHH_20180913T235533_20180913T235555_023686_000005_1BDE.SAFE"));
        doReturn(new LevelSegmentMetadata(
                "S1A_IW_RAW__0SHH_20180913T235650_20180913T235801_023686_000005_1BDE.SAFE",
                "IW_RAW__0S",
                "S1A_IW_RAW__0SHH_20180913T235650_20180913T235801_023686_000005_1BDE.SAFE",
                "2019-01-21T05:57:07.384773Z", "2019-01-21T05:57:26.287514Z",
                "HH", "END", "000001")).when(metadataService).getLevelSegment(
                        Mockito.eq(ProductFamily.L0_SEGMENT), Mockito.eq(
                                "S1A_IW_RAW__0SHH_20180913T235650_20180913T235801_023686_000005_1BDE.SAFE"));
        doReturn(new LevelSegmentMetadata(
                "S1A_IW_RAW__0SHH_20180913T235550_20180913T235555_023686_000005_1BDE.SAFE",
                "IW_RAW__0S",
                "S1A_IW_RAW__0SHH_20180913T235550_20180913T235555_023686_000005_1BDE.SAFE",
                "2019-01-21T05:47:12.277580Z", "2019-01-21T05:57:12.431404Z",
                "HH", "MIDDLE", "000001")).when(metadataService).getLevelSegment(
                        Mockito.eq(ProductFamily.L0_SEGMENT), Mockito.eq(
                                "S1A_IW_RAW__0SHH_20180913T235550_20180913T235655_023686_000005_1BDE.SAFE"));
        doReturn(new LevelSegmentMetadata(
                "S1A_IW_RAW__0SHV_20180913T235550_20180913T235655_023686_000005_1BDE.SAFE",
                "IW_RAW__0S",
                "S1A_IW_RAW__0SHV_20180913T235550_20180913T235655_023686_000005_1BDE.SAFE",
                "2019-01-21T05:47:12.277580Z", "2019-01-21T05:57:12.431404Z",
                "HV", "MIDDLE", "000001")).when(metadataService).getLevelSegment(
                        Mockito.eq(ProductFamily.L0_SEGMENT), Mockito.eq(
                                "S1A_IW_RAW__0SHV_20180913T235550_20180913T235655_023686_000005_1BDE.SAFE"));
    }

    private void mockKafkaSender() throws AbstractCodedException {
        Mockito.doAnswer(i -> {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(new File("./tmp/inputMessageL0Segment.json"),
                    i.getArgument(0));
            mapper.writeValue(new File("./tmp/jobDtoL0Segment.json"),
                    i.getArgument(1));
            publishedJob = i.getArgument(1);
            return null;
        }).when(this.JobsSender).sendJob(Mockito.any(), Mockito.any());
    }

    @Test
    public void testPresearchMissingSegmentsException()
            throws JobGenMetadataException {
        TestL0SegmentUtils.setMessageToBuildData(job.getAppDataJob(),
                Arrays.asList(
                        "S1A_WV_RAW__0SSV_20180913T234452_20180913T235538_023686_0294FC_1BDE.SAFE",
                        "S1A_WV_RAW__0SSV_20180913T235533_20180913T235555_023686_0294FC_1BDE.SAFE"));
        doThrow(new JobGenMetadataException("error occurred"))
                .when(metadataService)
                .getLevelSegment(Mockito.eq(ProductFamily.L0_SEGMENT), Mockito
                        .eq("S1A_WV_RAW__0SSV_20180913T235533_20180913T235555_023686_0294FC_1BDE.SAFE"));
        try {
            generator.preSearch(job);
        } catch (JobGenInputsMissingException e) {
            assertEquals(1, e.getMissingMetadata().size());
            assertTrue(e.getMissingMetadata().containsKey(
                    "S1A_WV_RAW__0SSV_20180913T235533_20180913T235555_023686_0294FC_1BDE.SAFE"));
            verify(metadataService, times(2)).getLevelSegment(Mockito.any(),
                    Mockito.any());
        }
    }

    @Test
    public void testPresearchMissingSegmentsNull()
            throws JobGenMetadataException {
        TestL0SegmentUtils.setMessageToBuildData(job.getAppDataJob(),
                Arrays.asList(
                        "S1A_WV_RAW__0SSV_20180913T234452_20180913T235538_023686_0294FC_1BDE.SAFE",
                        "S1A_WV_RAW__0SSV_20180913T235533_20180913T235555_023686_0294FC_1BDE.SAFE"));
        doReturn(null).when(metadataService).getLevelSegment(
                Mockito.eq(ProductFamily.L0_SEGMENT), Mockito.eq(
                        "S1A_WV_RAW__0SSV_20180913T234452_20180913T235538_023686_0294FC_1BDE.SAFE"));
        try {
            generator.preSearch(job);
        } catch (JobGenInputsMissingException e) {
            assertEquals(1, e.getMissingMetadata().size());
            assertTrue(e.getMissingMetadata().containsKey(
                    "S1A_WV_RAW__0SSV_20180913T234452_20180913T235538_023686_0294FC_1BDE.SAFE"));
            verify(metadataService, times(2)).getLevelSegment(Mockito.any(),
                    Mockito.any());
        }
    }

    @Test
    public void testPressearchNoPol() {
        TestL0SegmentUtils.setMessageToBuildData(job.getAppDataJob(),
                Arrays.asList());

        try {
            generator.preSearch(job);
        } catch (JobGenInputsMissingException e) {
            assertEquals(1, e.getMissingMetadata().size());
            assertTrue(e.getMissingMetadata()
                    .get(job.getAppDataJob().getProduct().getProductName())
                    .contains("Invalid number of polarisation 0"));
        }
    }

    @Test
    public void testPressearchTooMuchPol() {
        TestL0SegmentUtils.setMessageToBuildData(job.getAppDataJob(),
                Arrays.asList(
                        "S1A_WV_RAW__0SSV_20180913T234452_20180913T235538_023686_0294FC_1BDE.SAFE",
                        "S1A_IW_RAW__0SHV_20180913T235533_20180913T235555_023686_000001_1BDE.SAFE",
                        "S1A_IW_RAW__0SHH_20180913T235533_20180913T235555_023686_000001_1BDE.SAFE"));

        try {
            generator.preSearch(job);
        } catch (JobGenInputsMissingException e) {
            assertEquals(1, e.getMissingMetadata().size());
            assertTrue(e.getMissingMetadata()
                    .get(job.getAppDataJob().getProduct().getProductName())
                    .contains("Invalid number of polarisation 3"));
        }
    }

    @Test
    public void testPresearchOkOnePolSeveralSegment()
            throws JobGenInputsMissingException, InternalErrorException {
        TestL0SegmentUtils.setMessageToBuildData(job.getAppDataJob(),
                Arrays.asList(
                        "S1A_WV_RAW__0SSV_20180913T234452_20180913T235538_023686_0294FC_1BDE.SAFE",
                        "S1A_WV_RAW__0SSV_20180913T235533_20180913T235555_023686_0294FC_1BDE.SAFE"));
        generator.preSearch(job);
        assertEquals("2018-09-13T23:44:52.000000Z",
                job.getAppDataJob().getProduct().getStartTime());
        assertEquals("2018-09-13T23:55:55.000000Z",
                job.getAppDataJob().getProduct().getStopTime());
    }

    @Test
    public void testPresearchOkOnePolFullSegment()
            throws JobGenInputsMissingException, InternalErrorException {
        TestL0SegmentUtils.setMessageToBuildData(job.getAppDataJob(), Arrays
                .asList("S1A_WV_RAW__0SSV_20180913T234452_20180913T235538_023686_011111_1BDE.SAFE"));
        generator.preSearch(job);
        assertEquals("2018-09-13T23:44:52.000000Z",
                job.getAppDataJob().getProduct().getStartTime());
        assertEquals("2018-09-13T23:55:38.000000Z",
                job.getAppDataJob().getProduct().getStopTime());
    }

    @Test
    public void testPresearchOnePolNotFull()
            throws JobGenInputsMissingException, InternalErrorException {
        TestL0SegmentUtils.setMessageToBuildData(job.getAppDataJob(), Arrays
                .asList("S1A_WV_RAW__0SSV_20180913T234452_20180913T235538_023686_0294FC_1BDE.SAFE"));
        try {
            generator.preSearch(job);
        } catch (JobGenInputsMissingException e) {
            assertEquals(1, e.getMissingMetadata().size());
            assertTrue(e.getMissingMetadata()
                    .get(job.getAppDataJob().getProduct().getProductName())
                    .contains(
                            "Missing segments for the coverage of polarisation SV"));
        }
    }

    @Test
    public void testPresearchOnePolNotFullTimeout()
            throws JobGenInputsMissingException, InternalErrorException {
        TestL0SegmentUtils.setMessageToBuildData(job.getAppDataJob(), Arrays
                .asList("S1A_WV_RAW__0SSV_20180913T234452_20180913T235538_023686_0294FC_1BDE.SAFE"));
        job.getGeneration()
                .setCreationDate(new Date(System.currentTimeMillis() - 10010));
        generator.preSearch(job);
        assertEquals("2018-09-13T23:44:52.000000Z",
                job.getAppDataJob().getProduct().getStartTime());
        assertEquals("2018-09-13T23:55:38.000000Z",
                job.getAppDataJob().getProduct().getStopTime());
    }

    @Test
    public void testPresearchDualPolSegmentOnePol()
            throws JobGenInputsMissingException, InternalErrorException {
        TestL0SegmentUtils.setMessageToBuildData(job.getAppDataJob(), Arrays
                .asList("S1A_IW_RAW__0SVH_20180913T235533_20180913T235555_023686_000002_1BDE.SAFE"));
        try {
            generator.preSearch(job);
            fail("The vocerage shall be not checked");
        } catch (JobGenInputsMissingException e) {
            assertEquals(1, e.getMissingMetadata().size());
            assertTrue(e.getMissingMetadata()
                    .get(job.getAppDataJob().getProduct().getProductName())
                    .contains("Missing the other polarisation of VH"));
        }
    }

    @Test
    public void testPresearchDualPolSegmentInvalidPol()
            throws JobGenInputsMissingException, InternalErrorException {
        TestL0SegmentUtils.setMessageToBuildData(job.getAppDataJob(),
                Arrays.asList(
                        "S1A_IW_RAW__0SVH_20180913T235533_20180913T235555_023686_000002_1BDE.SAFE",
                        "S1A_WV_RAW__0SSV_20180913T234452_20180913T235538_023686_011111_1BDE.SAFE"));
        try {
            generator.preSearch(job);
            fail("The vocerage shall be not checked");
        } catch (JobGenInputsMissingException e) {
            assertEquals(1, e.getMissingMetadata().size());
            assertTrue(e.getMissingMetadata()
                    .get(job.getAppDataJob().getProduct().getProductName())
                    .contains("Invalid double polarisation VH - SV"));
        }
    }

    @Test
    public void testPresearchTwoPolSegmentIncomplete()
            throws JobGenInputsMissingException, InternalErrorException {
        TestL0SegmentUtils.setMessageToBuildData(job.getAppDataJob(),
                Arrays.asList(
                        "S1A_IW_RAW__0SHV_20180913T235533_20180913T235555_023686_000005_1BDE.SAFE",
                        "S1A_IW_RAW__0SHH_20180913T235650_20180913T235801_023686_000005_1BDE.SAFE"));
        try {
            generator.preSearch(job);
            fail("The vocerage shall be not checked");
        } catch (JobGenInputsMissingException e) {
            assertEquals(1, e.getMissingMetadata().size());
            assertTrue(e.getMissingMetadata()
                    .get(job.getAppDataJob().getProduct().getProductName())
                    .contains(
                            "Missing segments for the coverage of polarisation"));
        }
    }

    @Test
    public void testPresearchTwoPolSegmentIncompletePol1()
            throws JobGenInputsMissingException, InternalErrorException {
        TestL0SegmentUtils.setMessageToBuildData(job.getAppDataJob(),
                Arrays.asList(
                        "S1A_IW_RAW__0SHV_20180913T235533_20180913T235555_023686_000005_1BDE.SAFE",
                        "S1A_IW_RAW__0SHH_20180913T235650_20180913T235801_023686_000005_1BDE.SAFE",
                        "S1A_IW_RAW__0SHH_20180913T235533_20180913T235555_023686_000005_1BDE.SAFE",
                        "S1A_IW_RAW__0SHH_20180913T235550_20180913T235655_023686_000005_1BDE.SAFE"));
        try {
            generator.preSearch(job);
            fail("The vocerage shall be not checked");
        } catch (JobGenInputsMissingException e) {
            assertEquals(1, e.getMissingMetadata().size());
            assertTrue(e.getMissingMetadata()
                    .get(job.getAppDataJob().getProduct().getProductName())
                    .contains(
                            "Missing segments for the coverage of polarisation HV"));
        }
    }

    @Test
    public void testPresearchTwoPolSegmentIncompletePol2()
            throws JobGenInputsMissingException, InternalErrorException {
        TestL0SegmentUtils.setMessageToBuildData(job.getAppDataJob(),
                Arrays.asList(
                        "S1A_IW_RAW__0SHV_20180913T235533_20180913T235555_023686_000005_1BDE.SAFE",
                        "S1A_IW_RAW__0SHH_20180913T235650_20180913T235801_023686_000005_1BDE.SAFE",
                        "S1A_IW_RAW__0SHV_20180913T235650_20180913T235801_023686_000005_1BDE.SAFE",
                        "S1A_IW_RAW__0SHV_20180913T235550_20180913T235655_023686_000005_1BDE.SAFE"));
        try {
            generator.preSearch(job);
            fail("The vocerage shall be not checked");
        } catch (JobGenInputsMissingException e) {
            assertEquals(1, e.getMissingMetadata().size());
            assertTrue(e.getMissingMetadata()
                    .get(job.getAppDataJob().getProduct().getProductName())
                    .contains(
                            "Missing segments for the coverage of polarisation HH"));
        }
    }

    @Test
    public void testPresearchOkTwoPolFullSegment()
            throws JobGenInputsMissingException, InternalErrorException {
        TestL0SegmentUtils.setMessageToBuildData(job.getAppDataJob(),
                Arrays.asList(
                        "S1A_IW_RAW__0SHV_20180913T235533_20180913T235555_023686_000001_1BDE.SAFE",
                        "S1A_IW_RAW__0SHH_20180913T235533_20180913T235555_023686_000001_1BDE.SAFE"));
        generator.preSearch(job);
        assertEquals("2018-09-13T23:55:33.000000Z",
                job.getAppDataJob().getProduct().getStartTime());
        assertEquals("2018-09-13T23:55:55.000000Z",
                job.getAppDataJob().getProduct().getStopTime());
    }

    @Test
    public void testPresearchOkTwoPolSeveralSegment()
            throws JobGenInputsMissingException, InternalErrorException {
        TestL0SegmentUtils.setMessageToBuildData(job.getAppDataJob(),
                Arrays.asList(
                        "S1A_IW_RAW__0SHV_20180913T235533_20180913T235555_023686_000005_1BDE.SAFE",
                        "S1A_IW_RAW__0SHH_20180913T235650_20180913T235801_023686_000005_1BDE.SAFE",
                        "S1A_IW_RAW__0SHH_20180913T235533_20180913T235555_023686_000005_1BDE.SAFE",
                        "S1A_IW_RAW__0SHV_20180913T235650_20180913T235801_023686_000005_1BDE.SAFE",
                        "S1A_IW_RAW__0SHV_20180913T235550_20180913T235655_023686_000005_1BDE.SAFE",
                        "S1A_IW_RAW__0SHH_20180913T235550_20180913T235655_023686_000005_1BDE.SAFE"));
        generator.preSearch(job);
        assertEquals("2019-01-21T05:43:26.195854Z",
                job.getAppDataJob().getProduct().getStartTime());
        assertEquals("2019-01-21T05:57:26.287514Z",
                job.getAppDataJob().getProduct().getStopTime());
    }

    @Test
    public void testCustomJobDto() {
        AppDataJobDto<LevelSegmentDto> appDataJob =
                TestL0SegmentUtils.buildAppData();
        JobGeneration<LevelSegmentDto> job =
                new JobGeneration<>(appDataJob, "TaskTable.L0ASP.xml");

        generator.customJobDto(job, mockJobDto);
        verifyZeroInteractions(mockJobDto);
    }

    // TODO replace by L0 ASP
    @Test
    public void testCustomJobOrder() {
        JobOrder jobOrder = TestL0Utils.buildJobOrderL20171109175634707000125();
        AppDataJobDto<LevelSegmentDto> appDataJob =
                TestL0SegmentUtils.buildAppData();
        appDataJob.getProduct().setSatelliteId("B");
        appDataJob.getProduct().setMissionId("S1");
        JobGeneration<LevelSegmentDto> job =
                new JobGeneration<>(appDataJob, "TaskTable.L0ASP.xml");
        job.setJobOrder(jobOrder);

        for (JobOrderProcParam param : jobOrder.getConf().getProcParams()) {
            if (param.getName().equals("Mision_Id")) {
                assertEquals("S1A",
                        jobOrder.getConf().getProcParams().get(0).getValue());
            }
        }
        generator.customJobOrder(job);
        for (JobOrderProcParam param : jobOrder.getConf().getProcParams()) {
            if (param.getName().equals("Mision_Id")) {
                assertEquals("S1B",
                        jobOrder.getConf().getProcParams().get(0).getValue());
            }
        }
    }

    @Test
    public void testSortSegmentPerStartDate() {
        LevelSegmentMetadata obj1 = new LevelSegmentMetadata("name1",
                "IW_RAW_0S", "key1", "2018-10-12T12:55:00.000000Z",
                "2018-10-12T18:55:00.000000Z", "SH", "FULL", "14256");
        LevelSegmentMetadata obj2 = new LevelSegmentMetadata("name1",
                "IW_RAW_0S", "key1", "2018-10-12T13:12:04.000000Z",
                "2018-10-12T16:55:00.000000Z", "SH", "FULL", "14256");
        LevelSegmentMetadata obj3 = new LevelSegmentMetadata("name1",
                "IW_RAW_0S", "key1", "2018-10-12T13:12:04.000001Z",
                "2018-10-12T18:55:00.000000Z", "SH", "FULL", "14256");
        LevelSegmentMetadata obj4 = new LevelSegmentMetadata("name1",
                "IW_RAW_0S", "key1", "2018-10-05T12:55:00.000000Z",
                "2018-10-12T18:55:00.000000Z", "SH", "FULL", "14256");
        LevelSegmentMetadata obj5 = new LevelSegmentMetadata("name1",
                "IW_RAW_0S", "key1", "2018-11-12T12:55:00.000000Z",
                "2018-10-12T18:55:00.000000Z", "SH", "FULL", "14256");

        List<LevelSegmentMetadata> expectedList =
                Arrays.asList(obj4, obj1, obj2, obj3, obj5);

        List<LevelSegmentMetadata> input =
                Arrays.asList(obj1, obj2, obj3, obj4, obj5);
        List<LevelSegmentMetadata> input2 =
                Arrays.asList(obj3, obj5, obj1, obj4, obj2);
        List<LevelSegmentMetadata> input3 =
                Arrays.asList(obj4, obj1, obj2, obj3, obj5);

        assertNotEquals(expectedList, input);

        generator.sortSegmentsPerStartDate(input);
        generator.sortSegmentsPerStartDate(input2);
        generator.sortSegmentsPerStartDate(input3);

        assertEquals(expectedList, input);
        assertEquals(expectedList, input2);
        assertEquals(expectedList, input3);
    }

    @Test
    public void testIsSinglePolarisation() {
        assertTrue(generator.isSinglePolarisation("SH"));
        assertTrue(generator.isSinglePolarisation("SV"));
        assertFalse(generator.isSinglePolarisation("VV"));
        assertFalse(generator.isSinglePolarisation("VH"));
        assertFalse(generator.isSinglePolarisation("HH"));
        assertFalse(generator.isSinglePolarisation("HV"));
    }

    @Test
    public void testIsDoublePolarisation() {
        assertTrue(generator.isDoublePolarisation("HH", "HV"));
        assertTrue(generator.isDoublePolarisation("HV", "HH"));
        assertTrue(generator.isDoublePolarisation("VH", "VV"));
        assertTrue(generator.isDoublePolarisation("VV", "VH"));
        assertFalse(generator.isDoublePolarisation("VV", "HV"));
        assertFalse(generator.isDoublePolarisation("HH", "VV"));
        assertFalse(generator.isDoublePolarisation("HH", "SH"));
        assertFalse(generator.isDoublePolarisation("SH", "SV"));
        assertFalse(generator.isDoublePolarisation("SH", ""));
        assertFalse(generator.isDoublePolarisation("SH", null));
    }

    @Test
    public void testGetSensingTime() throws InternalErrorException {
        LevelSegmentMetadata obj1 = new LevelSegmentMetadata("name1",
                "IW_RAW_0S", "key1", "2018-10-12T12:55:00.111111Z",
                "2018-10-12T18:55:00.000000Z", "SH", "FULL", "14256");
        LevelSegmentMetadata obj2 = new LevelSegmentMetadata("name1",
                "IW_RAW_0S", "key1", "2018-10-12T13:12:04.222222Z",
                "2018-10-12T16:55:00.000000Z", "SH", "FULL", "14256");
        LevelSegmentMetadata obj3 = new LevelSegmentMetadata("name1",
                "IW_RAW_0S", "key1", "2018-10-12T13:12:05.999999Z",
                "2018-10-12T18:58:06.985621Z", "SH", "FULL", "14256");
        LevelSegmentMetadata obj4 = new LevelSegmentMetadata("name1",
                "IW_RAW_0S", "key1", "2018-10-05T12:55:00.000000Z",
                "2018-10-12T18:55:00.124356Z", "SH", "FULL", "14256");

        List<LevelSegmentMetadata> listSortedFull =
                Arrays.asList(obj4, obj1, obj2, obj3);
        List<LevelSegmentMetadata> listSortedOne = Arrays.asList(obj4);

        assertEquals("2018-10-05T12:55:00.000000Z",
                generator.getStartSensingDate(listSortedFull,
                        AppDataJobProductDto.TIME_FORMATTER));
        assertEquals("20181005_125500_000000",
                generator.getStartSensingDate(listSortedFull,
                        DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSSSSS")));
        assertEquals("2018-10-12T18:58:06.985621Z",
                generator.getStopSensingDate(listSortedFull,
                        AppDataJobProductDto.TIME_FORMATTER));

        assertEquals("2018-10-05T12:55:00.000000Z",
                generator.getStartSensingDate(listSortedFull,
                        AppDataJobProductDto.TIME_FORMATTER));
        assertEquals("2018-10-12T18:55:00.124356Z",
                generator.getStopSensingDate(listSortedOne,
                        AppDataJobProductDto.TIME_FORMATTER));

        assertNull(generator.getStartSensingDate(new ArrayList<>(),
                AppDataJobProductDto.TIME_FORMATTER));
        assertNull(generator.getStopSensingDate(new ArrayList<>(),
                AppDataJobProductDto.TIME_FORMATTER));

        assertNull(generator.getStartSensingDate(null,
                AppDataJobProductDto.TIME_FORMATTER));
        assertNull(generator.getStopSensingDate(null,
                AppDataJobProductDto.TIME_FORMATTER));

    }

    @Test
    public void testLeastMoreDates() {
        String date1 = "2017-10-24T12:14:02.123456Z";
        String date2 = "2017-10-24T12:14:02.183456Z";
        String date3 = "2017-10-24T12:11:02.123456Z";

        assertEquals(date1, generator.least(date1, date2,
                AppDataJobProductDto.TIME_FORMATTER));
        assertEquals(date1, generator.least(date2, date1,
                AppDataJobProductDto.TIME_FORMATTER));
        assertEquals(date3, generator.least(date1, date3,
                AppDataJobProductDto.TIME_FORMATTER));
        assertEquals(date3, generator.least(date2, date3,
                AppDataJobProductDto.TIME_FORMATTER));
        assertEquals(date1, generator.least(date1, date1,
                AppDataJobProductDto.TIME_FORMATTER));

        assertEquals(date2, generator.more(date1, date2,
                AppDataJobProductDto.TIME_FORMATTER));
        assertEquals(date2, generator.more(date2, date1,
                AppDataJobProductDto.TIME_FORMATTER));
        assertEquals(date1, generator.more(date1, date3,
                AppDataJobProductDto.TIME_FORMATTER));
        assertEquals(date2, generator.more(date2, date3,
                AppDataJobProductDto.TIME_FORMATTER));
        assertEquals(date1, generator.more(date1, date1,
                AppDataJobProductDto.TIME_FORMATTER));

    }

    @Test
    public void testExtractConsolidation() {
        LevelSegmentMetadata obj1 = new LevelSegmentMetadata("name1",
                "IW_RAW_0S", "key1", "2018-10-12T12:55:00",
                "2018-10-12T18:55:00", "SH", "FULL", "14256");
        LevelSegmentMetadata obj2 = new LevelSegmentMetadata("name1",
                "IW_RAW_0S", "key1", "2018-10-12T13:12:04",
                "2018-10-12T16:55:00", "SH", "START", "14256");
        LevelSegmentMetadata obj3 = new LevelSegmentMetadata("name1",
                "IW_RAW_0S", "key1", "2018-10-12T13:12:05",
                "2018-10-12T18:58:06", "SH", "END", "14256");

        List<LevelSegmentMetadata> listSortedFull =
                Arrays.asList(obj1, obj2, obj3);

        String expected = "FULL 2018-10-12T12:55:00 2018-10-12T18:55:00 | "
                + "START 2018-10-12T13:12:04 2018-10-12T16:55:00 | "
                + "END 2018-10-12T13:12:05 2018-10-12T18:58:06 | ";

        assertEquals(expected, generator.extractConsolidation(listSortedFull));
    }

    @Test
    public void testIsCovered() {

        assertFalse(generator.isCovered(null));
        assertFalse(generator.isCovered(new ArrayList<>()));

        LevelSegmentMetadata obj1 = new LevelSegmentMetadata("name1",
                "IW_RAW_0S", "key1", "2018-10-12T12:55:00.000000Z",
                "2018-10-12T18:55:00.000000Z", "SH", "FULL", "14256");
        LevelSegmentMetadata obj2 = new LevelSegmentMetadata("name1",
                "IW_RAW_0S", "key1", "2018-10-12T12:55:00.000000Z",
                "2018-10-12T18:55:00.000152Z", "SH", "START", "14256");
        LevelSegmentMetadata obj3 = new LevelSegmentMetadata("name1",
                "IW_RAW_0S", "key1", "2018-10-12T17:55:00.000000Z",
                "2018-10-12T18:55:01.000000Z", "SH", "PARTIAL", "14256");
        LevelSegmentMetadata obj4 = new LevelSegmentMetadata("name1",
                "IW_RAW_0S", "key1", "2018-10-12T18:55:01.000000Z",
                "2018-10-12T19:55:00.000000Z", "SH", "PARTIAL", "14256");
        LevelSegmentMetadata obj6 = new LevelSegmentMetadata("name1",
                "IW_RAW_0S", "key1", "2018-10-12T18:55:00.000152Z",
                "2018-10-12T19:53:10.000001Z", "SH", "PARTIAL", "14256");
        LevelSegmentMetadata obj5 = new LevelSegmentMetadata("name1",
                "IW_RAW_0S", "key1", "2018-10-12T19:53:10.000000Z",
                "2018-10-12T19:58:10.000000Z", "SH", "END", "14256");

        assertFalse(generator.isCovered(Arrays.asList(obj2)));
        assertFalse(generator.isCovered(Arrays.asList(obj3)));
        assertFalse(generator.isCovered(Arrays.asList(obj4)));
        assertTrue(generator.isCovered(Arrays.asList(obj1)));

        assertFalse(generator.isCovered(Arrays.asList(obj1, obj2)));
        assertFalse(generator.isCovered(Arrays.asList(obj2, obj4)));
        assertFalse(generator.isCovered(Arrays.asList(obj2, obj5)));
        assertFalse(generator.isCovered(Arrays.asList(obj2, obj3, obj5)));
        assertTrue(generator.isCovered(Arrays.asList(obj2, obj3, obj4, obj5)));
        assertTrue(generator.isCovered(Arrays.asList(obj2, obj6, obj5)));
    }

    /**@Test
    public void testRun() {
        try {

            generator.run();

            Mockito.verify(JobsSender).sendJob(Mockito.any(), Mockito.any());
            
            assertEquals(ProductFamily.L0_SEGMENT_JOB, publishedJob.getFamily());
            assertEquals("NRT", publishedJob.getProductProcessMode());
            assertEquals("S1A_IW_RAW__0SDV_20171213T142312_20171213T142344_019685_02173E_07F5.SAFE", publishedJob.getProductIdentifier());
            assertEquals(expectedTaskTable.getPools().size(), publishedJob.getPools().size());
            for (int i = 0; i < expectedTaskTable.getPools().size(); i++) {
                assertEquals(expectedTaskTable.getPools().get(i).getTasks().size(), publishedJob.getPools().get(i).getTasks().size());
                for (int j = 0; j < expectedTaskTable.getPools().get(i).getTasks().size(); j++) {
                    assertEquals(expectedTaskTable.getPools().get(i).getTasks().get(j).getFileName(), publishedJob.getPools().get(i).getTasks().get(j).getBinaryPath());
                }
            }

            // TODO to improve to check dto ok (do manually by reading the file
            // ./tmp/jobDto.txt)
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }*/

}

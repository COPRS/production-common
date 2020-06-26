package esa.s1pdgs.cpoc.ipf.preparation.worker.service;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.util.StringUtils;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerInputsMissingException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.AiopProperties;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.IpfPreparationWorkerSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.ProcessSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.generator.JobsGeneratorFactory;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.JobGeneration;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.JobOrder;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.JobOrderProcParam;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTable;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableFactory;
import esa.s1pdgs.cpoc.ipf.preparation.worker.timeout.InputTimeoutChecker;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.model.LevelSegmentMetadata;
import esa.s1pdgs.cpoc.mqi.client.MqiClient;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;

public class L0SegmentAppJobsGeneratorTest {

    private static final int primaryCheckTimeoutS = 300;
    @Mock
    ProcessConfiguration processConfiguration;
    @Mock
    AiopProperties aiopProperties;
    @Mock
    private XmlConverter xmlConverter;
    @Mock
    private MetadataClient metadataClient;
    @Mock
    private ProcessSettings l0ProcessSettings;
    @Mock
    private IpfPreparationWorkerSettings ipfPreparationWorkerSettings;
    @Mock
    private AppCatalogJobClient<CatalogEvent> appDataService;
    private TaskTable expectedTaskTable;
    private L0SegmentAppJobsGenerator generator;
    private JobGeneration job;
    @Mock
    private IpfExecutionJob mockJobDto;
    @Mock
    private MqiClient mqiClient;
    @Mock
    private Function<TaskTable, InputTimeoutChecker> timeoutCheckerFactory;

    /**
     * Test set up
     */
    @Before
    public void init() throws Exception {

        AppDataJob<CatalogEvent> appDataJob =
                TestL0SegmentUtils.buildAppData();
        job = new JobGeneration(appDataJob, "TaskTable.L0ASP.xml");

        // Retrieve task table from the XML converter
        expectedTaskTable = TestL0SegmentUtils.buildTaskTableL0ASP();

        // Mockito
        MockitoAnnotations.initMocks(this);
        mockProcessSettings();
        mockIpfPreparationWorkerSettings();
        mockXmlConverter();
        mockMetadataClient();

        JobsGeneratorFactory factory = new JobsGeneratorFactory(
                l0ProcessSettings, ipfPreparationWorkerSettings, aiopProperties,
                xmlConverter, metadataClient, processConfiguration, mqiClient, new TaskTableFactory(xmlConverter), timeoutCheckerFactory);

        generator = (L0SegmentAppJobsGenerator) factory.newJobGenerator(new File(
                "./test/data/l0_segment_config/task_tables/TaskTable.L0ASP.xml"), appDataService, JobsGeneratorFactory.JobGenType.LEVEL_SEGMENT);
    }

    private void mockProcessSettings() {
        doAnswer(i -> new HashMap<String, String>(2)).when(l0ProcessSettings).getParams();
        doAnswer(i -> {
            Map<String, String> r = new HashMap<>(5);
            r.put("SM_RAW__0S", "^S1[A-B]_S[1-6]_RAW__0S.*$");
            r.put("AN_RAW__0S", "^S1[A-B]_N[1-6]_RAW__0S.*$");
            r.put("ZS_RAW__0S", "^S1[A-B]_N[1-6]_RAW__0S.*$");
            r.put("REP_L0PSA_", "^S1[A|B|_]_OPER_REP_ACQ.*$");
            r.put("REP_EFEP_", "^S1[A|B|_]_OPER_REP_PASS.*.EOF$");
            return r;
        }).when(l0ProcessSettings).getOutputregexps();
        doAnswer(i -> ApplicationLevel.L0_SEGMENT).when(l0ProcessSettings).getLevel();
        doAnswer(i -> "hostname").when(l0ProcessSettings).getHostname();
    }

    private void mockIpfPreparationWorkerSettings() {
        doAnswer(i -> {
            Map<String, ProductFamily> r =
                    new HashMap<>(20);
            String families =
                    "MPL_ORBPRE:AUXILIARY_FILE||MPL_ORBSCT:AUXILIARY_FILE||AUX_OBMEMC:AUXILIARY_FILE||AUX_CAL:AUXILIARY_FILE||AUX_PP1:AUXILIARY_FILE||AUX_INS:AUXILIARY_FILE||AUX_RESORB:AUXILIARY_FILE||AUX_RES:AUXILIARY_FILE";
            if (!StringUtils.isEmpty(families)) {
                String[] paramsTmp = families.split("\\|\\|");
                for (String param : paramsTmp) {
                    if (!StringUtils.isEmpty(param)) {
                        String[] tmp = param.split(":", 2);
                        if (tmp.length == 2) {
                            r.put(tmp[0], ProductFamily.fromValue(tmp[1]));
                        }
                    }
                }
            }
            return r;
        }).when(ipfPreparationWorkerSettings).getInputfamilies();
        doAnswer(i -> new HashMap<String, ProductFamily>()).when(ipfPreparationWorkerSettings).getOutputfamilies();
        doAnswer(i -> "L0_ACN").when(ipfPreparationWorkerSettings).getDefaultfamily();
        doAnswer(i -> 2).when(ipfPreparationWorkerSettings).getMaxnumberofjobs();
        doAnswer(i -> new IpfPreparationWorkerSettings.WaitTempo(2000, primaryCheckTimeoutS)).when(ipfPreparationWorkerSettings).getWaitprimarycheck();
        doAnswer(i -> new IpfPreparationWorkerSettings.WaitTempo(10000, 3000)).when(ipfPreparationWorkerSettings).getWaitmetadatainput();
    }

    private void mockXmlConverter() {
        try {
            when(
                    xmlConverter.convertFromXMLToObject(anyString()))
                    .thenReturn(expectedTaskTable);
            when(
                    xmlConverter.convertFromObjectToXMLString(any()))
                    .thenReturn(null);
        } catch (IOException | JAXBException e1) {
            fail("BuildTaskTableException raised: " + e1.getMessage());
        }
    }

    private void mockMetadataClient() throws MetadataQueryException {
        // One polarisation, several segments, complete
        doReturn(new LevelSegmentMetadata(
                "S1A_WV_RAW__0SSV_20180913T234452_20180913T235538_023686_011111_1BDE.SAFE",
                "WV_RAW__0S",
                "S1A_WV_RAW__0SSV_20180913T234452_20180913T235538_023686_011111_1BDE.SAFE",
                "2018-09-13T23:44:52.000000Z", "2018-09-13T23:55:38.000000Z",
                "S1", "A", "WILE",
                "SV", "FULL", "", "011111")).when(metadataClient).getLevelSegment(
                eq(ProductFamily.L0_SEGMENT), eq(
                        "S1A_WV_RAW__0SSV_20180913T234452_20180913T235538_023686_011111_1BDE.SAFE"));
        // One polarisation, several segments, complete
        doReturn(new LevelSegmentMetadata(
                "S1A_WV_RAW__0SSV_20180913T234452_20180913T235538_023686_0294FC_1BDE.SAFE",
                "WV_RAW__0S",
                "S1A_WV_RAW__0SSV_20180913T234452_20180913T235538_023686_0294FC_1BDE.SAFE",
                "2018-09-13T23:44:52.000000Z", "2018-09-13T23:55:38.000000Z",
                "S1", "A", "WILE",
                "SV", "PARTIAL", "BEGIN", "0294FC")).when(metadataClient).getLevelSegment(
                eq(ProductFamily.L0_SEGMENT), eq(
                        "S1A_WV_RAW__0SSV_20180913T234452_20180913T235538_023686_0294FC_1BDE.SAFE"));
        doReturn(new LevelSegmentMetadata(
                "S1A_WV_RAW__0SSV_20180913T235533_20180913T235555_023686_0294FC_1BDE.SAFE",
                "WV_RAW__0S",
                "S1A_WV_RAW__0SSV_20180913T235533_20180913T235555_023686_0294FC_1BDE.SAFE",
                "2018-09-13T23:55:33.000000Z", "2018-09-13T23:55:55.000000Z",
                "S1", "A", "WILE",
                "SV", "PARTIAL", "END", "0294FC")).when(metadataClient).getLevelSegment(
                eq(ProductFamily.L0_SEGMENT), eq(
                        "S1A_WV_RAW__0SSV_20180913T235533_20180913T235555_023686_0294FC_1BDE.SAFE"));
        // 2 polarisation, one segment, complete
        doReturn(new LevelSegmentMetadata(
                "S1A_IW_RAW__0SHV_20180913T235533_20180913T235555_023686_000001_1BDE.SAFE",
                "IW_RAW__0S",
                "S1A_IW_RAW__0SHV_20180913T235533_20180913T235555_023686_000001_1BDE.SAFE",
                "2018-09-13T23:55:33.000000Z", "2018-09-13T23:55:55.000000Z",
                "S1", "A", "WILE",
                "HV", "FULL", "", "000001")).when(metadataClient).getLevelSegment(
                eq(ProductFamily.L0_SEGMENT), eq(
                        "S1A_IW_RAW__0SHV_20180913T235533_20180913T235555_023686_000001_1BDE.SAFE"));
        doReturn(new LevelSegmentMetadata(
                "S1A_IW_RAW__0SHH_20180913T235533_20180913T235555_023686_000001_1BDE.SAFE",
                "IW_RAW__0S",
                "S1A_IW_RAW__0SHH_20180913T235533_20180913T235555_023686_000001_1BDE.SAFE",
                "2018-09-13T23:55:33.000000Z", "2018-09-13T23:55:55.000000Z",
                "S1", "A", "WILE",
                "HH", "FULL", "", "000001")).when(metadataClient).getLevelSegment(
                eq(ProductFamily.L0_SEGMENT), eq(
                        "S1A_IW_RAW__0SHH_20180913T235533_20180913T235555_023686_000001_1BDE.SAFE"));
        // 2 polarisation, only one segment, incomplete
        doReturn(new LevelSegmentMetadata(
                "S1A_IW_RAW__0SVH_20180913T235533_20180913T235555_023686_000002_1BDE.SAFE",
                "IW_RAW__0S",
                "S1A_IW_RAW__0SVH_20180913T235533_20180913T235555_023686_000002_1BDE.SAFE",
                "2018-09-13T23:55:33.000000Z", "2018-09-13T23:55:55.000000Z",
                "S1", "A", "WILE",
                "VH", "FULL", "", "000002")).when(metadataClient).getLevelSegment(
                eq(ProductFamily.L0_SEGMENT), eq(
                        "S1A_IW_RAW__0SVH_20180913T235533_20180913T235555_023686_000002_1BDE.SAFE"));
        // 2 polarisation, one segment, incomplete
        doReturn(new LevelSegmentMetadata(
                "S1A_IW_RAW__0SHV_20180913T235533_20180913T235555_023686_000005_1BDE.SAFE",
                "IW_RAW__0S",
                "S1A_IW_RAW__0SHV_20180913T235533_20180913T235555_023686_000005_1BDE.SAFE",
                "2019-01-21T05:43:26.195854Z", "2019-01-21T05:47:18.047569Z",
                "S1", "A", "WILE",
                "HV", "PARTIAL", "BEGIN", "000001")).when(metadataClient).getLevelSegment(
                eq(ProductFamily.L0_SEGMENT), eq(
                        "S1A_IW_RAW__0SHV_20180913T235533_20180913T235555_023686_000005_1BDE.SAFE"));
        doReturn(new LevelSegmentMetadata(
                "S1A_IW_RAW__0SHV_20180913T235650_20180913T235801_023686_000005_1BDE.SAFE",
                "IW_RAW__0S",
                "S1A_IW_RAW__0SHV_20180913T235650_20180913T235801_023686_000005_1BDE.SAFE",
                "2019-01-21T05:57:07.384773Z", "2019-01-21T05:57:26.287514Z",
                "S1", "A", "WILE",
                "HV", "PARTIAL", "END", "000001")).when(metadataClient).getLevelSegment(
                eq(ProductFamily.L0_SEGMENT), eq(
                        "S1A_IW_RAW__0SHV_20180913T235650_20180913T235801_023686_000005_1BDE.SAFE"));
        doReturn(new LevelSegmentMetadata(
                "S1A_IW_RAW__0SHH_20180913T235533_20180913T235555_023686_000005_1BDE.SAFE",
                "IW_RAW__0S",
                "S1A_IW_RAW__0SHH_20180913T235533_20180913T235555_023686_000005_1BDE.SAFE",
                "2019-01-21T05:43:26.195854Z", "2019-01-21T05:47:18.047569Z",
                "S1", "A", "WILE",
                "HH", "PARTIAL", "BEGIN", "000001")).when(metadataClient).getLevelSegment(
                eq(ProductFamily.L0_SEGMENT), eq(
                        "S1A_IW_RAW__0SHH_20180913T235533_20180913T235555_023686_000005_1BDE.SAFE"));
        doReturn(new LevelSegmentMetadata(
                "S1A_IW_RAW__0SHH_20180913T235650_20180913T235801_023686_000005_1BDE.SAFE",
                "IW_RAW__0S",
                "S1A_IW_RAW__0SHH_20180913T235650_20180913T235801_023686_000005_1BDE.SAFE",
                "2019-01-21T05:57:07.384773Z", "2019-01-21T05:57:26.287514Z",
                "S1", "A", "WILE",
                "HH", "PARTIAL", "END", "000001")).when(metadataClient).getLevelSegment(
                eq(ProductFamily.L0_SEGMENT), eq(
                        "S1A_IW_RAW__0SHH_20180913T235650_20180913T235801_023686_000005_1BDE.SAFE"));
        doReturn(new LevelSegmentMetadata(
                "S1A_IW_RAW__0SHH_20180913T235550_20180913T235555_023686_000005_1BDE.SAFE",
                "IW_RAW__0S",
                "S1A_IW_RAW__0SHH_20180913T235550_20180913T235555_023686_000005_1BDE.SAFE",
                "2019-01-21T05:47:12.277580Z", "2019-01-21T05:57:12.431404Z",
                "S1", "A", "WILE",
                "HH", "PARTIAL", "MIDDLE", "000001")).when(metadataClient).getLevelSegment(
                eq(ProductFamily.L0_SEGMENT), eq(
                        "S1A_IW_RAW__0SHH_20180913T235550_20180913T235655_023686_000005_1BDE.SAFE"));
        doReturn(new LevelSegmentMetadata(
                "S1A_IW_RAW__0SHV_20180913T235550_20180913T235655_023686_000005_1BDE.SAFE",
                "IW_RAW__0S",
                "S1A_IW_RAW__0SHV_20180913T235550_20180913T235655_023686_000005_1BDE.SAFE",
                "2019-01-21T05:47:12.277580Z", "2019-01-21T05:57:12.431404Z",
                "S1", "A", "WILE",
                "HV", "PARTIAL", "MIDDLE", "000001")).when(metadataClient).getLevelSegment(
                eq(ProductFamily.L0_SEGMENT), eq(
                        "S1A_IW_RAW__0SHV_20180913T235550_20180913T235655_023686_000005_1BDE.SAFE"));
    }

    @Test
    public void testPresearchMissingSegmentsException() throws MetadataQueryException {
        TestL0SegmentUtils.setMessageToBuildData(job.getAppDataJob(),
                Arrays.asList(
                        "S1A_WV_RAW__0SSV_20180913T234452_20180913T235538_023686_0294FC_1BDE.SAFE",
                        "S1A_WV_RAW__0SSV_20180913T235533_20180913T235555_023686_0294FC_1BDE.SAFE"));
        doThrow(new MetadataQueryException("error occurred"))
                .when(metadataClient)
                .getLevelSegment(eq(ProductFamily.L0_SEGMENT), eq("S1A_WV_RAW__0SSV_20180913T235533_20180913T235555_023686_0294FC_1BDE.SAFE"));
        try {
            generator.preSearch(job);
        } catch (IpfPrepWorkerInputsMissingException e) {
            assertEquals(1, e.getMissingMetadata().size());
            assertTrue(e.getMissingMetadata().containsKey(
                    "S1A_WV_RAW__0SSV_20180913T235533_20180913T235555_023686_0294FC_1BDE.SAFE"));
            verify(metadataClient, times(2)).getLevelSegment(any(),
                    any());
        }
    }

    @Test
    public void testPresearchMissingSegmentsNull()
            throws MetadataQueryException {
        TestL0SegmentUtils.setMessageToBuildData(job.getAppDataJob(),
                Arrays.asList(
                        "S1A_WV_RAW__0SSV_20180913T234452_20180913T235538_023686_0294FC_1BDE.SAFE",
                        "S1A_WV_RAW__0SSV_20180913T235533_20180913T235555_023686_0294FC_1BDE.SAFE"));
        doReturn(null).when(metadataClient).getLevelSegment(
                eq(ProductFamily.L0_SEGMENT), eq(
                        "S1A_WV_RAW__0SSV_20180913T234452_20180913T235538_023686_0294FC_1BDE.SAFE"));
        try {
            generator.preSearch(job);
        } catch (IpfPrepWorkerInputsMissingException e) {
            assertEquals(1, e.getMissingMetadata().size());
            assertTrue(e.getMissingMetadata().containsKey(
                    "S1A_WV_RAW__0SSV_20180913T234452_20180913T235538_023686_0294FC_1BDE.SAFE"));
            verify(metadataClient, times(2)).getLevelSegment(any(),
                    any());
        }
    }

    @Test
    public void testPressearchNoPol() {
        TestL0SegmentUtils.setMessageToBuildData(job.getAppDataJob(),
                Collections.emptyList());

        try {
            generator.preSearch(job);
        } catch (IpfPrepWorkerInputsMissingException e) {
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
        } catch (IpfPrepWorkerInputsMissingException e) {
            assertEquals(1, e.getMissingMetadata().size());
            assertTrue(e.getMissingMetadata()
                    .get(job.getAppDataJob().getProduct().getProductName())
                    .contains("Invalid number of polarisation 3"));
        }
    }

    @Test
    public void testPresearchOkOnePolSeveralSegment()
            throws IpfPrepWorkerInputsMissingException {
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
            throws IpfPrepWorkerInputsMissingException {
        TestL0SegmentUtils.setMessageToBuildData(job.getAppDataJob(), Collections.singletonList("S1A_WV_RAW__0SSV_20180913T234452_20180913T235538_023686_011111_1BDE.SAFE"));
        generator.preSearch(job);
        assertEquals("2018-09-13T23:44:52.000000Z",
                job.getAppDataJob().getProduct().getStartTime());
        assertEquals("2018-09-13T23:55:38.000000Z",
                job.getAppDataJob().getProduct().getStopTime());
    }

    @Test
    public void testPresearchOnePolNotFull() {
        TestL0SegmentUtils.setMessageToBuildData(job.getAppDataJob(), Collections.singletonList("S1A_WV_RAW__0SSV_20180913T234452_20180913T235538_023686_0294FC_1BDE.SAFE"));
        try {
            generator.preSearch(job);
        } catch (IpfPrepWorkerInputsMissingException e) {
            assertEquals(1, e.getMissingMetadata().size());
            assertTrue(e.getMissingMetadata()
                    .get(job.getAppDataJob().getProduct().getProductName())
                    .contains(
                            "Missing segments for the coverage of polarisation SV"));
        }
    }

    @Test
    public void testPresearchOnePolNotFullTimeout() throws IpfPrepWorkerInputsMissingException {
        TestL0SegmentUtils.setMessageToBuildData(job.getAppDataJob(), Collections.singletonList("S1A_WV_RAW__0SSV_20180913T234452_20180913T235538_023686_0294FC_1BDE.SAFE"));
        job.getGeneration()
                .setCreationDate(new Date(System.currentTimeMillis() - ((primaryCheckTimeoutS + 10) * 1000)));
        generator.preSearch(job);
        assertEquals("2018-09-13T23:44:52.000000Z",
                job.getAppDataJob().getProduct().getStartTime());
        assertEquals("2018-09-13T23:55:38.000000Z",
                job.getAppDataJob().getProduct().getStopTime());
    }

    @Test
    public void testPresearchDualPolSegmentOnePol() {
        TestL0SegmentUtils.setMessageToBuildData(job.getAppDataJob(), Collections.singletonList("S1A_IW_RAW__0SVH_20180913T235533_20180913T235555_023686_000002_1BDE.SAFE"));
        try {
            generator.preSearch(job);
            fail("The coverage shall be not checked");
        } catch (IpfPrepWorkerInputsMissingException e) {
            assertEquals(1, e.getMissingMetadata().size());
            assertTrue(e.getMissingMetadata()
                    .get(job.getAppDataJob().getProduct().getProductName())
                    .contains("Missing the other polarisation of VH"));
        }
    }

    @Test
    public void testPresearchDualPolSegmentInvalidPol() {
        TestL0SegmentUtils.setMessageToBuildData(job.getAppDataJob(),
                Arrays.asList(
                        "S1A_IW_RAW__0SVH_20180913T235533_20180913T235555_023686_000002_1BDE.SAFE",
                        "S1A_WV_RAW__0SSV_20180913T234452_20180913T235538_023686_011111_1BDE.SAFE"));
        try {
            generator.preSearch(job);
            fail("The coverage shall be not checked");
        } catch (IpfPrepWorkerInputsMissingException e) {
            assertEquals(1, e.getMissingMetadata().size());
            assertTrue(e.getMissingMetadata()
                    .get(job.getAppDataJob().getProduct().getProductName())
                    .contains("Invalid double polarisation VH - SV"));
        }
    }

    @Test
    public void testPresearchTwoPolSegmentIncomplete() {
        TestL0SegmentUtils.setMessageToBuildData(job.getAppDataJob(),
                Arrays.asList(
                        "S1A_IW_RAW__0SHV_20180913T235533_20180913T235555_023686_000005_1BDE.SAFE",
                        "S1A_IW_RAW__0SHH_20180913T235650_20180913T235801_023686_000005_1BDE.SAFE"));
        try {
            generator.preSearch(job);
            fail("The coverage shall be not checked");
        } catch (IpfPrepWorkerInputsMissingException e) {
            assertEquals(1, e.getMissingMetadata().size());
            assertTrue(e.getMissingMetadata()
                    .get(job.getAppDataJob().getProduct().getProductName())
                    .contains(
                            "Missing segments for the coverage of polarisation"));
        }
    }

    @Test
    public void testPresearchTwoPolSegmentIncompletePol1() {
        TestL0SegmentUtils.setMessageToBuildData(job.getAppDataJob(),
                Arrays.asList(
                        "S1A_IW_RAW__0SHV_20180913T235533_20180913T235555_023686_000005_1BDE.SAFE",
                        "S1A_IW_RAW__0SHH_20180913T235650_20180913T235801_023686_000005_1BDE.SAFE",
                        "S1A_IW_RAW__0SHH_20180913T235533_20180913T235555_023686_000005_1BDE.SAFE",
                        "S1A_IW_RAW__0SHH_20180913T235550_20180913T235655_023686_000005_1BDE.SAFE"));
        try {
            generator.preSearch(job);
            fail("The coverage shall be not checked");
        } catch (IpfPrepWorkerInputsMissingException e) {
            assertEquals(1, e.getMissingMetadata().size());
            assertTrue(e.getMissingMetadata()
                    .get(job.getAppDataJob().getProduct().getProductName())
                    .contains(
                            "Missing segments for the coverage of polarisation HV"));
        }
    }

    @Test
    public void testPresearchTwoPolSegmentIncompletePol2() {
        TestL0SegmentUtils.setMessageToBuildData(job.getAppDataJob(),
                Arrays.asList(
                        "S1A_IW_RAW__0SHV_20180913T235533_20180913T235555_023686_000005_1BDE.SAFE",
                        "S1A_IW_RAW__0SHH_20180913T235650_20180913T235801_023686_000005_1BDE.SAFE",
                        "S1A_IW_RAW__0SHV_20180913T235650_20180913T235801_023686_000005_1BDE.SAFE",
                        "S1A_IW_RAW__0SHV_20180913T235550_20180913T235655_023686_000005_1BDE.SAFE"));
        try {
            generator.preSearch(job);
            fail("The coverage shall be not checked");
        } catch (IpfPrepWorkerInputsMissingException e) {
            assertEquals(1, e.getMissingMetadata().size());
            assertTrue(e.getMissingMetadata()
                    .get(job.getAppDataJob().getProduct().getProductName())
                    .contains(
                            "Missing segments for the coverage of polarisation HH"));
        }
    }

    @Test
    public void testPresearchOkTwoPolFullSegment() throws IpfPrepWorkerInputsMissingException {
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
    public void testPresearchOkTwoPolSeveralSegment() throws IpfPrepWorkerInputsMissingException {
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
        AppDataJob<CatalogEvent> appDataJob =
                TestL0SegmentUtils.buildAppData();
        JobGeneration job =
                new JobGeneration(appDataJob, "TaskTable.L0ASP.xml");

        generator.customJobDto(job, mockJobDto);
        verifyZeroInteractions(mockJobDto);
    }

    @Test
    public void testCustomJobOrder() {
        JobOrder jobOrder = TestL0SegmentUtils.buildJobOrderL20171109175634707000125();
        AppDataJob<CatalogEvent> appDataJob =
                TestL0SegmentUtils.buildAppData();
        appDataJob.getProduct().setSatelliteId("B");
        appDataJob.getProduct().setMissionId("S1");
        JobGeneration job =
                new JobGeneration(appDataJob, "TaskTable.L0ASP.xml");
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
                "2018-10-12T18:55:00.000000Z",
                "S1", "A", "WILE",
                "SH", "FULL", "", "14256");
        LevelSegmentMetadata obj2 = new LevelSegmentMetadata("name1",
                "IW_RAW_0S", "key1", "2018-10-12T13:12:04.000000Z",
                "2018-10-12T16:55:00.000000Z",
                "S1", "A",
                "WILE", "SH", "FULL", "", "14256");
        LevelSegmentMetadata obj3 = new LevelSegmentMetadata("name1",
                "IW_RAW_0S", "key1", "2018-10-12T13:12:04.000001Z",
                "2018-10-12T18:55:00.000000Z",
                "S1", "A", "WILE",
                "SH", "FULL", "", "14256");
        LevelSegmentMetadata obj4 = new LevelSegmentMetadata("name1",
                "IW_RAW_0S", "key1", "2018-10-05T12:55:00.000000Z",
                "2018-10-12T18:55:00.000000Z",
                "S1", "A", "WILE",
                "SH", "FULL", "", "14256");
        LevelSegmentMetadata obj5 = new LevelSegmentMetadata("name1",
                "IW_RAW_0S", "key1", "2018-11-12T12:55:00.000000Z",
                "2018-10-12T18:55:00.000000Z",
                "S1", "A", "WILE",
                "SH", "FULL", "", "14256");

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
    public void testGetSensingTime() {
        LevelSegmentMetadata obj1 = new LevelSegmentMetadata("name1",
                "IW_RAW_0S", "key1", "2018-10-12T12:55:00.111111Z",
                "2018-10-12T18:55:00.000000Z",
                "S1", "A", "WILE",
                "SH", "FULL", "", "14256");
        LevelSegmentMetadata obj2 = new LevelSegmentMetadata("name1",
                "IW_RAW_0S", "key1", "2018-10-12T13:12:04.222222Z",
                "2018-10-12T16:55:00.000000Z",
                "S1", "A", "WILE",
                "SH", "FULL", "", "14256");
        LevelSegmentMetadata obj3 = new LevelSegmentMetadata("name1",
                "IW_RAW_0S", "key1", "2018-10-12T13:12:05.999999Z",
                "2018-10-12T18:58:06.985621Z",
                "S1", "A", "WILE",
                "SH", "FULL", "", "14256");
        LevelSegmentMetadata obj4 = new LevelSegmentMetadata("name1",
                "IW_RAW_0S", "key1", "2018-10-05T12:55:00.000000Z",
                "2018-10-12T18:55:00.124356Z",
                "S1", "A", "WILE",
                "SH", "FULL", "", "14256");

        List<LevelSegmentMetadata> listSortedFull =
                Arrays.asList(obj4, obj1, obj2, obj3);
        List<LevelSegmentMetadata> listSortedOne = Collections.singletonList(obj4);

        assertEquals("2018-10-05T12:55:00.000000Z",
                generator.getStartSensingDate(listSortedFull,
                        AppDataJobProduct.TIME_FORMATTER));
        assertEquals("20181005_125500_000000",
                generator.getStartSensingDate(listSortedFull,
                        DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSSSSS")));
        assertEquals("2018-10-12T18:58:06.985621Z",
                generator.getStopSensingDate(listSortedFull,
                        AppDataJobProduct.TIME_FORMATTER));

        assertEquals("2018-10-05T12:55:00.000000Z",
                generator.getStartSensingDate(listSortedFull,
                        AppDataJobProduct.TIME_FORMATTER));
        assertEquals("2018-10-12T18:55:00.124356Z",
                generator.getStopSensingDate(listSortedOne,
                        AppDataJobProduct.TIME_FORMATTER));

        assertNull(generator.getStartSensingDate(new ArrayList<>(),
                AppDataJobProduct.TIME_FORMATTER));
        assertNull(generator.getStopSensingDate(new ArrayList<>(),
                AppDataJobProduct.TIME_FORMATTER));

        assertNull(generator.getStartSensingDate(null,
                AppDataJobProduct.TIME_FORMATTER));
        assertNull(generator.getStopSensingDate(null,
                AppDataJobProduct.TIME_FORMATTER));

    }

    @Test
    public void testLeastMoreDates() {
        String date1 = "2017-10-24T12:14:02.123456Z";
        String date2 = "2017-10-24T12:14:02.183456Z";
        String date3 = "2017-10-24T12:11:02.123456Z";

        assertEquals(date1, generator.least(date1, date2,
                AppDataJobProduct.TIME_FORMATTER));
        assertEquals(date1, generator.least(date2, date1,
                AppDataJobProduct.TIME_FORMATTER));
        assertEquals(date3, generator.least(date1, date3,
                AppDataJobProduct.TIME_FORMATTER));
        assertEquals(date3, generator.least(date2, date3,
                AppDataJobProduct.TIME_FORMATTER));
        assertEquals(date1, generator.least(date1, date1,
                AppDataJobProduct.TIME_FORMATTER));

        assertEquals(date2, generator.more(date1, date2,
                AppDataJobProduct.TIME_FORMATTER));
        assertEquals(date2, generator.more(date2, date1,
                AppDataJobProduct.TIME_FORMATTER));
        assertEquals(date1, generator.more(date1, date3,
                AppDataJobProduct.TIME_FORMATTER));
        assertEquals(date2, generator.more(date2, date3,
                AppDataJobProduct.TIME_FORMATTER));
        assertEquals(date1, generator.more(date1, date1,
                AppDataJobProduct.TIME_FORMATTER));

    }

    @Test
    public void testExtractConsolidation() {
        LevelSegmentMetadata obj1 = new LevelSegmentMetadata("name1",
                "IW_RAW_0S", "key1", "2018-10-12T12:55:00",
                "2018-10-12T18:55:00",
                "S1", "A", "WILE",
                "SH", "FULL", "", "14256");
        LevelSegmentMetadata obj2 = new LevelSegmentMetadata("name1",
                "IW_RAW_0S", "key1", "2018-10-12T13:12:04",
                "2018-10-12T16:55:00",
                "S1", "A", "WILE",
                "SH", "PARTIAL", "BEGIN", "14256");
        LevelSegmentMetadata obj3 = new LevelSegmentMetadata("name1",
                "IW_RAW_0S", "key1", "2018-10-12T13:12:05",
                "2018-10-12T18:58:06",
                "S1", "A", "WILE",
                "SH", "PARTIAL", "END", "14256");

        List<LevelSegmentMetadata> listSortedFull =
                Arrays.asList(obj1, obj2, obj3);

        String expected = " 2018-10-12T12:55:00 2018-10-12T18:55:00 | "
                + "BEGIN 2018-10-12T13:12:04 2018-10-12T16:55:00 | "
                + "END 2018-10-12T13:12:05 2018-10-12T18:58:06 | ";

        assertEquals(expected, generator.extractProductSensingConsolidation(listSortedFull));
    }

    @Test
    public void testIsCovered() {

        assertFalse(generator.isCovered(null));
        assertFalse(generator.isCovered(new ArrayList<>()));

        LevelSegmentMetadata obj1 = new LevelSegmentMetadata("name1",
                "IW_RAW_0S", "key1", "2018-10-12T12:55:00.000000Z",
                "2018-10-12T18:55:00.000000Z",
                "S1", "A", "WILE",
                "SH", "FULL", "", "14256");
        LevelSegmentMetadata obj2 = new LevelSegmentMetadata("name1",
                "IW_RAW_0S", "key1", "2018-10-12T12:55:00.000000Z",
                "2018-10-12T18:55:00.000152Z",
                "S1", "A", "WILE",
                "SH", "PARTIAL", "BEGIN", "14256");
        LevelSegmentMetadata obj3 = new LevelSegmentMetadata("name1",
                "IW_RAW_0S", "key1", "2018-10-12T17:55:00.000000Z",
                "2018-10-12T18:55:01.000000Z",
                "S1", "A", "WILE",
                "SH", "PARTIAL", "MIDDLE", "14256");
        LevelSegmentMetadata obj4 = new LevelSegmentMetadata("name1",
                "IW_RAW_0S", "key1", "2018-10-12T18:55:01.000000Z",
                "2018-10-12T19:55:00.000000Z",
                "S1", "A", "WILE",
                "SH", "PARTIAL", "MIDDLE", "14256");
        LevelSegmentMetadata obj6 = new LevelSegmentMetadata("name1",
                "IW_RAW_0S", "key1", "2018-10-12T18:55:00.000152Z",
                "2018-10-12T19:53:10.000001Z",
                "S1", "A", "WILE",
                "SH", "PARTIAL", "MIDDLE", "14256");
        LevelSegmentMetadata obj5 = new LevelSegmentMetadata("name1",
                "IW_RAW_0S", "key1", "2018-10-12T19:53:10.000000Z",
                "2018-10-12T19:58:10.000000Z",
                "S1", "A", "WILE",
                "SH", "PARTIAL", "END", "14256");

        assertFalse(generator.isCovered(Collections.singletonList(obj2)));
        assertFalse(generator.isCovered(Collections.singletonList(obj3)));
        assertFalse(generator.isCovered(Collections.singletonList(obj4)));
        assertTrue(generator.isCovered(Collections.singletonList(obj1)));

        assertFalse(generator.isCovered(Arrays.asList(obj1, obj2)));
        assertFalse(generator.isCovered(Arrays.asList(obj2, obj4)));
        assertFalse(generator.isCovered(Arrays.asList(obj2, obj5)));
        assertFalse(generator.isCovered(Arrays.asList(obj2, obj3, obj5)));
        assertTrue(generator.isCovered(Arrays.asList(obj2, obj3, obj4, obj5)));
        assertTrue(generator.isCovered(Arrays.asList(obj2, obj6, obj5)));
    }

    //@Test public void testRun() {
    //try {
    //
    //generator.run();
    //
    //Mockito.verify(JobsSender).sendJob(Mockito.any(), Mockito.any());
    //
    //assertEquals(ProductFamily.L0_SEGMENT_JOB, publishedJob.getFamily());
    //assertEquals("NRT", publishedJob.getProductProcessMode());
    //assertEquals("S1A_IW_RAW__0SDV_20171213T142312_20171213T142344_019685_02173E_07F5.SAFE", publishedJob.getProductIdentifier());
    //assertEquals(expectedTaskTable.getPools().size(), publishedJob.getPools().size());
    //for (int i = 0; i < expectedTaskTable.getPools().size(); i++) {
    //assertEquals(expectedTaskTable.getPools().get(i).getTasks().size(), publishedJob.getPools().get(i).getTasks().size());
    //for (int j = 0; j < expectedTaskTable.getPools().get(i).getTasks().size(); j++) {
    //assertEquals(expectedTaskTable.getPools().get(i).getTasks().get(j).getFileName(), publishedJob.getPools().get(i).getTasks().get(j).getBinaryPath());
    //}
    //}
    //
    //// TODO to improve to check dto ok (do manually by reading the file
    //// ./tmp/jobDto.txt)
    //} catch (Exception e) {
    //fail(e.getMessage());
    //}
    //}

}

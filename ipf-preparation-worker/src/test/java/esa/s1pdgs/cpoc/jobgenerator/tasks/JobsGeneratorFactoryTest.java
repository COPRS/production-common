package esa.s1pdgs.cpoc.jobgenerator.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
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

import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.processing.JobGenBuildTaskTableException;
import esa.s1pdgs.cpoc.jobgenerator.config.AiopProperties;
import esa.s1pdgs.cpoc.jobgenerator.config.IpfPreparationWorkerSettings;
import esa.s1pdgs.cpoc.jobgenerator.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.jobgenerator.config.ProcessSettings;
import esa.s1pdgs.cpoc.jobgenerator.model.joborder.JobOrder;
import esa.s1pdgs.cpoc.jobgenerator.model.joborder.JobOrderOutput;
import esa.s1pdgs.cpoc.jobgenerator.model.joborder.enums.JobOrderFileNameType;
import esa.s1pdgs.cpoc.jobgenerator.model.tasktable.TaskTable;
import esa.s1pdgs.cpoc.jobgenerator.model.tasktable.TaskTableOuput;
import esa.s1pdgs.cpoc.jobgenerator.model.tasktable.TaskTableTask;
import esa.s1pdgs.cpoc.jobgenerator.service.XmlConverter;
import esa.s1pdgs.cpoc.jobgenerator.service.mqi.OutputProducerFactory;
import esa.s1pdgs.cpoc.jobgenerator.utils.TestGenericUtils;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.client.SearchMetadataQuery;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;

public class JobsGeneratorFactoryTest {

    /**
     * XML converter
     */
    @Mock
    private XmlConverter xmlConverter;

    @Mock
    private MetadataClient metadataClient;

    @Mock
    private ProcessSettings l0ProcessSettings;

    @Mock
    private IpfPreparationWorkerSettings jobGeneratorSettings;

    @Mock
    private AiopProperties aiopProperties;
    
    @Mock
    private OutputProducerFactory JobsSender;

    @Mock
    private AppCatalogJobClient appDataEService;

    @Mock
    private AppCatalogJobClient appDataPService;
    
    @Mock
    private ProcessConfiguration processConfiguration;

    /**
     * Test set up
     * 
     * @throws Exception
     */
    @Before
    public void init() throws Exception {
        // Mockito
        MockitoAnnotations.initMocks(this);

        // Mock job generator settings
        Mockito.doAnswer(i -> {
            Map<String, String> r = new HashMap<String, String>(2);
            r.put("PT_Assembly", "no");
            r.put("Timeout", "60");
            return r;
        }).when(l0ProcessSettings).getParams();
        Mockito.doAnswer(i -> {
            Map<String, String> r = new HashMap<String, String>(2);
            r.put("AN_RAW__0S", "^S1[A-B]_N[1-6]_RAW__0S.*$");
            r.put("REP_EFEP_", "^S1[A|B|_]_OPER_REP_PASS.*.EOF$");
            return r;
        }).when(l0ProcessSettings).getOutputregexps();
        Mockito.doAnswer(i -> {
            Map<String, ProductFamily> r =
                    new HashMap<String, ProductFamily>(20);
            String families =
                    "SM_RAW__0S:L0_SLICE||IW_RAW__0S:L0_SLICE||EW_RAW__0S:L0_SLICE||WV_RAW__0S:L0_SLICE||RF_RAW__0S:L0_SLICE||AN_RAW__0S:L0_SLICE||EN_RAW__0S:L0_SLICE||ZS_RAW__0S:L0_SLICE||ZE_RAW__0S:L0_SLICE||ZI_RAW__0S:L0_SLICE||ZW_RAW__0S:L0_SLICE||GP_RAW__0_:BLANK||HK_RAW__0_:BLANK||REP_ACQNR:L0_REPORT||REP_L0PSA_:L0_REPORT||REP_EFEP_:L0_REPORT||IW_GRDH_1S:L1_SLICE||IW_GRDH_1A:L1_ACN";
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
        }).when(jobGeneratorSettings).getOutputfamilies();
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
    }

    @Test
    public void testCreateJobGeneratorForEdrsSession() {
        try {

            TaskTable expectedTaskTable = TestGenericUtils.buildTaskTableAIOP();
            Mockito.when(
                    xmlConverter.convertFromXMLToObject(Mockito.anyString()))
                    .thenReturn(expectedTaskTable);

            Mockito.doAnswer(i -> {
                return ApplicationLevel.L0;
            }).when(l0ProcessSettings).getLevel();

            JobsGeneratorFactory factory = new JobsGeneratorFactory(
                    l0ProcessSettings, jobGeneratorSettings, aiopProperties,
                    xmlConverter, metadataClient, JobsSender, processConfiguration);

            AbstractJobsGenerator<IngestionEvent> generator =
                    factory.createJobGeneratorForEdrsSession(new File(
                            "./test/data/generic_config/task_tables/task_tables/TaskTable.AIOP.xml"),
                            appDataEService);

            // Check Task table
            this.checkInitializeWithTaskTableAIOPTaskTable(expectedTaskTable,
                    generator.taskTable);

            // Check job order
            this.checkInitializeWithTaskTableAIOPJobOrder(expectedTaskTable,
                    generator.jobOrderTemplate);

            // Check metadata search queries
            this.checkInitializeWithTaskTableAIOPMetadataSearchQueries(
                    generator.taskTable, generator.metadataSearchQueries);

            // Check task
            this.checkInitializeWithTaskTableAIOPTasks(expectedTaskTable,
                    generator.tasks);
        } catch (JobGenBuildTaskTableException e) {
            fail("BuildTaskTableException raised: " + e.getMessage());
        } catch (IOException | JAXBException e1) {
            fail("BuildTaskTableException raised: " + e1.getMessage());
        }
    }

    private void checkInitializeWithTaskTableAIOPTaskTable(
            TaskTable expectedTaskTable, TaskTable result) {
        assertEquals(expectedTaskTable, result);
    }

    private void checkInitializeWithTaskTableAIOPJobOrder(TaskTable t,
            JobOrder o) {

        TaskTableTask task11 = t.getPools().get(0).getTasks().get(0);
        TaskTableTask task12 = t.getPools().get(0).getTasks().get(1);
        TaskTableTask task13 = t.getPools().get(0).getTasks().get(2);

        assertEquals("[JobOrder > Conf] Invalid processor name",
                t.getProcessorName(), o.getConf().getProcessorName());
        assertEquals("[JobOrder > Conf] Invalid version", t.getVersion(),
                o.getConf().getVersion());
        assertEquals("[JobOrder > ProcParam] Invalid number of DynProcParams",
                t.getDynProcParams().size(),
                o.getConf().getProcParams().size());
        assertEquals(
                "[JobOrder > ProcParam] Invalid name for 1st DynProcParams",
                t.getDynProcParams().get(0).getName(),
                o.getConf().getProcParams().get(0).getName());
        assertEquals(
                "[JobOrder > ProcParam] Invalid value for 1st DynProcParams, shall be the default one",
                t.getDynProcParams().get(0).getDefaultValue(),
                o.getConf().getProcParams().get(0).getValue());
        assertEquals(
                "[JobOrder > ProcParam] Invalid name for 2nd DynProcParams",
                "PT_Assembly", o.getConf().getProcParams().get(1).getName());
        assertEquals(
                "[JobOrder > ProcParam] Invalid value for 2nd DynProcParams, shall be the configured one",
                "no", o.getConf().getProcParams().get(1).getValue());
        assertEquals(
                "[JobOrder > ProcParam] Invalid value for 3rd DynProcParams, shall be the configured one",
                "60", o.getConf().getProcParams().get(2).getValue());
        assertEquals(
                "[JobOrder > ProcParam] Invalid value for 4th DynProcParams, shall be the default one",
                t.getDynProcParams().get(3).getDefaultValue(),
                o.getConf().getProcParams().get(3).getValue());
        assertEquals(
                "[JobOrder > ProcParam] Invalid value for 5th DynProcParams, shall be the default one",
                t.getDynProcParams().get(4).getDefaultValue(),
                o.getConf().getProcParams().get(4).getValue());
        assertEquals("[Cfg File] Invalid number", t.getCfgFiles().size(),
                o.getConf().getConfigFiles().size());
        assertEquals("[Cfg File] Invalid name",
                t.getCfgFiles().get(0).getFileName(),
                o.getConf().getConfigFiles().get(0));

        assertEquals("[Procs] Invalid number", 4, o.getProcs().size());
        assertEquals("[Procs] Invalid name", task13.getName(),
                o.getProcs().get(2).getTaskName());
        assertEquals("[Procs] Invalid name", task13.getVersion(),
                o.getProcs().get(2).getTaskVersion());
        assertNotNull("[Procs] Invalid breakpoint",
                o.getProcs().get(2).getBreakpoint());
        assertEquals("[Procs] Invalid breakpoint enable", "OFF",
                o.getProcs().get(2).getBreakpoint().getEnable());

        assertTrue("[Inputs] Invalid number",
                0 == o.getProcs().get(0).getInputs().size());

        assertTrue("[Outputs] Invalid number", task11.getOutputs().size() == o
                .getProcs().get(0).getOutputs().size());
        JobOrderOutput oOutput0 = o.getProcs().get(0).getOutputs().get(0);
        TaskTableOuput tOutput0 = task11.getOutputs().get(0);
        assertFalse("[Outputs] Invalid mandatory", oOutput0.isMandatory());
        assertTrue("[Outputs] Invalid filenametype",
                oOutput0.getFileNameType() == JobOrderFileNameType.REGEXP);
        assertEquals("[Outputs] Invalid filetype", tOutput0.getType(),
                oOutput0.getFileType());
        assertEquals("[Outputs] Invalid filename",
                "^.*" + tOutput0.getType() + ".*$", oOutput0.getFileName());

        JobOrderOutput oOutput2 = o.getProcs().get(0).getOutputs().get(2);
        TaskTableOuput tOutput2 = task11.getOutputs().get(2);
        assertFalse("[Outputs] Invalid mandatory", oOutput2.isMandatory());
        assertTrue("[Outputs] Invalid filenametype",
                oOutput2.getFileNameType() == JobOrderFileNameType.DIRECTORY);
        assertEquals("[Outputs] Invalid family", ProductFamily.L0_SLICE,
                oOutput2.getFamily());
        assertEquals("[Outputs] Invalid filetype", tOutput2.getType(),
                oOutput2.getFileType());
        assertEquals("[Outputs] Invalid filename", "", oOutput2.getFileName());

        JobOrderOutput oOutput15 = o.getProcs().get(0).getOutputs().get(15);
        TaskTableOuput tOutput15 = task11.getOutputs().get(15);
        assertFalse("[Outputs] Invalid mandatory", oOutput15.isMandatory());
        assertTrue("[Outputs] Invalid filenametype",
                oOutput15.getFileNameType() == JobOrderFileNameType.REGEXP);
        assertEquals("[Outputs] Invalid filetype", tOutput15.getType(),
                oOutput15.getFileType());
        assertEquals("[Outputs] Invalid family", ProductFamily.L0_REPORT,
                oOutput15.getFamily());
        assertEquals("[Outputs] Invalid filename",
                "^S1[A|B|_]_OPER_REP_PASS.*.EOF$", oOutput15.getFileName());

        JobOrderOutput oOutput15b = o.getProcs().get(1).getOutputs().get(5);
        TaskTableOuput tOutput15b = task12.getOutputs().get(5);
        assertFalse("[Outputs] Invalid mandatory", oOutput15b.isMandatory());
        assertTrue("[Outputs] Invalid filenametype",
                oOutput15b.getFileNameType() == JobOrderFileNameType.REGEXP);
        assertEquals("[Outputs] Invalid filetype", tOutput15b.getType(),
                oOutput15b.getFileType());
        assertEquals("[Outputs] Invalid family", ProductFamily.L0_SLICE,
                oOutput15b.getFamily());
        assertEquals("[Outputs] Invalid filename", "^S1[A-B]_N[1-6]_RAW__0S.*$",
                oOutput15b.getFileName());
    }

    private void checkInitializeWithTaskTableAIOPMetadataSearchQueries(
            TaskTable r, Map<Integer, SearchMetadataQuery> s) {

        assertTrue("Invalid number of search metadata query", s.size() == 3);

        TaskTableTask task11 = r.getPools().get(0).getTasks().get(0);
        TaskTableTask task12 = r.getPools().get(0).getTasks().get(1);

        s.forEach((k, v) -> {
            if ("MPL_ORBPRE".equals(v.getProductType())) {
                assertEquals("[smq MPL_ORBPRE] invalid delta 0", 345600,
                        v.getDeltaTime0(), 0);
                assertEquals("[smq MPL_ORBPRE] invalid delta 1", 0,
                        v.getDeltaTime1(), 0);
                assertEquals("[smq MPL_ORBPRE] invalid lastest val cover",
                        "LatestValCover", v.getRetrievalMode());
                assertEquals("[smq MPL_ORBPRE] invalid identifier",
                        k.intValue(), v.getIdentifier());
                assertTrue(
                        "[smq MPL_ORBPRE] invalid search query id for alternative task 1",
                        task11.getInputs().get(0).getAlternatives().get(0)
                                .getIdSearchMetadataQuery() == k.intValue());
                assertTrue(
                        "[smq MPL_ORBPRE] invalid search query id for alternative task 1",
                        task12.getInputs().get(0).getAlternatives().get(0)
                                .getIdSearchMetadataQuery() == k.intValue());
            } else if ("MPL_ORBSCT".equals(v.getProductType())) {
                assertEquals("[smq MPL_ORBSCT] invalid delta 0", 0,
                        v.getDeltaTime0(), 0);
                assertEquals("[smq MPL_ORBSCT] invalid delta 1", 0,
                        v.getDeltaTime1(), 0);
                assertEquals("[smq MPL_ORBSCT] invalid lastest val cover",
                        "LatestValCover", v.getRetrievalMode());
                assertEquals("[smq MPL_ORBSCT] invalid identifier",
                        k.intValue(), v.getIdentifier());
                assertTrue(
                        "[smq MPL_ORBSCT] invalid search query id for alternative task 1",
                        task11.getInputs().get(1).getAlternatives().get(0)
                                .getIdSearchMetadataQuery() == k.intValue());
                assertTrue(
                        "[smq MPL_ORBSCT] invalid search query id for alternative task 1",
                        task12.getInputs().get(1).getAlternatives().get(0)
                                .getIdSearchMetadataQuery() == k.intValue());
            } else {
                assertEquals("[smq AUX_OBMEMC] invalid type", "AUX_OBMEMC",
                        v.getProductType());
                assertEquals("[smq AUX_OBMEMC] invalid delta 0", 0,
                        v.getDeltaTime0(), 0);
                assertEquals("[smq AUX_OBMEMC] invalid delta 1", 0,
                        v.getDeltaTime1(), 0);
                assertEquals("[smq AUX_OBMEMC] invalid lastest val cover",
                        "LatestValCover", v.getRetrievalMode());
                assertEquals("[smq AUX_OBMEMC] invalid identifier",
                        k.intValue(), v.getIdentifier());
                assertTrue(
                        "[smq AUX_OBMEMC] invalid search query id for alternative task 1",
                        task11.getInputs().get(2).getAlternatives().get(0)
                                .getIdSearchMetadataQuery() == k.intValue());
                assertTrue(
                        "[smq AUX_OBMEMC] invalid search query id for alternative task 1",
                        task12.getInputs().get(2).getAlternatives().get(0)
                                .getIdSearchMetadataQuery() == k.intValue());
            }
        });
    }

    private void checkInitializeWithTaskTableAIOPTasks(TaskTable t,
            List<List<String>> tasks) {
        assertEquals("Invalid number of pools", t.getPools().size(),
                tasks.size());
        assertEquals("Invalid number of pools", 2, tasks.size());
        assertEquals("Invalid number of tasks for pool1",
                t.getPools().get(0).getTasks().size(), tasks.get(0).size());
        assertEquals("Invalid number of tasks for pool1", 3,
                tasks.get(0).size());
        assertEquals("Invalid task 1",
                t.getPools().get(0).getTasks().get(0).getFileName(),
                tasks.get(0).get(0));
        assertEquals("Invalid task 2",
                t.getPools().get(0).getTasks().get(1).getFileName(),
                tasks.get(0).get(1));
        assertEquals("Invalid task 3",
                t.getPools().get(0).getTasks().get(2).getFileName(),
                tasks.get(0).get(2));
        assertEquals("Invalid number of tasks for pool2",
                t.getPools().get(1).getTasks().size(), tasks.get(1).size());
        assertEquals("Invalid number of tasks for pool2", 1,
                tasks.get(1).size());
        assertEquals("Invalid task 4",
                t.getPools().get(1).getTasks().get(0).getFileName(),
                tasks.get(1).get(0));

    }

    @Test
    public void testCreateJobGeneratorForL0Slice() {
        try {

            Mockito.doAnswer(i -> {
                return ApplicationLevel.L1;
            }).when(l0ProcessSettings).getLevel();

            TaskTable expectedTaskTable = TestGenericUtils.buildTaskTableIW();
            Mockito.when(
                    xmlConverter.convertFromXMLToObject(Mockito.anyString()))
                    .thenReturn(expectedTaskTable);

            JobsGeneratorFactory factory = new JobsGeneratorFactory(
                    l0ProcessSettings, jobGeneratorSettings, aiopProperties,
                    xmlConverter, metadataClient, JobsSender, processConfiguration);

            AbstractJobsGenerator<ProductionEvent> generator =
                    factory.createJobGeneratorForL0Slice(new File(
                            "./test/data/generic_config/task_tables/IW_RAW__0_GRDH_1.xml"),
                    		 appDataPService);

            // Check Task table
            this.checkInitializeWithTaskTableIWTaskTable(expectedTaskTable,
                    generator.taskTable);

            // Check job order
            this.checkInitializeWithTaskTableIWJobOrder(expectedTaskTable,
                    generator.jobOrderTemplate);

            // Check metadata search queries
            this.checkInitializeWithTaskTableIWMetadataSearchQueries(
                    generator.taskTable, generator.metadataSearchQueries);

            // Check task
            this.checkInitializeWithTaskTableIWTasks(expectedTaskTable,
                    generator.tasks);
        } catch (JobGenBuildTaskTableException e) {
            fail("BuildTaskTableException raised: " + e.getMessage());
        } catch (IOException | JAXBException e1) {
            fail("BuildTaskTableException raised: " + e1.getMessage());
        }
    }

    private void checkInitializeWithTaskTableIWTaskTable(
            TaskTable expectedTaskTable, TaskTable result) {
        assertEquals(expectedTaskTable, result);
    }

    private void checkInitializeWithTaskTableIWJobOrder(TaskTable t,
            JobOrder o) {

        TaskTableTask task11 = t.getPools().get(0).getTasks().get(0);
        TaskTableTask task13 = t.getPools().get(2).getTasks().get(0);
        TaskTableTask task14 = t.getPools().get(3).getTasks().get(0);

        assertEquals("[JobOrder > Conf] Invalid processor name",
                t.getProcessorName(), o.getConf().getProcessorName());
        assertEquals("[JobOrder > Conf] Invalid version", t.getVersion(),
                o.getConf().getVersion());
        assertEquals("[JobOrder > ProcParam] Invalid number of DynProcParams",
                t.getDynProcParams().size(),
                o.getConf().getProcParams().size());
        assertEquals("[Cfg File] Invalid number", t.getCfgFiles().size(),
                o.getConf().getConfigFiles().size());

        assertEquals("[Procs] Invalid number", 5, o.getProcs().size());
        assertEquals("[Procs] Invalid name for proc3", task13.getName(),
                o.getProcs().get(2).getTaskName());
        assertEquals("[Procs] Invalid name for proc3", task13.getVersion(),
                o.getProcs().get(2).getTaskVersion());
        assertNotNull("[Procs] Invalid breakpoint for proc3",
                o.getProcs().get(2).getBreakpoint());
        assertEquals("[Procs] Invalid breakpoint enabl for proc3", "OFF",
                o.getProcs().get(2).getBreakpoint().getEnable());

        assertTrue("[Inputs] Invalid number for proc1",
                0 == o.getProcs().get(0).getInputs().size());
        assertTrue("[Outputs] Invalid number for proc1", task11.getOutputs()
                .size() == o.getProcs().get(0).getOutputs().size());

        assertTrue("[Inputs] Invalid number for proc4",
                0 == o.getProcs().get(3).getInputs().size());
        assertTrue("[Outputs] Invalid number for proc4", task14.getOutputs()
                .size() == o.getProcs().get(3).getOutputs().size());

        JobOrderOutput oOutput0 = o.getProcs().get(3).getOutputs().get(0);
        TaskTableOuput tOutput0 = task14.getOutputs().get(0);
        assertTrue("[Outputs] Invalid mandatory", oOutput0.isMandatory());
        assertTrue("[Outputs] Invalid filenametype",
                oOutput0.getFileNameType() == JobOrderFileNameType.DIRECTORY);
        assertEquals("[Outputs] Invalid filetype", tOutput0.getType(),
                oOutput0.getFileType());
        assertEquals("[Outputs] Invalid filename", "", oOutput0.getFileName());
        assertEquals("[Outputs] Invalid family", ProductFamily.L1_SLICE,
                oOutput0.getFamily());

        JobOrderOutput oOutput1 = o.getProcs().get(3).getOutputs().get(1);
        TaskTableOuput tOutput1 = task14.getOutputs().get(1);
        assertFalse("[Outputs] Invalid mandatory", oOutput1.isMandatory());
        assertTrue("[Outputs] Invalid filenametype",
                oOutput1.getFileNameType() == JobOrderFileNameType.DIRECTORY);
        assertEquals("[Outputs] Invalid filetype", tOutput1.getType(),
                oOutput1.getFileType());
        assertEquals("[Outputs] Invalid filename", "", oOutput1.getFileName());
        assertEquals("[Outputs] Invalid family", ProductFamily.L1_ACN,
                oOutput1.getFamily());
    }

    private void checkInitializeWithTaskTableIWMetadataSearchQueries(
            TaskTable r, Map<Integer, SearchMetadataQuery> s) {

        assertTrue("Invalid number of search metadata query", s.size() == 10);

        s.forEach((k, v) -> {
            TaskTableTask task11 = r.getPools().get(0).getTasks().get(0);
            if ("AUX_POE".equals(v.getProductType())) {
                assertEquals("[smq AUX_POE] invalid delta 0", 0,
                        v.getDeltaTime0(), 0);
                assertEquals("[smq AUX_POE] invalid delta 1", 0,
                        v.getDeltaTime1(), 0);
                assertEquals("[smq AUX_POE] invalid lastest val cover",
                        "LatestValCover", v.getRetrievalMode());
                assertEquals("[smq AUX_POE] invalid identifier", k.intValue(),
                        v.getIdentifier());
                assertTrue(
                        "[smq AUX_POE] invalid search query id for alternative task 1",
                        task11.getInputs().get(10).getAlternatives().get(0)
                                .getIdSearchMetadataQuery() == k.intValue());
            } else if ("AUX_RES".equals(v.getProductType())) {
                assertEquals("[smq AUX_RES] invalid delta 0", 0,
                        v.getDeltaTime0(), 0);
                assertEquals("[smq AUX_RES] invalid delta 1", 0,
                        v.getDeltaTime1(), 0);
                assertEquals("[smq AUX_RES] invalid lastest val cover",
                        "LatestValCover", v.getRetrievalMode());
                assertEquals("[smq AUX_RES] invalid identifier", k.intValue(),
                        v.getIdentifier());
                assertTrue(
                        "[smq AUX_RES] invalid search query id for alternative task 1",
                        task11.getInputs().get(10).getAlternatives().get(1)
                                .getIdSearchMetadataQuery() == k.intValue());
            } else if ("IW_SL1__1_".equals(v.getProductType())) {
                fail("Invalid file type for input " + v.getProductType());
            }
        });
    }

    private void checkInitializeWithTaskTableIWTasks(TaskTable t,
            List<List<String>> tasks) {
        assertEquals("Invalid number of pools", t.getPools().size(),
                tasks.size());
        assertEquals("Invalid number of pools", 5, tasks.size());
        assertEquals("Invalid number of tasks for pool1",
                t.getPools().get(0).getTasks().size(), tasks.get(0).size());
        assertEquals("Invalid number of tasks for pool1", 1,
                tasks.get(0).size());
        assertEquals("Invalid task for pool1",
                t.getPools().get(0).getTasks().get(0).getFileName(),
                tasks.get(0).get(0));
        assertEquals("Invalid number of tasks for pool2",
                t.getPools().get(1).getTasks().size(), tasks.get(1).size());
        assertEquals("Invalid number of tasks for pool2", 1,
                tasks.get(1).size());
        assertEquals("Invalid task for pool1",
                t.getPools().get(1).getTasks().get(0).getFileName(),
                tasks.get(1).get(0));
        assertEquals("Invalid number of tasks for pool3",
                t.getPools().get(2).getTasks().size(), tasks.get(2).size());
        assertEquals("Invalid number of tasks for pool3", 1,
                tasks.get(2).size());
        assertEquals("Invalid task for pool3",
                t.getPools().get(2).getTasks().get(0).getFileName(),
                tasks.get(2).get(0));
        assertEquals("Invalid number of tasks for pool4",
                t.getPools().get(3).getTasks().size(), tasks.get(3).size());
        assertEquals("Invalid number of tasks for pool4", 1,
                tasks.get(3).size());
        assertEquals("Invalid task for pool4",
                t.getPools().get(3).getTasks().get(0).getFileName(),
                tasks.get(3).get(0));

    }

}

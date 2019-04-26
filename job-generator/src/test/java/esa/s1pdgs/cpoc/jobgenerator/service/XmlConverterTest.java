package esa.s1pdgs.cpoc.jobgenerator.service;

import static org.assertj.core.api.Assertions.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import javax.xml.bind.JAXBException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import esa.s1pdgs.cpoc.jobgenerator.config.AppConfig;
import esa.s1pdgs.cpoc.jobgenerator.model.EdrsSessionFile;
import esa.s1pdgs.cpoc.jobgenerator.model.joborder.JobOrder;
import esa.s1pdgs.cpoc.jobgenerator.model.l1routing.L1Routing;
import esa.s1pdgs.cpoc.jobgenerator.model.tasktable.TaskTable;
import esa.s1pdgs.cpoc.jobgenerator.model.tasktable.TaskTableInput;
import esa.s1pdgs.cpoc.jobgenerator.model.tasktable.TaskTableInputAlternative;
import esa.s1pdgs.cpoc.jobgenerator.model.tasktable.TaskTableOuput;
import esa.s1pdgs.cpoc.jobgenerator.model.tasktable.TaskTablePool;
import esa.s1pdgs.cpoc.jobgenerator.model.tasktable.TaskTableTask;
import esa.s1pdgs.cpoc.jobgenerator.utils.TestL0Utils;
import esa.s1pdgs.cpoc.jobgenerator.utils.TestL1Utils;

/**
 * Test XML conversion and mapping
 * 
 * @author Cyrielle
 */
public class XmlConverterTest {

    /**
     * Spring annotation context
     */
    private AnnotationConfigApplicationContext ctx;

    /**
     * XML converter
     */
    private XmlConverter xmlConverter;

    /**
     * Initialize the spring context
     */
    @Before
    public void init() {
        ctx = new AnnotationConfigApplicationContext();
        ctx.register(AppConfig.class);
        ctx.refresh();
        xmlConverter = ctx.getBean(XmlConverter.class);

    }

    /**
     * Close the spring context
     */
    @After
    public void close() {
        ctx.close();
    }

    /**
     * Test the conversion XML file => EDRS session object
     */
    @Test
    public void testUnmarshalingEdrsSessionFiles() {
        try {
            EdrsSessionFile fileChannel1 =
                    (EdrsSessionFile) xmlConverter.convertFromXMLToObject(
                            "./test/data/DCS_02_L20171109175634707000125_ch1_DSIB.xml");
            assertEquals("Session identifiers not equaled",
                    "L20171109175634707000125", fileChannel1.getSessionId());
            assertEquals("Start times not equaled", "2017-12-13T14:59:48Z",
                    fileChannel1.getStartTime());
            assertEquals("End times not equaled", "2017-12-13T15:17:25Z",
                    fileChannel1.getStopTime());
            assertEquals("Invalid number of raws", 35,
                    fileChannel1.getRawNames().size());
        } catch (IOException | JAXBException e) {
            fail("Exception raised", e);
        }
    }

    /**
     * Test the conversion task table XML file => task table object
     */
    @Test
    public void testUnmarshalingTaskTable() {
        try {
            TaskTable taskTable =
                    (TaskTable) xmlConverter.convertFromXMLToObject(
                            "./test/data/generic_config/task_tables/TaskTable.AIOP.xml");
            TaskTable expectedTaskTable = TestL0Utils.buildTaskTableAIOP();

            assertEquals("[TaskTable] Invalid processor name",
                    expectedTaskTable.getProcessorName(),
                    taskTable.getProcessorName());
            assertEquals("[TaskTable] Invalid version",
                    expectedTaskTable.getVersion(), taskTable.getVersion());
            assertTrue("[TaskTable] Invalid test",
                    expectedTaskTable.getTest() == taskTable.getTest());

            assertNotNull(taskTable.getCfgFiles());
            assertEquals("[CfgFiles] Invalid number",
                    expectedTaskTable.getCfgFiles().size(),
                    taskTable.getCfgFiles().size());
            assertEquals("[CfgFiles] Invalid version",
                    expectedTaskTable.getCfgFiles().get(0).getVersion(),
                    taskTable.getCfgFiles().get(0).getVersion());
            assertEquals("[CfgFiles] Invalid filename",
                    expectedTaskTable.getCfgFiles().get(0).getFileName(),
                    taskTable.getCfgFiles().get(0).getFileName());

            assertNotNull(taskTable.getDynProcParams());
            assertEquals("[DynProcName] Invalid number",
                    expectedTaskTable.getDynProcParams().size(),
                    taskTable.getDynProcParams().size());
            assertEquals("[DynProcName] Invalid version",
                    expectedTaskTable.getDynProcParams().get(0).getName(),
                    taskTable.getDynProcParams().get(0).getName());
            assertEquals("[DynProcName] Invalid filename",
                    expectedTaskTable.getDynProcParams().get(0).getType(),
                    taskTable.getDynProcParams().get(0).getType());
            assertEquals("[DynProcName] Invalid filename",
                    expectedTaskTable.getDynProcParams().get(0)
                            .getDefaultValue(),
                    taskTable.getDynProcParams().get(0).getDefaultValue());

            assertNotNull(taskTable.getPools());
            assertEquals("[Pool] Invalid number",
                    expectedTaskTable.getPools().size(),
                    taskTable.getPools().size());

            TaskTablePool pool1 = taskTable.getPools().get(0);
            TaskTablePool expectedPool1 = expectedTaskTable.getPools().get(0);
            assertTrue("[Pool1] Invalid detached",
                    expectedPool1.isDetached() == pool1.isDetached());
            assertEquals("[Pool1] Invalid killing signal",
                    expectedPool1.getKillingSignal(), pool1.getKillingSignal());

            assertNotNull(pool1.getTasks());
            assertEquals("[Task] Invalid number",
                    expectedPool1.getTasks().size(), pool1.getTasks().size());

            TaskTableTask task1 = pool1.getTasks().get(0);
            TaskTableTask expectedTask1 = expectedPool1.getTasks().get(0);
            assertEquals("[Task1] Invalid processor name",
                    expectedTask1.getName(), task1.getName());
            assertEquals("[Task1] Invalid version", expectedTask1.getVersion(),
                    task1.getVersion());
            assertEquals("[Task1] Invalid criticity level",
                    expectedTask1.getCriticityLevel(),
                    task1.getCriticityLevel());
            assertTrue("[Task1] Invalid critical",
                    expectedTask1.isCritical() == task1.isCritical());
            assertEquals("[Task1] Invalid filename",
                    expectedTask1.getFileName(), task1.getFileName());

            assertNotNull(task1.getInputs());
            assertEquals("[Input] Invalid number",
                    expectedTask1.getInputs().size(), task1.getInputs().size());

            TaskTableInput input1 = task1.getInputs().get(0);
            TaskTableInput expectedInput1 = expectedTask1.getInputs().get(0);
            assertTrue("[Input1] Invalid mode",
                    expectedInput1.getMode() == input1.getMode());
            assertTrue("[Input1] Invalid mandatory",
                    expectedInput1.getMandatory() == input1.getMandatory());

            assertNotNull(input1.getAlternatives());
            assertEquals("[Alternative] Invalid number",
                    expectedInput1.getAlternatives().size(),
                    input1.getAlternatives().size());

            TaskTableInputAlternative alt1 = input1.getAlternatives().get(0);
            TaskTableInputAlternative expectedAlt1 =
                    expectedInput1.getAlternatives().get(0);
            assertEquals("[Alternative1] Invalid order",
                    expectedAlt1.getOrder(), alt1.getOrder());
            assertEquals("[Alternative1] Invalid t0",
                    expectedAlt1.getDeltaTime0(), alt1.getDeltaTime0(), 0);
            assertEquals("[Alternative1] Invalid t1",
                    expectedAlt1.getDeltaTime1(), alt1.getDeltaTime1(), 0);
            assertTrue("[Alternative1] Invalid origin",
                    expectedAlt1.getOrigin() == alt1.getOrigin());
            assertEquals("[Alternative1] Invalid retrieval mode",
                    expectedAlt1.getRetrievalMode(), alt1.getRetrievalMode());
            assertEquals("[Alternative1] Invalid file type",
                    expectedAlt1.getFileType(), alt1.getFileType());
            assertTrue("[Alternative1] Invalid filename type",
                    expectedAlt1.getFileNameType() == alt1.getFileNameType());

            assertNotNull(task1.getOutputs());
            assertEquals("[Output] Invalid number",
                    expectedTask1.getOutputs().size(),
                    task1.getOutputs().size());

            TaskTableOuput output1 = task1.getOutputs().get(0);
            TaskTableOuput expectedOutput1 = expectedTask1.getOutputs().get(0);
            assertTrue("[Output1] Invalid destination", expectedOutput1
                    .getDestination() == output1.getDestination());
            assertTrue("[Output1] Invalid mandatory",
                    expectedOutput1.getMandatory() == output1.getMandatory());
            assertEquals("[Output1] Invalid type", expectedOutput1.getType(),
                    output1.getType());
            assertTrue("[Output1] Invalid filename type", expectedOutput1
                    .getFileNameType() == output1.getFileNameType());

        } catch (IOException | JAXBException e) {
            fail("Exception raised", e);
        }
    }

    /**
     * Test the conversion JobOrder => JobOrder.xml => JobORder
     */
    @Test
    public void testMarshallingJobOrder() {
        JobOrder job = TestL0Utils.buildJobOrderL20171109175634707000125(true);
        File file = new File("./tmp/jobOrder.xml");

        try {
            file.createNewFile();
            xmlConverter.convertFromObjectToXML(job, "./tmp/jobOrder.xml");

            JobOrder convertJobOrder = (JobOrder) xmlConverter
                    .convertFromXMLToObject("./tmp/jobOrder.xml");

            assertNotNull("Conf not null", convertJobOrder.getConf());
            assertEquals("Conf not equal", job.getConf(),
                    convertJobOrder.getConf());

            assertNotNull("Con files",
                    convertJobOrder.getConf().getConfigFiles());
            assertTrue("9 con files",
                    job.getConf().getConfigFiles().size() == convertJobOrder
                            .getConf().getConfigFiles().size());
            assertEquals("Conf not equal",
                    job.getConf().getConfigFiles().get(0),
                    convertJobOrder.getConf().getConfigFiles().get(0));

            assertNotNull("getProcParams",
                    convertJobOrder.getConf().getProcParams());
            assertTrue("getProcParams",
                    job.getConf().getProcParams().size() == convertJobOrder
                            .getConf().getProcParams().size());
            assertEquals("Conf not equal", job.getConf().getProcParams().get(0),
                    convertJobOrder.getConf().getProcParams().get(0));

            assertNotNull("getProcs", convertJobOrder.getProcs());
            assertTrue("getProcs",
                    job.getProcs().size() == convertJobOrder.getProcs().size());

            assertNotNull("getInputs",
                    convertJobOrder.getProcs().get(0).getInputs());
            assertTrue("getInputs",
                    job.getProcs().get(0).getInputs().size() == convertJobOrder
                            .getProcs().get(0).getInputs().size());
            // TODO update because object storage key is null
            // assertEquals("Conf not equal",
            // job.getProcs().get(0).getInputs().get(0),
            // convertJobOrder.getProcs().get(0).getInputs().get(0));

            assertNotNull("getOutputs",
                    convertJobOrder.getProcs().get(0).getOutputs());
            assertTrue("getOutputs",
                    job.getProcs().get(0).getOutputs().size() == convertJobOrder
                            .getProcs().get(0).getOutputs().size());
            assertEquals("Conf not equal",
                    job.getProcs().get(0).getOutputs().get(0),
                    convertJobOrder.getProcs().get(0).getOutputs().get(0));
        } catch (IOException | JAXBException e) {
            fail("Exception raised", e);
            e.printStackTrace();
        } finally {
            file.delete();
        }
    }

    @Test
    public void testMarshallingJobOrderIntoString() {
        JobOrder job = TestL0Utils.buildJobOrderL20171109175634707000125(true);
        File file = new File("./tmp/jobOrderFromString.xml");

        try {
            file.createNewFile();

            String jobString = xmlConverter.convertFromObjectToXMLString(job);
            try (PrintStream out = new PrintStream(
                    new FileOutputStream("./tmp/jobOrderFromString.xml"))) {
                out.print(jobString);
            }

            JobOrder convertJobOrder = (JobOrder) xmlConverter
                    .convertFromXMLToObject("./tmp/jobOrderFromString.xml");

            assertNotNull("Conf not null", convertJobOrder.getConf());
            assertEquals("Conf not equal", job.getConf(),
                    convertJobOrder.getConf());

            assertNotNull("Con files",
                    convertJobOrder.getConf().getConfigFiles());
            assertTrue("9 con files",
                    job.getConf().getConfigFiles().size() == convertJobOrder
                            .getConf().getConfigFiles().size());
            assertEquals("Conf not equal",
                    job.getConf().getConfigFiles().get(0),
                    convertJobOrder.getConf().getConfigFiles().get(0));

            assertNotNull("getProcParams",
                    convertJobOrder.getConf().getProcParams());
            assertTrue("getProcParams",
                    job.getConf().getProcParams().size() == convertJobOrder
                            .getConf().getProcParams().size());
            assertEquals("Conf not equal", job.getConf().getProcParams().get(0),
                    convertJobOrder.getConf().getProcParams().get(0));

            assertNotNull("getProcs", convertJobOrder.getProcs());
            assertTrue("getProcs",
                    job.getProcs().size() == convertJobOrder.getProcs().size());

            assertNotNull("getInputs",
                    convertJobOrder.getProcs().get(0).getInputs());
            assertTrue("getInputs",
                    job.getProcs().get(0).getInputs().size() == convertJobOrder
                            .getProcs().get(0).getInputs().size());
            // TODO update because object storage key is null
            // assertEquals("Conf not equal",
            // job.getProcs().get(0).getInputs().get(0),
            // convertJobOrder.getProcs().get(0).getInputs().get(0));

            assertNotNull("getOutputs",
                    convertJobOrder.getProcs().get(0).getOutputs());
            assertTrue("getOutputs",
                    job.getProcs().get(0).getOutputs().size() == convertJobOrder
                            .getProcs().get(0).getOutputs().size());
            assertEquals("Conf not equal",
                    job.getProcs().get(0).getOutputs().get(0),
                    convertJobOrder.getProcs().get(0).getOutputs().get(0));
        } catch (IOException | JAXBException e) {
            fail("Exception raised", e);
            e.printStackTrace();
        } finally {
            file.delete();
        }
    }

    @Test
    public void testUnmarshalingL1Routing() {
        try {
            L1Routing converted =
                    (L1Routing) xmlConverter.convertFromXMLToObject(
                            "./test/data/l1_config/routing.xml");
            L1Routing expected = TestL1Utils.buildL1Routing();

            assertEquals("0", expected.getRoutes().get(0),
                    converted.getRoutes().get(0));
            assertEquals("1", expected.getRoutes().get(1),
                    converted.getRoutes().get(1));
            assertEquals("2", expected.getRoutes().get(2),
                    converted.getRoutes().get(2));
            assertEquals("3", expected.getRoutes().get(3),
                    converted.getRoutes().get(3));
            assertEquals("4", expected.getRoutes().get(4),
                    converted.getRoutes().get(4));
            assertEquals("5", expected.getRoutes().get(5),
                    converted.getRoutes().get(5));
            assertEquals("6", expected.getRoutes().get(6),
                    converted.getRoutes().get(6));
            assertEquals("7", expected.getRoutes().get(7),
                    converted.getRoutes().get(7));
        } catch (IOException | JAXBException e) {
            fail("Exception raised", e);
        }
    }
}

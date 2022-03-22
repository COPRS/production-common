package esa.s1pdgs.cpoc.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import esa.s1pdgs.cpoc.xml.config.XmlConfig;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTable;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableInput;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableInputAlternative;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableOuput;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTablePool;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableTask;
import esa.s1pdgs.cpoc.xml.model.tasktable.routing.LevelProductsRoute;
import esa.s1pdgs.cpoc.xml.model.tasktable.routing.LevelProductsRouteFrom;
import esa.s1pdgs.cpoc.xml.model.tasktable.routing.LevelProductsRouteTo;
import esa.s1pdgs.cpoc.xml.model.tasktable.routing.LevelProductsRouting;

/**
 * Test XML conversion and mapping
 * 
 * @author Cyrielle
 */
public class XmlConverterTest {	
	private final XmlConverter uut = new XmlConfig().xmlConverter();
    /**
     * Test the conversion task table XML file => task table object
     * @throws JAXBException 
     * @throws IOException 
     */
    @Test
    public void testUnmarshalingTaskTable() throws IOException, JAXBException {
            final TaskTable taskTable =
                    (TaskTable) uut.convertFromXMLToObject(
                            "./test/data/generic_config/task_tables/TaskTable.AIOP.xml");
            final TaskTable expectedTaskTable = TestGenericUtils.buildTaskTableAIOP();

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

            final TaskTablePool pool1 = taskTable.getPools().get(0);
            final TaskTablePool expectedPool1 = expectedTaskTable.getPools().get(0);
            assertTrue("[Pool1] Invalid detached",
                    expectedPool1.isDetached() == pool1.isDetached());
            assertEquals("[Pool1] Invalid killing signal",
                    expectedPool1.getKillingSignal(), pool1.getKillingSignal());

            assertNotNull(pool1.getTasks());
            assertEquals("[Task] Invalid number",
                    expectedPool1.getTasks().size(), pool1.getTasks().size());

            final TaskTableTask task1 = pool1.getTasks().get(0);
            final TaskTableTask expectedTask1 = expectedPool1.getTasks().get(0);
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

            final TaskTableInput input1 = task1.getInputs().get(0);
            final TaskTableInput expectedInput1 = expectedTask1.getInputs().get(0);
            assertTrue("[Input1] Invalid mode",
                    expectedInput1.getMode() == input1.getMode());
            assertTrue("[Input1] Invalid mandatory",
                    expectedInput1.getMandatory() == input1.getMandatory());

            assertNotNull(input1.getAlternatives());
            assertEquals("[Alternative] Invalid number",
                    expectedInput1.getAlternatives().size(),
                    input1.getAlternatives().size());

            final TaskTableInputAlternative alt1 = input1.getAlternatives().get(0);
            final TaskTableInputAlternative expectedAlt1 =
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

            final TaskTableOuput output1 = task1.getOutputs().get(0);
            final TaskTableOuput expectedOutput1 = expectedTask1.getOutputs().get(0);
            assertTrue("[Output1] Invalid destination", expectedOutput1
                    .getDestination() == output1.getDestination());
            assertTrue("[Output1] Invalid mandatory",
                    expectedOutput1.getMandatory() == output1.getMandatory());
            assertEquals("[Output1] Invalid type", expectedOutput1.getType(),
                    output1.getType());
            assertTrue("[Output1] Invalid filename type", expectedOutput1
                    .getFileNameType() == output1.getFileNameType());

    }

// FIXME: Enable test
//    /**
//     * Test the conversion JobOrder => JobOrder.xml => JobORder
//     */
//    @Test
//    public void testMarshallingJobOrder() {
//        JobOrder job = TestL0Utils.buildJobOrderL20171109175634707000125(true);
//        File file = new File("./tmp/jobOrder.xml");
//
//        try {
//            file.createNewFile();
//            xmlConverter.convertFromObjectToXML(job, "./tmp/jobOrder.xml");
//
//            JobOrder convertJobOrder = (JobOrder) xmlConverter
//                    .convertFromXMLToObject("./tmp/jobOrder.xml");
//
//            assertNotNull("Conf not null", convertJobOrder.getConf());
//            assertEquals("Conf not equal", job.getConf(),
//                    convertJobOrder.getConf());
//
//            assertNotNull("Con files",
//                    convertJobOrder.getConf().getConfigFiles());
//            assertTrue("9 con files",
//                    job.getConf().getConfigFiles().size() == convertJobOrder
//                            .getConf().getConfigFiles().size());
//            assertEquals("Conf not equal",
//                    job.getConf().getConfigFiles().get(0),
//                    convertJobOrder.getConf().getConfigFiles().get(0));
//
//            assertNotNull("getProcParams",
//                    convertJobOrder.getConf().getProcParams());
//            assertTrue("getProcParams",
//                    job.getConf().getProcParams().size() == convertJobOrder
//                            .getConf().getProcParams().size());
//            assertEquals("Conf not equal", job.getConf().getProcParams().get(0),
//                    convertJobOrder.getConf().getProcParams().get(0));
//
//            assertNotNull("getProcs", convertJobOrder.getProcs());
//            assertTrue("getProcs",
//                    job.getProcs().size() == convertJobOrder.getProcs().size());
//
//            assertNotNull("getInputs",
//                    convertJobOrder.getProcs().get(0).getInputs());
//            assertTrue("getInputs",
//                    job.getProcs().get(0).getInputs().size() == convertJobOrder
//                            .getProcs().get(0).getInputs().size());
//            // TODO update because object storage key is null
//            // assertEquals("Conf not equal",
//            // job.getProcs().get(0).getInputs().get(0),
//            // convertJobOrder.getProcs().get(0).getInputs().get(0));
//
//            assertNotNull("getOutputs",
//                    convertJobOrder.getProcs().get(0).getOutputs());
//            assertTrue("getOutputs",
//                    job.getProcs().get(0).getOutputs().size() == convertJobOrder
//                            .getProcs().get(0).getOutputs().size());
//            assertEquals("Conf not equal",
//                    job.getProcs().get(0).getOutputs().get(0),
//                    convertJobOrder.getProcs().get(0).getOutputs().get(0));
//        } catch (IOException | JAXBException e) {
//            fail("Exception raised", e);
//            e.printStackTrace();
//        } finally {
//            file.delete();
//        }
//    }

// FIXME: Enable test
//    @Test
//    public void testMarshallingJobOrderIntoString() {
//        JobOrder job = TestL0Utils.buildJobOrderL20171109175634707000125(true);
//        File file = new File("./tmp/jobOrderFromString.xml");
//
//        try {
//            file.createNewFile();
//
//            String jobString = xmlConverter.convertFromObjectToXMLString(job);
//            try (PrintStream out = new PrintStream(
//                    new FileOutputStream("./tmp/jobOrderFromString.xml"))) {
//                out.print(jobString);
//            }
//
//            JobOrder convertJobOrder = (JobOrder) xmlConverter
//                    .convertFromXMLToObject("./tmp/jobOrderFromString.xml");
//
//            assertNotNull("Conf not null", convertJobOrder.getConf());
//            assertEquals("Conf not equal", job.getConf(),
//                    convertJobOrder.getConf());
//
//            assertNotNull("Con files",
//                    convertJobOrder.getConf().getConfigFiles());
//            assertTrue("9 con files",
//                    job.getConf().getConfigFiles().size() == convertJobOrder
//                            .getConf().getConfigFiles().size());
//            assertEquals("Conf not equal",
//                    job.getConf().getConfigFiles().get(0),
//                    convertJobOrder.getConf().getConfigFiles().get(0));
//
//            assertNotNull("getProcParams",
//                    convertJobOrder.getConf().getProcParams());
//            assertTrue("getProcParams",
//                    job.getConf().getProcParams().size() == convertJobOrder
//                            .getConf().getProcParams().size());
//            assertEquals("Conf not equal", job.getConf().getProcParams().get(0),
//                    convertJobOrder.getConf().getProcParams().get(0));
//
//            assertNotNull("getProcs", convertJobOrder.getProcs());
//            assertTrue("getProcs",
//                    job.getProcs().size() == convertJobOrder.getProcs().size());
//
//            assertNotNull("getInputs",
//                    convertJobOrder.getProcs().get(0).getInputs());
//            assertTrue("getInputs",
//                    job.getProcs().get(0).getInputs().size() == convertJobOrder
//                            .getProcs().get(0).getInputs().size());
//            // TODO update because object storage key is null
//            // assertEquals("Conf not equal",
//            // job.getProcs().get(0).getInputs().get(0),
//            // convertJobOrder.getProcs().get(0).getInputs().get(0));
//
//            assertNotNull("getOutputs",
//                    convertJobOrder.getProcs().get(0).getOutputs());
//            assertTrue("getOutputs",
//                    job.getProcs().get(0).getOutputs().size() == convertJobOrder
//                            .getProcs().get(0).getOutputs().size());
//            assertEquals("Conf not equal",
//                    job.getProcs().get(0).getOutputs().get(0),
//                    convertJobOrder.getProcs().get(0).getOutputs().get(0));
//        } catch (IOException | JAXBException e) {
//            fail("Exception raised", e);
//            e.printStackTrace();
//        } finally {
//            file.delete();
//        }
//    }

    @Test
    public void testUnmarshalingL1Routing() throws IOException, JAXBException {
        final LevelProductsRouting converted =
                (LevelProductsRouting) uut.convertFromXMLToObject(
                        "./test/data/l1_config/routing.xml");
        final LevelProductsRouting expected = buildL1Routing();

        assertEquals("0", expected.getRoutes().get(0),
                converted.getRoutes().get(0));

    }
    
    public static LevelProductsRouting buildL1Routing() {
        final LevelProductsRouting r = new LevelProductsRouting();
        r.addRoute(new LevelProductsRoute(new LevelProductsRouteFrom("EN", "A"),
                new LevelProductsRouteTo(Arrays.asList("EN_RAW__0_GRDF_1.xml"))));
        r.addRoute(new LevelProductsRoute(new LevelProductsRouteFrom("EN", "B"),
                new LevelProductsRouteTo(Arrays.asList("EN_RAW__0_SLC__1.xml",
                        "EN_RAW__0_SLC__1_GRDF_1.xml",
                        "EN_RAW__0_SLC__1_GRDH_1.xml"))));
        r.addRoute(new LevelProductsRoute(new LevelProductsRouteFrom("EW", "A"),
                new LevelProductsRouteTo(Arrays.asList("EW_RAW__0_GRDH_1.xml",
                        "EW_RAW__0_GRDM_1.xml", "EW_RAW__0_SLC__1.xml",
                        "EW_RAW__0_SLC__1_GRDH_1.xml",
                        "EW_RAW__0_SLC__1_GRDM_1.xml"))));
        r.addRoute(new LevelProductsRoute(new LevelProductsRouteFrom("EW", "B"),
                new LevelProductsRouteTo(Arrays.asList("EW_RAW__0_SLC__1.xml",
                        "EW_RAW__0_SLC__1_GRDH_1.xml",
                        "EW_RAW__0_SLC__1_GRDM_1.xml"))));
        r.addRoute(new LevelProductsRoute(new LevelProductsRouteFrom("IW", "A"),
                new LevelProductsRouteTo(Arrays.asList("IW_RAW__0_GRDH_1.xml",
                        "IW_RAW__0_GRDM_1.xml", "IW_RAW__0_SLC__1.xml",
                        "IW_RAW__0_SLC__1_GRDH_1.xml",
                        "IW_RAW__0_SLC__1_GRDM_1.xml"))));
        r.addRoute(new LevelProductsRoute(new LevelProductsRouteFrom("IW", "B"),
                new LevelProductsRouteTo(Arrays.asList("IW_RAW__0_SLC__1.xml",
                        "IW_RAW__0_SLC__1_GRDH_1.xml",
                        "IW_RAW__0_SLC__1_GRDM_1.xml"))));
        r.addRoute(new LevelProductsRoute(new LevelProductsRouteFrom("S[1-6]", "A"),
                new LevelProductsRouteTo(Arrays.asList("SM_RAW__0_GRDF_1.xml",
                        "SM_RAW__0_GRDH_1.xml", "SM_RAW__0_GRDM_1.xml",
                        "SM_RAW__0_SLC__1.xml", "SM_RAW__0_SLC__1_GRDF_1.xml",
                        "SM_RAW__0_SLC__1_GRDH_1.xml"))));
        r.addRoute(new LevelProductsRoute(new LevelProductsRouteFrom("S[1-6]", "B"),
                new LevelProductsRouteTo(Arrays.asList("SM_RAW__0_SLC__1.xml",
                        "SM_RAW__0_SLC__1_GRDF_1.xml",
                        "SM_RAW__0_SLC__1_GRDH_1.xml"))));

        return r;
    }
}

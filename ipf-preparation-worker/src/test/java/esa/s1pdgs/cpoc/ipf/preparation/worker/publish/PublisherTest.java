package esa.s1pdgs.cpoc.ipf.preparation.worker.publish;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.xml.bind.JAXBException;

import org.hamcrest.CustomMatcher;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobFile;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobInput;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobTaskInputs;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.IpfPreparationWorkerSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.ProcessSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.JobGen;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.converter.TaskTableToJobOrderConverter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.ElementMapper;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.ProductTypeAdapter;
import esa.s1pdgs.cpoc.mqi.client.MqiClient;
import esa.s1pdgs.cpoc.xml.XmlConverter;
import esa.s1pdgs.cpoc.xml.config.XmlConfig;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrder;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderInputFile;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderProc;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTable;


@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class PublisherTest {

    @Mock
    ProductTypeAdapter typeAdapter;
    @Autowired
    private IpfPreparationWorkerSettings ipfPreparationWorkerSettings;
    @Autowired
    private ProcessSettings settings;
    @Autowired
    private ElementMapper elementMapper;
    @Mock
    private MqiClient mqiClient;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void buildJobOrder() throws IOException, JAXBException {

        final String xmlFile = "./test/data/generic_config/task_tables/TaskTable.AIOP.xml";
        final XmlConverter converter = new XmlConfig().xmlConverter();
        final TaskTable taskTable = (TaskTable) converter.convertFromXMLToObject(xmlFile);
        final TaskTableAdapter taskTableAdapter = new TaskTableAdapter(new File(xmlFile), taskTable, elementMapper);

        AppDataJob job = new AppDataJob(133L);
        final AppDataJobProduct product = new AppDataJobProduct();

        //TODO where to set these properties?
        //product.setStartTime("2020-07-13T12:20:00.000000Z");
        //product.setStopTime("2020-07-13T12:25:00.000000Z");
        //product.setRaws1(createSomeRaws("ch1"));
        //product.setRaws2(createSomeRaws("ch2"));
        job.setProduct(product);

        JobOrder jobOrder = new TaskTableToJobOrderConverter().apply(taskTable);

        JobGen jobGen = new JobGen(job, typeAdapter, null, taskTableAdapter, null, jobOrder, null);

        job.setAdditionalInputs(createAdditionalInputs(
                //format is task:input:file1,file2,...
                "AIOP_PROC_APP:MPL_ORBPRE:PRE_1,PRE_2,PRE_3",
                "AIOP_PROC_APP:MPL_ORBSCT:SCT_1,SCT_2",
                "AIOP_PROC_APP:AUX_OBMEMC:OBM_1",
                "AIOP_DPASSEMBLER_APP:MPL_ORBPRE:PRE_1,PRE_2,PRE_3",
                "AIOP_DPASSEMBLER_APP:MPL_ORBSCT:SCT_1,SCT_2",
                "AIOP_DPASSEMBLER_APP:AUX_OBMEMC:OBM_1",
                "AIOP_LIST_APP:AUX_OBMEMC:OBM_1"));

        new Publisher(ipfPreparationWorkerSettings, settings, elementMapper, converter, mqiClient).buildJobOrder(jobGen, "/tmp/WD33/");

        assertThat(
                procOfType("AIOP_PROC_APP", jobOrder),
                hasInputs(
                        "/tmp/WD33/PRE_1",
                        "/tmp/WD33/PRE_2",
                        "/tmp/WD33/PRE_3",
                        "/tmp/WD33/SCT_2",
                        "/tmp/WD33/SCT_1",
                        "/tmp/WD33/OBM_1"));

        assertThat(
                procOfType("AIOP_DPASSEMBLER_APP", jobOrder),
                hasInputs(
                        "/tmp/WD33/PRE_1",
                        "/tmp/WD33/PRE_2",
                        "/tmp/WD33/PRE_3",
                        "/tmp/WD33/SCT_1",
                        "/tmp/WD33/SCT_2",
                        "/tmp/WD33/OBM_1"
                ));

        assertThat(
                procOfType("AIOP_LIST_APP", jobOrder),
                hasInputs(
                        "/tmp/WD33/OBM_1"));

    }

    private JobOrderProc procOfType(String type, JobOrder jobOrder) {
        final Optional<JobOrderProc> proc = jobOrder.getProcs().stream().filter(p -> p.getTaskName().equals(type)).findFirst();

        if (!proc.isPresent()) {
            throw new IllegalArgumentException(format("job order does not have proc %s", type));
        }

        return proc.get();
    }

    //check if proc has expectedInputs where in input is defined by "resultFileName"
    private Matcher<JobOrderProc> hasInputs(String... expectedInputs) {
        return new CustomMatcher<JobOrderProc>(format("proc with inputs %s", asList(expectedInputs))) {
            @Override
            public boolean matches(Object item) {
                if (!(item instanceof JobOrderProc)) {
                    return false;
                }

                JobOrderProc actual = (JobOrderProc) item;

                final List<String> actualInputs = actual.getInputs().stream()
                        .flatMap(i -> i.getFilenames().stream().map(JobOrderInputFile::getFilename))
                        .sorted()
                        .collect(toList());

                assertThat(actualInputs, is(equalTo(Stream.of(expectedInputs).sorted().collect(toList()))));

                return Stream.of(expectedInputs).sorted().collect(toList()).equals(actualInputs);
            }
        };
    }

    private List<AppDataJobFile> createSomeRaws(String channel) {
        return IntStream.range(0, 10).mapToObj(i -> new AppDataJobFile("raw_" + channel + "_" + i)).collect(toList());
    }

    //input format: task1:input:file1,file2,file3
    private List<AppDataJobTaskInputs> createAdditionalInputs(String... inputs) {

        final Map<String, List<String>> inputsByTaskName = Stream.of(inputs).collect(groupingBy(input -> input.split(":")[0]));

        return inputsByTaskName.entrySet().stream().map(
                entry -> new AppDataJobTaskInputs(entry.getKey(), "01.00", toInput(entry.getValue()))).collect(toList());
    }

    private List<AppDataJobInput> toInput(List<String> inputs) {
        return inputs.stream().map(input -> new AppDataJobInput(input.split(":")[1], "DIRECTORY", toFiles(input.split(":")[2]))).collect(toList());
    }

    //files format: file1,file2,file3
    private List<AppDataJobFile> toFiles(String files) {
        return Stream.of(files.split(",")).map(file -> new AppDataJobFile(file)).collect(toList());
    }

}
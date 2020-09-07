package esa.s1pdgs.cpoc.ipf.preparation.worker.publish;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.hamcrest.CustomMatcher;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobFile;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobInput;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobTaskInputs;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.ProcessSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.ProductMode;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.ElementMapper;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableFactory;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.ProductTypeAdapter;
import esa.s1pdgs.cpoc.xml.XmlConverter;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrder;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderInputFile;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderProc;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class JobOrderAdapterTest {

    @Autowired
    private ElementMapper elementMapper;

    @Autowired
    private TaskTableFactory taskTableFactory;

    @Autowired
    private ProcessSettings processSettings;

    @Autowired
    private XmlConverter xmlConverter;

    @Mock
    private ProductTypeAdapter productTypeAdapter;

    @Test
    public final void bogusTest() {
    	
    }

    public void testFactoryNewJobOrderFor() {
        final File xmlFile = new File("./test/data/generic_config/task_tables/TaskTable.AIOP.xml");

        final TaskTableAdapter taskTableAdapter = new TaskTableAdapter(
                xmlFile,
                taskTableFactory.buildTaskTable(xmlFile, processSettings.getLevel()),
                elementMapper
        );

        final JobOrder jobOrder = taskTableAdapter.newJobOrder(processSettings, ProductMode.SLICING);

        final JobOrderAdapter.Factory jobOrderFactory = new JobOrderAdapter.Factory(
                () -> jobOrder,
                productTypeAdapter,
                elementMapper,
                xmlConverter
        );

        final AppDataJob job = new AppDataJob(133L);
        final AppDataJobProduct product = new AppDataJobProduct();
        job.setProduct(product);

        job.setAdditionalInputs(createAdditionalInputs(
                //format is task:input:file1,file2,...
                "AIOP_PROC_APP:MPL_ORBPRE:PRE_1,PRE_2,PRE_3",
                "AIOP_PROC_APP:MPL_ORBSCT:SCT_1,SCT_2",
                "AIOP_PROC_APP:AUX_OBMEMC:OBM_1",
                "AIOP_DPASSEMBLER_APP:MPL_ORBPRE:PRE_1,PRE_2,PRE_3",
                "AIOP_DPASSEMBLER_APP:MPL_ORBSCT:SCT_1,SCT_2",
                "AIOP_DPASSEMBLER_APP:AUX_OBMEMC:OBM_1",
                "AIOP_LIST_APP:AUX_OBMEMC:OBM_1"));


        //internally the inputs are now added to the jobOrder
        jobOrderFactory.newJobOrderFor(job);

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

    private JobOrderProc procOfType(final String type, final JobOrder jobOrder) {
        final Optional<JobOrderProc> proc = jobOrder.getProcs().stream().filter(p -> p.getTaskName().equals(type)).findFirst();

        if (!proc.isPresent()) {
            throw new IllegalArgumentException(format("job order does not have proc %s", type));
        }

        return proc.get();
    }

    //check if proc has expectedInputs where in input is defined by "resultFileName"
    private Matcher<JobOrderProc> hasInputs(final String... expectedInputs) {
        return new CustomMatcher<JobOrderProc>(format("proc with inputs %s", asList(expectedInputs))) {
            @Override
            public boolean matches(final Object item) {
                if (!(item instanceof JobOrderProc)) {
                    return false;
                }

                final JobOrderProc actual = (JobOrderProc) item;

                final List<String> actualInputs = actual.getInputs().stream()
                        .flatMap(i -> i.getFilenames().stream().map(JobOrderInputFile::getFilename))
                        .sorted()
                        .collect(toList());

                assertThat(actualInputs, is(equalTo(Stream.of(expectedInputs).sorted().collect(toList()))));

                return Stream.of(expectedInputs).sorted().collect(toList()).equals(actualInputs);
            }
        };
    }


    //input format: task1:input:file1,file2,file3
    private List<AppDataJobTaskInputs> createAdditionalInputs(final String... inputs) {

        final Map<String, List<String>> inputsByTaskName = Stream.of(inputs).collect(groupingBy(input -> input.split(":")[0]));

        return inputsByTaskName.entrySet().stream().map(
                entry -> new AppDataJobTaskInputs(entry.getKey(), "01.00", toInput(entry.getValue()))).collect(toList());
    }

    private List<AppDataJobInput> toInput(final List<String> inputs) {
        return inputs.stream().map(input -> new AppDataJobInput(
                "ref",
                input.split(":")[1],
                "DIRECTORY",
                true,
                toFiles(input.split(":")[2]))).collect(toList());
    }

    //files format: file1,file2,file3
    private List<AppDataJobFile> toFiles(final String files) {
        return Stream.of(files.split(",")).map(AppDataJobFile::new).collect(toList());
    }
}
package esa.s1pdgs.cpoc.ipf.preparation.worker.query;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.hamcrest.CustomMatcher;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.XmlConfig;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.JobGen;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.ProductMode;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.converter.TaskTableToJobOrderConverter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.converter.XmlConverter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.JobOrder;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.JobOrderInputFile;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.JobOrderProc;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.ElementMapper;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTable;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.timeout.InputTimeoutChecker;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.client.SearchMetadataQuery;
import esa.s1pdgs.cpoc.metadata.model.SearchMetadata;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class AuxQueryTest {

    @Mock
    MetadataClient metadataClient;

    @Mock
    InputTimeoutChecker inputTimeoutChecker;

    @Autowired
    ElementMapper elementMapper;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void call() throws Exception {

        final String xmlFile = "./test/data/generic_config/task_tables/TaskTable.AIOP.xml";
        XmlConverter converter = new XmlConfig().xmlConverter();
        final TaskTable taskTable = (TaskTable) converter.convertFromXMLToObject(xmlFile);
        final TaskTableAdapter taskTableAdapter = new TaskTableAdapter(new File(xmlFile), taskTable, elementMapper);

        taskTableAdapter.allTaskTableInputs().forEach((key, value) -> System.out.println("input: " + key));

        AppDataJob job = new AppDataJob(133L);
        final AppDataJobProduct product = new AppDataJobProduct();
        product.setStartTime("2020-07-13T12:20:00.000000Z");
        product.setStopTime("2020-07-13T12:25:00.000000Z");
        job.setProduct(product);

        JobOrder jobOrder = new TaskTableToJobOrderConverter().apply(taskTable);

        JobGen jobGen = new JobGen(job, null, null, taskTableAdapter, null, null, jobOrder, null);

        when(metadataClient
                .search(argThat(isQueryWithType("MPL_ORBPRE")), any(), any(), any(), anyInt(), any(), any()))
                .thenReturn(singletonList(
                        new SearchMetadata(
                                "MPL_ORBPRE_RESULT",
                                "aux",
                                "aux",
                                "2020-07-13T12:20:00.000000Z",
                                "2020-07-13T12:25:00.000000Z",
                                "S1A",
                                "S1A",
                                "")));

        when(metadataClient
                .search(argThat(isQueryWithType("MPL_ORBSCT")), any(), any(), any(), anyInt(), any(), any()))
                .thenReturn(asList(
                        new SearchMetadata(
                                "MPL_ORBSCT_RESULT_1",
                                "aux",
                                "aux",
                                "2020-07-13T12:20:00.000000Z",
                                "2020-07-13T12:25:00.000000Z",
                                "S1A",
                                "S1A",
                                ""),
                        new SearchMetadata(
                                "MPL_ORBSCT_RESULT_2",
                                "aux",
                                "aux",
                                "2020-07-13T12:20:00.000000Z",
                                "2020-07-13T12:25:00.000000Z",
                                "S1A",
                                "S1A",
                                "")));

        when(metadataClient
                .search(argThat(isQueryWithType("AUX_OBMEMC")), any(), any(), any(), anyInt(), any(), any()))
                .thenReturn(singletonList(
                        new SearchMetadata(
                                "AUX_OBMEMC_RESULT",
                                "aux",
                                "aux",
                                "2020-07-13T12:20:00.000000Z",
                                "2020-07-13T12:25:00.000000Z",
                                "S1A",
                                "S1A",
                                "")));


        //actual test
        new AuxQueryHandler(metadataClient, ProductMode.SLICING, inputTimeoutChecker, elementMapper).queryFor(jobGen).call();

        //we have three distinct alternatives in the task table -> three different queries
        verify(metadataClient, times(3)).search(any(), any(), any(), any(), anyInt(), any(), any());
        verify(metadataClient, times(1))
                .search(
                        argThat(isQueryMatching(
                                "MPL_ORBPRE",
                                ProductFamily.AUXILIARY_FILE,
                                345600.0,
                                0.0,
                                "LatestValCover")),
                        eq("2020-07-13T12:20:00.000000Z"), eq("2020-07-13T12:25:00.000000Z"), any(), anyInt(), any(), any());

        verify(metadataClient, times(1))
                .search(
                        argThat(isQueryMatching(
                                "MPL_ORBSCT",
                                ProductFamily.AUXILIARY_FILE,
                                0.0,
                                0.0,
                                "LatestValCover")),
                        eq("2020-07-13T12:20:00.000000Z"), eq("2020-07-13T12:25:00.000000Z"), any(), anyInt(), any(), any());

        verify(metadataClient, times(1))
                .search(
                        argThat(isQueryMatching(
                                "AUX_OBMEMC",
                                ProductFamily.AUXILIARY_FILE,
                                0.0,
                                0.0,
                                "LatestValCover")),
                        eq("2020-07-13T12:20:00.000000Z"), eq("2020-07-13T12:25:00.000000Z"), any(), anyInt(), any(), any());


        assertThat(procOfType(
                "AIOP_PROC_APP", jobGen.jobOrder()),
                hasInputs("AUX_OBMEMC:AUX_OBMEMC_RESULT", "MPL_ORBPRE:MPL_ORBPRE_RESULT", "MPL_ORBSCT:MPL_ORBSCT_RESULT_1", "MPL_ORBSCT:MPL_ORBSCT_RESULT_2"));

        assertThat(procOfType(
                "AIOP_DPASSEMBLER_APP", jobGen.jobOrder()),
                hasInputs("AUX_OBMEMC:AUX_OBMEMC_RESULT", "MPL_ORBPRE:MPL_ORBPRE_RESULT", "MPL_ORBSCT:MPL_ORBSCT_RESULT_1", "MPL_ORBSCT:MPL_ORBSCT_RESULT_2"));

        assertThat(procOfType(
                "AIOP_LIST_APP", jobGen.jobOrder()),
                hasInputs("AUX_OBMEMC:AUX_OBMEMC_RESULT"));
    }

    @Test
    public void callWithMultipleAlternativesAndRefInputs() throws Exception {

        final String xmlFile = "./test/data/generic_config/task_tables/IW_RAW__0_GRDH_1.xml";
        XmlConverter converter = new XmlConfig().xmlConverter();
        final TaskTable taskTable = (TaskTable) converter.convertFromXMLToObject(xmlFile);
        final TaskTableAdapter taskTableAdapter = new TaskTableAdapter(new File(xmlFile), taskTable, elementMapper);

        taskTableAdapter.allTaskTableInputs().forEach((key, value) -> System.out.println("input: " + key));

        AppDataJob job = new AppDataJob(133L);
        final AppDataJobProduct product = new AppDataJobProduct();
        product.setStartTime("2020-07-13T12:20:00.000000Z");
        product.setStopTime("2020-07-13T12:25:00.000000Z");
        job.setProduct(product);

        JobOrder jobOrder = new TaskTableToJobOrderConverter().apply(taskTable);

        JobGen jobGen = new JobGen(job, null, null, taskTableAdapter, elementMapper, null, jobOrder, null);

        //currently for multiple alternatives all are queried no matter a result has already been found or not
        //but later the first result is taken in the final job order
        expectAndReturnMetadataQuery("AUX_ATT", "AUX_ATT_RESULT", metadataClient);
        expectAndReturnMetadataQuery("AUX_CAL", "AUX_CAL_RESULT", metadataClient);
        expectAndReturnMetadataQuery("AUX_INS", "AUX_INS_RESULT", metadataClient);
        // no result for first alternative POE, in job order the second RESORB should be taken instead
        expectAndReturnMetadataQueryNoResult("AUX_POE", metadataClient);
        expectAndReturnMetadataQuery("AUX_PP1", "AUX_PP1_RESULT", metadataClient);
        expectAndReturnMetadataQuery("AUX_PRE", "AUX_PRE_RESULT", metadataClient);
        expectAndReturnMetadataQuery("AUX_RESORB", "AUX_RESORB_RESULT", metadataClient);
        expectAndReturnMetadataQuery("IW_RAW__0A", "IW_RAW__0A_RESULT", metadataClient);
        expectAndReturnMetadataQuery("IW_RAW__0C", "IW_RAW__0C_RESULT", metadataClient);
        expectAndReturnMetadataQuery("IW_RAW__0N", "IW_RAW__0N_RESULT", metadataClient);
        expectAndReturnMetadataQuery("IW_RAW__0S", "IW_RAW__0S_RESULT", metadataClient);

        //actual test
        new AuxQueryHandler(metadataClient, ProductMode.SLICING, inputTimeoutChecker, elementMapper).queryFor(jobGen).call();

        verify(metadataClient, times(11)).search(any(), any(), any(), any(), anyInt(), any(), any());

        verify(metadataClient, times(1)).search(argThat(isQueryWithType("AUX_ATT")), any(), any(), any(), anyInt(), any(), any());
        verify(metadataClient, times(1)).search(argThat(isQueryWithType("AUX_CAL")), any(), any(), any(), anyInt(), any(), any());
        verify(metadataClient, times(1)).search(argThat(isQueryWithType("AUX_INS")), any(), any(), any(), anyInt(), any(), any());
        verify(metadataClient, times(1)).search(argThat(isQueryWithType("AUX_POE")), any(), any(), any(), anyInt(), any(), any());
        verify(metadataClient, times(1)).search(argThat(isQueryWithType("AUX_PP1")), any(), any(), any(), anyInt(), any(), any());
        verify(metadataClient, times(1)).search(argThat(isQueryWithType("AUX_PRE")), any(), any(), any(), anyInt(), any(), any());
        //AUX_RES is changed to AUX_RESORB via configuration
        verify(metadataClient, times(1)).search(argThat(isQueryWithType("AUX_RESORB")), any(), any(), any(), anyInt(), any(), any());
        verify(metadataClient, times(1)).search(argThat(isQueryWithType("IW_RAW__0A")), any(), any(), any(), anyInt(), any(), any());
        verify(metadataClient, times(1)).search(argThat(isQueryWithType("IW_RAW__0C")), any(), any(), any(), anyInt(), any(), any());
        verify(metadataClient, times(1)).search(argThat(isQueryWithType("IW_RAW__0N")), any(), any(), any(), anyInt(), any(), any());
        verify(metadataClient, times(1)).search(argThat(isQueryWithType("IW_RAW__0S")), any(), any(), any(), anyInt(), any(), any());

        assertThat(procOfType("PSC", jobOrder), hasInputs(
                "IW_RAW__0S_RESULT",
                "IW_RAW__0C_RESULT",
                "IW_RAW__0N_RESULT",
                "IW_RAW__0A_RESULT",
                "AUX_PP1_RESULT",
                "AUX_CAL_RESULT",
                "AUX_INS_RESULT",
                "AUX_RESORB_RESULT",
                "AUX_ATT_RESULT"));

        assertThat(procOfType("MDC", jobOrder), hasInputs(
                "AUX_PP1_RESULT",
                "AUX_CAL_RESULT",
                "AUX_INS_RESULT",
                "AUX_RESORB_RESULT",
                "AUX_ATT_RESULT"));

        assertThat(procOfType("WPC", jobOrder), hasInputs(
                "AUX_PP1_RESULT",
                "AUX_CAL_RESULT",
                "AUX_INS_RESULT",
                "AUX_RESORB_RESULT",
                "AUX_ATT_RESULT"));

        assertThat(procOfType("LPC1", jobOrder), hasInputs(
                "IW_SL1__1_", //this is an output
                "AUX_PP1_RESULT",
                "AUX_CAL_RESULT",
                "AUX_INS_RESULT",
                "AUX_RESORB_RESULT",
                "AUX_ATT_RESULT"));

        assertThat(procOfType("stats", jobOrder), hasInputs(
                "AUX_PP1_RESULT",
                "AUX_CAL_RESULT",
                "AUX_INS_RESULT",
                "AUX_RESORB_RESULT",
                "AUX_ATT_RESULT"));
    }

    private void expectAndReturnMetadataQuery(String fileType, String result, MetadataClient metadataClient) throws MetadataQueryException {
        when(metadataClient
                .search(argThat(isQueryWithType(fileType)), any(), any(), any(), anyInt(), any(), any()))
                .thenReturn(singletonList(
                        new SearchMetadata(
                                result,
                                "aux",
                                "aux",
                                "2020-07-13T12:20:00.000000Z",
                                "2020-07-13T12:25:00.000000Z",
                                "S1A",
                                "S1A",
                                "")));
    }

    private void expectAndReturnMetadataQueryNoResult(String fileType, MetadataClient metadataClient) throws MetadataQueryException {
        when(metadataClient
                .search(argThat(isQueryWithType(fileType)), any(), any(), any(), anyInt(), any(), any()))
                .thenReturn(emptyList());
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

    private JobOrderProc procOfType(String type, JobOrder jobOrder) {
        final Optional<JobOrderProc> proc = jobOrder.getProcs().stream().filter(p -> p.getTaskName().equals(type)).findFirst();

        if (!proc.isPresent()) {
            throw new IllegalArgumentException(format("job order does not have proc %s", type));
        }

        return proc.get();
    }

    private ArgumentMatcher<SearchMetadataQuery> isQueryWithType(String productType) {
        return new ArgumentMatcher<SearchMetadataQuery>() {
            @Override
            public boolean matches(SearchMetadataQuery query) {
                return query != null && query.getProductType().equals(productType);
            }

            @Override
            public String toString() {
                return format("query with type %s", productType);
            }
        };
    }

    private ArgumentMatcher<SearchMetadataQuery> isQueryMatching(String productType, ProductFamily productFamily, double dT0, double dT1, String retrievalMode) {

        return new ArgumentMatcher<SearchMetadataQuery>() {
            @Override
            public boolean matches(SearchMetadataQuery searchMetadataQuery) {
                return searchMetadataQuery.getProductType().equals(productType)
                        && searchMetadataQuery.getProductFamily().equals(productFamily)
                        && searchMetadataQuery.getDeltaTime0() == dT0
                        && searchMetadataQuery.getDeltaTime1() == dT1
                        && searchMetadataQuery.getRetrievalMode().equals(retrievalMode);

            }

            @Override
            public String toString() {
                return format("query with type %s of family %s dt0 %s dt1 %s retrieval mode %s",
                        productType, productFamily, dT0, dT1, retrievalMode);
            }
        };

    }

}
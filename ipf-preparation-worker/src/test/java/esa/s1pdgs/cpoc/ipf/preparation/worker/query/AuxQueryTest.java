package esa.s1pdgs.cpoc.ipf.preparation.worker.query;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.hamcrest.CustomMatcher;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobFile;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobTaskInputs;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.ProcessSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.ProductMode;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.ElementMapper;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableFactory;
import esa.s1pdgs.cpoc.ipf.preparation.worker.publish.Publisher;
import esa.s1pdgs.cpoc.ipf.preparation.worker.timeout.InputTimeoutChecker;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.client.SearchMetadataQuery;
import esa.s1pdgs.cpoc.metadata.model.SearchMetadata;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class AuxQueryTest {

    @Autowired
    private ElementMapper elementMapper;

    @Autowired
    private TaskTableFactory taskTableFactory;

    @Autowired
    private ProcessSettings processSettings;

    @Mock
    private MetadataClient metadataClient;

    @Mock
    private InputTimeoutChecker inputTimeoutChecker;

    @MockBean
    private Publisher publisher;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void call() throws Exception {

        final File xmlFile = new File("./test/data/generic_config/task_tables/TaskTable.AIOP.xml");

        final TaskTableAdapter taskTableAdapter = new TaskTableAdapter(
                xmlFile,
                taskTableFactory.buildTaskTable(xmlFile, processSettings.getLevel()),
                elementMapper
        );

        final AppDataJob job = new AppDataJob(133L);
        job.setStartTime("2020-07-13T12:20:00.000000Z");
        job.setStopTime("2020-07-13T12:25:00.000000Z");
        final AppDataJobProduct product = new AppDataJobProduct();
        product.getMetadata().put("satelliteId", "S1A");

        job.setProduct(product);

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
        final List<AppDataJobTaskInputs> appDataJobTaskInputs =
                new AuxQueryHandler(metadataClient, ProductMode.SLICING, inputTimeoutChecker, taskTableAdapter).queryFor(job).queryAux();

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


        assertThat(inputsForTask(
                "AIOP_PROC_APP:01.00", appDataJobTaskInputs),
                hasInputs("AUX_OBMEMC_RESULT", "MPL_ORBPRE_RESULT", "MPL_ORBSCT_RESULT_1", "MPL_ORBSCT_RESULT_2"));

        assertThat(inputsForTask(
                "AIOP_DPASSEMBLER_APP:01.00", appDataJobTaskInputs),
                hasInputs("AUX_OBMEMC_RESULT", "MPL_ORBPRE_RESULT", "MPL_ORBSCT_RESULT_1", "MPL_ORBSCT_RESULT_2"));

        assertThat(inputsForTask(
                "AIOP_LIST_APP:01.00", appDataJobTaskInputs),
                hasInputs("AUX_OBMEMC_RESULT"));
    }

    @Test
    public void callWithMultipleAlternativesAndRefInputs() throws Exception {

        final File xmlFile = new File("./test/data/generic_config/task_tables/IW_RAW__0_GRDH_1.xml");

        final TaskTableAdapter taskTableAdapter = new TaskTableAdapter(
                xmlFile,
                taskTableFactory.buildTaskTable(xmlFile, processSettings.getLevel()),
                elementMapper
        );

        final AppDataJob job = new AppDataJob(133L);

        job.setStartTime("2020-07-13T12:20:00.000000Z");
        job.setStopTime("2020-07-13T12:25:00.000000Z");
        final AppDataJobProduct product = new AppDataJobProduct();
        product.getMetadata().put("satelliteId", "S1A");

        job.setProduct(product);

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
        final List<AppDataJobTaskInputs> appDataJobTaskInputs = new AuxQueryHandler(metadataClient, ProductMode.SLICING, inputTimeoutChecker, taskTableAdapter).queryFor(job).queryAux();

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

        assertThat(inputsForTask("PSC:3.20", appDataJobTaskInputs), hasInputs(
                "IW_RAW__0S_RESULT",
                "IW_RAW__0C_RESULT",
                "IW_RAW__0N_RESULT",
                "IW_RAW__0A_RESULT",
                "AUX_PP1_RESULT",
                "AUX_CAL_RESULT",
                "AUX_INS_RESULT",
                "AUX_RESORB_RESULT",
                "AUX_ATT_RESULT"));

        assertThat(inputsForTask("MDC:3.20", appDataJobTaskInputs), hasInputs(
                "AUX_PP1_RESULT",
                "AUX_CAL_RESULT",
                "AUX_INS_RESULT",
                "AUX_RESORB_RESULT",
                "AUX_ATT_RESULT"));

        assertThat(inputsForTask("WPC:3.20", appDataJobTaskInputs), hasInputs(
                "AUX_PP1_RESULT",
                "AUX_CAL_RESULT",
                "AUX_INS_RESULT",
                "AUX_RESORB_RESULT",
                "AUX_ATT_RESULT"));

        assertThat(inputsForTask("LPC1:3.20", appDataJobTaskInputs), hasInputs(
                "IW_SL1__1_", //this is an output
                "AUX_PP1_RESULT",
                "AUX_CAL_RESULT",
                "AUX_INS_RESULT",
                "AUX_RESORB_RESULT",
                "AUX_ATT_RESULT"));

        assertThat(inputsForTask("stats:3.20", appDataJobTaskInputs), hasInputs(
                "AUX_PP1_RESULT",
                "AUX_CAL_RESULT",
                "AUX_INS_RESULT",
                "AUX_RESORB_RESULT",
                "AUX_ATT_RESULT"));
    }

    private void expectAndReturnMetadataQuery(final String fileType, final String result, final MetadataClient metadataClient) throws MetadataQueryException {
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

    private void expectAndReturnMetadataQueryNoResult(final String fileType, final MetadataClient metadataClient) throws MetadataQueryException {
        when(metadataClient
                .search(argThat(isQueryWithType(fileType)), any(), any(), any(), anyInt(), any(), any()))
                .thenReturn(emptyList());
    }

    private AppDataJobTaskInputs inputsForTask(final String taskVersion, final List<AppDataJobTaskInputs> inputs) {
        final Optional<AppDataJobTaskInputs> candidate =
                inputs.stream()
                        .filter(i -> taskVersion.equals(i.getTaskName() + ":" + i.getTaskVersion())).findFirst();

        Assert.assertTrue("input is present: " + taskVersion, candidate.isPresent());

        return candidate.get();
    }

    //check if proc has expectedInputs where in input is defined by "resultFileName"
    private Matcher<AppDataJobTaskInputs> hasInputs(final String... expectedInputs) {
        return new CustomMatcher<AppDataJobTaskInputs>(format("proc with inputs %s", asList(expectedInputs))) {
            @Override
            public boolean matches(final Object item) {
                if (!(item instanceof AppDataJobTaskInputs)) {
                    return false;
                }

                final AppDataJobTaskInputs actual = (AppDataJobTaskInputs) item;

                final List<String> actualInputs = actual.getInputs().stream()
                        .flatMap(i -> i.getFiles().stream().map(AppDataJobFile::getFilename))
                        .sorted()
                        .collect(toList());

                assertThat(actualInputs, is(equalTo(Stream.of(expectedInputs).sorted().collect(toList()))));

                return Stream.of(expectedInputs).sorted().collect(toList()).equals(actualInputs);
            }
        };
    }


    private ArgumentMatcher<SearchMetadataQuery> isQueryWithType(final String productType) {
        return new ArgumentMatcher<SearchMetadataQuery>() {
            @Override
            public boolean matches(final SearchMetadataQuery query) {
                return query != null && query.getProductType().equals(productType);
            }

            @Override
            public String toString() {
                return format("query with type %s", productType);
            }
        };
    }

    private ArgumentMatcher<SearchMetadataQuery> isQueryMatching(final String productType, final ProductFamily productFamily, final double dT0, final double dT1, final String retrievalMode) {

        return new ArgumentMatcher<SearchMetadataQuery>() {
            @Override
            public boolean matches(final SearchMetadataQuery searchMetadataQuery) {
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
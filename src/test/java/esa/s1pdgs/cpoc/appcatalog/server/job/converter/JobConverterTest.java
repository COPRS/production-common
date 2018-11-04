package esa.s1pdgs.cpoc.appcatalog.server.job.converter;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobDto;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobDtoState;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobFileDto;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobGenerationDto;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobGenerationDtoState;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobProductDto;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJobFile;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJobGeneration;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJobGenerationState;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJobState;
import esa.s1pdgs.cpoc.appcatalog.server.job.exception.AppCatalogJobGenerationInvalidStateException;
import esa.s1pdgs.cpoc.appcatalog.server.job.exception.AppCatalogJobInvalidStateException;
import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.mqi.model.queue.EdrsSessionDto;
import esa.s1pdgs.cpoc.mqi.model.rest.EdrsSessionsMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

/**
 * Test the job converter
 * 
 * @author Viveris Technologies
 */
public class JobConverterTest {

    /**
     * Db object
     */
    private AppDataJob jobDb;

    /**
     * DTO object
     */
    private AppDataJobDto<EdrsSessionDto> jobDto;

    private JobConverter<EdrsSessionDto> converter =
            new EdrsSessionJobConverter();

    @Before
    public void init() {
    }

    private void buildJobs(boolean productNull, boolean productRaw1Null,
            boolean productRaw2Empty) {

        Date creationDate = new Date(System.currentTimeMillis() - 152000);
        Date lastUpdateDate = new Date();
        Date creationDateGen1 = new Date(System.currentTimeMillis() - 12000);
        Date creationDateGen2 = new Date(System.currentTimeMillis() - 99000);
        Date updateDateGen1 = new Date(System.currentTimeMillis() - 1200);

        GenericMessageDto<EdrsSessionDto> message1 =
                new EdrsSessionsMessageDto(124, "input1", null);
        GenericMessageDto<EdrsSessionDto> message2 =
                new EdrsSessionsMessageDto(12, "input2", null);

        jobDb = new AppDataJob();
        jobDb.setCategory(ProductCategory.EDRS_SESSIONS);
        jobDb.setCreationDate(creationDate);
        jobDb.setIdentifier(1123L);
        jobDb.setLevel(ApplicationLevel.L0);
        jobDb.setPod("pod-name");
        jobDb.setLastUpdateDate(lastUpdateDate);
        jobDb.setState(AppDataJobState.GENERATING);
        if (!productNull) {
            AppDataJobProduct productDb = new AppDataJobProduct();
            productDb.setAcquisition("IW");
            productDb.setDataTakeId("123456");
            productDb.setInsConfId(5);
            productDb.setMissionId("S1");
            productDb.setNumberSlice(6);
            productDb.setProductName("product-name");
            productDb.setProductType("product-type");
            productDb.setSatelliteId("B");
            productDb.setSegmentStartDate("start-date");
            productDb.setSegmentStopDate("stop-date");
            productDb.setSessionId("session-id");
            productDb.setTotalNbOfSlice(15);
            productDb.setStartTime("2017-12-24T10:24:33.123456Z");
            productDb.setStopTime("2017-12-25T10:24:33.123456Z");
            productDb.setProcessMode("FAST");
            if (!productRaw1Null) {
                productDb.setRaws1(
                        Arrays.asList(new AppDataJobFile("file1", "key1"),
                                new AppDataJobFile("file5", "key5"),
                                new AppDataJobFile("file3", "key3")));
            }
            if (productRaw2Empty) {
                productDb.setRaws2(new ArrayList<>());
            } else {
                productDb.setRaws2(
                        Arrays.asList(new AppDataJobFile("file2", "key2")));
            }
            jobDb.setProduct(productDb);
        }
        jobDb.setMessages(Arrays.asList(message1, message2));
        AppDataJobGeneration gen1 = new AppDataJobGeneration();
        gen1.setTaskTable("task1");
        gen1.setNbErrors(2);
        gen1.setCreationDate(creationDateGen1);
        gen1.setState(AppDataJobGenerationState.READY);
        gen1.setLastUpdateDate(updateDateGen1);
        AppDataJobGeneration gen2 = new AppDataJobGeneration();
        gen2.setTaskTable("task2");
        gen2.setNbErrors(0);
        gen2.setCreationDate(creationDateGen2);
        gen2.setState(AppDataJobGenerationState.SENT);
        jobDb.setGenerations(Arrays.asList(gen1, gen2));

        jobDto = new AppDataJobDto<>();
        jobDto.setCreationDate(creationDate);
        jobDto.setIdentifier(1123L);
        jobDto.setLevel(ApplicationLevel.L0);
        jobDto.setPod("pod-name");
        jobDto.setLastUpdateDate(lastUpdateDate);
        jobDto.setState(AppDataJobDtoState.GENERATING);
        if (!productNull) {
            AppDataJobProductDto productDto = new AppDataJobProductDto();
            productDto.setAcquisition("IW");
            productDto.setDataTakeId("123456");
            productDto.setInsConfId(5);
            productDto.setMissionId("S1");
            productDto.setNumberSlice(6);
            productDto.setProductName("product-name");
            productDto.setProductType("product-type");
            productDto.setSatelliteId("B");
            productDto.setSegmentStartDate("start-date");
            productDto.setSegmentStopDate("stop-date");
            productDto.setSessionId("session-id");
            productDto.setTotalNbOfSlice(15);
            productDto.setStartTime("2017-12-24T10:24:33.123456Z");
            productDto.setStopTime("2017-12-25T10:24:33.123456Z");
            productDto.setProcessMode("FAST");
            if (!productRaw1Null) {
                productDto.setRaws1(
                        Arrays.asList(new AppDataJobFileDto("file1", "key1"),
                                new AppDataJobFileDto("file5", "key5"),
                                new AppDataJobFileDto("file3", "key3")));
            }
            if (productRaw2Empty) {
                productDto.setRaws2(new ArrayList<>());
            } else {
                productDto.setRaws2(
                        Arrays.asList(new AppDataJobFileDto("file2", "key2")));
            }
            jobDto.setProduct(productDto);
        }
        jobDto.setMessages(Arrays.asList(message1, message2));
        AppDataJobGenerationDto gen3 = new AppDataJobGenerationDto();
        gen3.setTaskTable("task1");
        gen3.setNbErrors(2);
        gen3.setCreationDate(creationDateGen1);
        gen3.setState(AppDataJobGenerationDtoState.READY);
        gen3.setLastUpdateDate(updateDateGen1);
        AppDataJobGenerationDto gen4 = new AppDataJobGenerationDto();
        gen4.setTaskTable("task2");
        gen4.setNbErrors(0);
        gen4.setCreationDate(creationDateGen2);
        gen4.setState(AppDataJobGenerationDtoState.SENT);
        jobDto.setGenerations(Arrays.asList(gen3, gen4));
    }

    @Test
    public void testConvertDbToDto() throws AppCatalogJobInvalidStateException,
            AppCatalogJobGenerationInvalidStateException {
        buildJobs(false, false, false);
        AppDataJobDto<EdrsSessionDto> resultDto =
                converter.convertJobFromDbToDto(jobDb);
        assertEquals(jobDto, resultDto);
    }

    @Test
    public void testConvertDtoToDb() throws AppCatalogJobInvalidStateException,
            AppCatalogJobGenerationInvalidStateException {
        buildJobs(false, false, false);
        AppDataJob resultDb = converter.convertJobFromDtoToDb(jobDto);
        assertEquals(jobDb, resultDb);
    }

    @Test
    public void testConvertDbToDtoProductNull()
            throws AppCatalogJobInvalidStateException,
            AppCatalogJobGenerationInvalidStateException {
        buildJobs(true, false, false);
        AppDataJobDto<EdrsSessionDto> resultDto =
                converter.convertJobFromDbToDto(jobDb);
        assertEquals(jobDto, resultDto);
    }

    @Test
    public void testConvertDtoToDbProductNull()
            throws AppCatalogJobInvalidStateException,
            AppCatalogJobGenerationInvalidStateException {
        buildJobs(true, false, false);
        AppDataJob resultDb = converter.convertJobFromDtoToDb(jobDto);
        assertEquals(jobDb, resultDb);
    }

    @Test
    public void testConvertDbToDtoRaw1null()
            throws AppCatalogJobInvalidStateException,
            AppCatalogJobGenerationInvalidStateException {
        buildJobs(false, true, false);
        AppDataJobDto<EdrsSessionDto> resultDto =
                converter.convertJobFromDbToDto(jobDb);
        assertEquals(jobDto, resultDto);
    }

    @Test
    public void testConvertDtoToDbRaw1null()
            throws AppCatalogJobInvalidStateException,
            AppCatalogJobGenerationInvalidStateException {
        buildJobs(false, true, false);
        AppDataJob resultDb = converter.convertJobFromDtoToDb(jobDto);
        assertEquals(jobDb, resultDb);
    }

    @Test
    public void testConvertDbToDtoRaw2Empty()
            throws AppCatalogJobInvalidStateException,
            AppCatalogJobGenerationInvalidStateException {
        buildJobs(false, false, true);
        AppDataJobDto<EdrsSessionDto> resultDto =
                converter.convertJobFromDbToDto(jobDb);
        assertEquals(jobDto, resultDto);
    }

    @Test
    public void testConvertDtoToDbRaw2Empty()
            throws AppCatalogJobInvalidStateException,
            AppCatalogJobGenerationInvalidStateException {
        buildJobs(false, false, true);
        AppDataJob resultDb = converter.convertJobFromDtoToDb(jobDto);
        assertEquals(jobDb, resultDb);
    }

    /**
     * Test the conversion AppDataJobState -> AppDataJobDtoState
     * 
     * @throws AppCatalogJobInvalidStateException
     */
    @Test
    public void testconvertJobStateFromDbToDto()
            throws AppCatalogJobInvalidStateException {
        assertEquals(AppDataJobDtoState.WAITING,
                converter.convertJobStateFromDbToDto(AppDataJobState.WAITING));
        assertEquals(AppDataJobDtoState.DISPATCHING, converter
                .convertJobStateFromDbToDto(AppDataJobState.DISPATCHING));
        assertEquals(AppDataJobDtoState.GENERATING, converter
                .convertJobStateFromDbToDto(AppDataJobState.GENERATING));
        assertEquals(AppDataJobDtoState.TERMINATED, converter
                .convertJobStateFromDbToDto(AppDataJobState.TERMINATED));
    }

    /**
     * Test the conversion AppDataJobDtoState -> AppDataJobState
     * 
     * @throws AppCatalogJobInvalidStateException
     */
    @Test
    public void testconvertJobStateFromDtoToDb()
            throws AppCatalogJobInvalidStateException {
        assertEquals(AppDataJobState.WAITING, converter
                .convertJobStateFromDtoToDb(AppDataJobDtoState.WAITING));
        assertEquals(AppDataJobState.DISPATCHING, converter
                .convertJobStateFromDtoToDb(AppDataJobDtoState.DISPATCHING));
        assertEquals(AppDataJobState.GENERATING, converter
                .convertJobStateFromDtoToDb(AppDataJobDtoState.GENERATING));
        assertEquals(AppDataJobState.TERMINATED, converter
                .convertJobStateFromDtoToDb(AppDataJobDtoState.TERMINATED));
    }

    /**
     * Test the conversion AppDataJobGenerationState ->
     * AppDataJobGenerationDtoState
     * 
     * @throws AppCatalogJobInvalidStateException
     * @throws AppCatalogJobGenerationInvalidStateException
     */
    @Test
    public void testconvertJobGenerationStateFromDbToDto()
            throws AppCatalogJobGenerationInvalidStateException {
        assertEquals(AppDataJobGenerationDtoState.INITIAL,
                converter.convertJobGenerationStateFromDbToDto(
                        AppDataJobGenerationState.INITIAL));
        assertEquals(AppDataJobGenerationDtoState.PRIMARY_CHECK,
                converter.convertJobGenerationStateFromDbToDto(
                        AppDataJobGenerationState.PRIMARY_CHECK));
        assertEquals(AppDataJobGenerationDtoState.READY,
                converter.convertJobGenerationStateFromDbToDto(
                        AppDataJobGenerationState.READY));
        assertEquals(AppDataJobGenerationDtoState.SENT,
                converter.convertJobGenerationStateFromDbToDto(
                        AppDataJobGenerationState.SENT));
    }

    /**
     * Test the conversion AppDataJobGenerationDtoState ->
     * AppDataJobGenerationDtoState
     * 
     * @throws AppCatalogJobInvalidStateException
     * @throws AppCatalogJobGenerationInvalidStateException
     */
    @Test
    public void testconvertJobGenerationStateFromDtoToDb()
            throws AppCatalogJobGenerationInvalidStateException {
        assertEquals(AppDataJobGenerationDtoState.INITIAL,
                converter.convertJobGenerationStateFromDbToDto(
                        AppDataJobGenerationState.INITIAL));
        assertEquals(AppDataJobGenerationDtoState.PRIMARY_CHECK,
                converter.convertJobGenerationStateFromDbToDto(
                        AppDataJobGenerationState.PRIMARY_CHECK));
        assertEquals(AppDataJobGenerationDtoState.READY,
                converter.convertJobGenerationStateFromDbToDto(
                        AppDataJobGenerationState.READY));
        assertEquals(AppDataJobGenerationDtoState.SENT,
                converter.convertJobGenerationStateFromDbToDto(
                        AppDataJobGenerationState.SENT));
    }
}

/**
 * Internal class for converting generic jobs for EDRS sessions
 * 
 * @author Viveris Technologies
 */
class EdrsSessionJobConverter extends JobConverter<EdrsSessionDto> {

    /**
     * Constructor
     */
    public EdrsSessionJobConverter() {
        super(ProductCategory.EDRS_SESSIONS);
    }
}

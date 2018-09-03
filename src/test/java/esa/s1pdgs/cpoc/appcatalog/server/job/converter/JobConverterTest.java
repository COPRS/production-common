package esa.s1pdgs.cpoc.appcatalog.server.job.converter;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobDto;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobDtoState;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobGenerationDtoState;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJobGenerationState;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJobState;
import esa.s1pdgs.cpoc.appcatalog.server.job.exception.AppCatalogJobGenerationInvalidStateException;
import esa.s1pdgs.cpoc.appcatalog.server.job.exception.AppCatalogJobInvalidStateException;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.mqi.model.queue.EdrsSessionDto;

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

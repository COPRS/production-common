/**
 * 
 */
package esa.s1pdgs.cpoc.ingestor.status;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import esa.s1pdgs.cpoc.common.AppState;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.ingestor.status.AppStatus.IngestorStatus;
import esa.s1pdgs.cpoc.ingestor.status.dto.IngestorStatusDto;

/**
 *
 *
 * @author Viveris Technologies
 */
public class IngestorRestControllerTest extends RestControllerTest {

    /**
     * Application status
     */
    @Mock
    protected AppStatus appStatus;

    /**
     * Controller to test
     */
    private IngestorRestController controller;

    /**
     * Initialization
     * 
     * @throws IOException
     * @throws AbstractCodedException
     */
    @Before
    public void init() throws IOException, AbstractCodedException {
        MockitoAnnotations.initMocks(this);

        controller = new IngestorRestController(appStatus);

        initMockMvc(this.controller);
    }

    /**
     * Test stop application
     * 
     * @throws Exception
     */
    @Test
    public void testUrlStopApp() throws Exception {
        doNothing().when(appStatus).setStopping();
        request(post("/app/stop"))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        verify(appStatus, times(1)).setStopping();
    }

    /**
     * Test get status when fatal error
     * 
     * @throws Exception
     */
    @Test
    public void testUrlStatusWhenFatalError() throws Exception {
        IngestorStatus status = (new AppStatus(3, 30)).getStatus();
        status.setFatalError();
        doReturn(status).when(appStatus).getStatus();

        request(get("/app/status"))
                .andExpect(
                        MockMvcResultMatchers.status().isInternalServerError())
                .andReturn();

    }

    /**
     * Test get status when fatal error
     * 
     * @throws Exception
     */
    @Test
    public void testStatusWhenFatalError() throws Exception {
        IngestorStatus status = (new AppStatus(3, 30)).getStatus();
        status.setFatalError();
        doReturn(status).when(appStatus).getStatus();

        long diffTmBefore =
                System.currentTimeMillis() - status.getDateLastChangeMs();
        ResponseEntity<IngestorStatusDto> result = controller.getStatusRest();

        assertNotNull(result.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());

        long diffTmAfter =
                System.currentTimeMillis() - status.getDateLastChangeMs();
        verify(appStatus, times(1)).getStatus();
        assertEquals(AppState.FATALERROR, result.getBody().getStatus());
        assertEquals(0, result.getBody().getErrorCounter());
        assertTrue(result.getBody().getTimeSinceLastChange() >= diffTmBefore);
        assertTrue(diffTmAfter >= result.getBody().getTimeSinceLastChange());

    }

    /**
     * Test get status when fatal error
     * 
     * @throws Exception
     */
    @Test
    public void testUrlStatusWhenError() throws Exception {
        IngestorStatus status = (new AppStatus(3, 30)).getStatus();
        status.setErrorAux(3);
        status.setErrorAux(3);
        doReturn(status).when(appStatus).getStatus();

        request(get("/app/status"))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

    }

    /**
     * Test get status when error
     * 
     * @throws Exception
     */
    @Test
    public void testStatusWhenError() throws Exception {
        IngestorStatus status = (new AppStatus(3, 30)).getStatus();
        status.setErrorAux(3);
        status.setErrorAux(3);
        doReturn(status).when(appStatus).getStatus();

        long diffTmBefore =
                System.currentTimeMillis() - status.getDateLastChangeMs();
        ResponseEntity<IngestorStatusDto> result = controller.getStatusRest();

        assertNotNull(result.getBody());
        assertEquals(HttpStatus.OK, result.getStatusCode());

        long diffTmAfter =
                System.currentTimeMillis() - status.getDateLastChangeMs();
        verify(appStatus, times(1)).getStatus();
        assertEquals(AppState.ERROR, result.getBody().getStatus());
        assertEquals(2, result.getBody().getErrorCounter());
        assertTrue(result.getBody().getTimeSinceLastChange() >= diffTmBefore);
        assertTrue(diffTmAfter >= result.getBody().getTimeSinceLastChange());
    }


}

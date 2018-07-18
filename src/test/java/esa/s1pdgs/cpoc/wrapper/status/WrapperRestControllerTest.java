package esa.s1pdgs.cpoc.wrapper.status;

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

import esa.s1pdgs.cpoc.wrapper.status.AppState;
import esa.s1pdgs.cpoc.wrapper.status.AppStatus;
import esa.s1pdgs.cpoc.wrapper.status.WrapperRestController;
import esa.s1pdgs.cpoc.wrapper.status.AppStatus.WrapperStatus;
import esa.s1pdgs.cpoc.wrapper.status.dto.WrapperStatusDto;
import esa.s1pdgs.cpoc.wrapper.test.RestControllerTest;

public class WrapperRestControllerTest extends RestControllerTest {

    /**
     * Application status
     */
    @Mock
    protected AppStatus appStatus;

    /**
     * Controller to test
     */
    private WrapperRestController controller;

    /**
     * Initialization
     * 
     * @throws IOException
     */
    @Before
    public void init() throws IOException {
        MockitoAnnotations.initMocks(this);

        controller = new WrapperRestController(appStatus);

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
        request(post("/wrapper/stop"))
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
        WrapperStatus status = (new AppStatus(3)).getStatus();
        status.setFatalError();
        doReturn(status).when(appStatus).getStatus();

        request(get("/wrapper/status"))
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
        WrapperStatus status = (new AppStatus(3)).getStatus();
        status.setFatalError();
        doReturn(status).when(appStatus).getStatus();

        long diffTmBefore =
                System.currentTimeMillis() - status.getDateLastChangeMs();
        ResponseEntity<WrapperStatusDto> result = controller.getStatusRest();

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
        WrapperStatus status = (new AppStatus(3)).getStatus();
        status.setError(3);
        status.setError(3);
        doReturn(status).when(appStatus).getStatus();

        request(get("/wrapper/status"))
                .andExpect(
                        MockMvcResultMatchers.status().isOk())
                .andReturn();

    }

    /**
     * Test get status when error
     * 
     * @throws Exception
     */
    @Test
    public void testStatusWhenError() throws Exception {
        WrapperStatus status = (new AppStatus(3)).getStatus();
        status.setError(3);
        status.setError(3);
        doReturn(status).when(appStatus).getStatus();

        long diffTmBefore =
                System.currentTimeMillis() - status.getDateLastChangeMs();
        ResponseEntity<WrapperStatusDto> result = controller.getStatusRest();

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

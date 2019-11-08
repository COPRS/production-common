package esa.s1pdgs.cpoc.appstatus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.io.IOException;
import java.util.NoSuchElementException;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import esa.s1pdgs.cpoc.appstatus.dto.AppStatusDto;
import esa.s1pdgs.cpoc.appstatus.rest.AppStatusRestController;
import esa.s1pdgs.cpoc.common.AppState;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.status.AppStatus;
import esa.s1pdgs.cpoc.status.Status;

public class AppStatusRestControllerTest {
	
    /**
     * Application status
     */
    @Mock
    protected AppStatus appStatus;

    /**
     * Controller to test
     */
    private AppStatusRestController uut;

    @Resource
	public WebApplicationContext wac;

	protected MockMvc mockMvc;

	protected ResultActions request(final MockHttpServletRequestBuilder builder) throws Exception {
		return mockMvc.perform(builder.accept(MediaType.APPLICATION_JSON_VALUE));
	}
    
    /**
     * Initialization
     * 
     * @throws IOException
     * @throws AbstractCodedException
     */
    @Before
    public void init() throws IOException, AbstractCodedException {
        MockitoAnnotations.initMocks(this);
        uut = new AppStatusRestController(appStatus);
        mockMvc = MockMvcBuilders.standaloneSetup(uut).build();
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
        Status status = new Status(3, 30);
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
        Status status = new Status(3, 30);
        status.setFatalError();
        doReturn(status).when(appStatus).getStatus();

        long diffTmBefore =
                System.currentTimeMillis() - status.getDateLastChangeMs();
        ResponseEntity<AppStatusDto> result = uut.getStatusRest();

        assertNotNull(result.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());

        long diffTmAfter =
                System.currentTimeMillis() - status.getDateLastChangeMs();
        verify(appStatus, times(1)).getStatus();
        assertEquals(AppState.FATALERROR, result.getBody().getStatus());
        assertEquals(new Integer(0), result.getBody().getErrorCounter());
        assertTrue(result.getBody().getTimeSinceLastChange() >= diffTmBefore);
        assertTrue(diffTmAfter >= result.getBody().getTimeSinceLastChange());
    }

    /**
     * Test get status when error
     * 
     * @throws Exception
     */
    @Test
    public void testUrlStatusWhenError() throws Exception {
        Status status = new Status(3, 30);
        status.incrementErrorCounterProcessing();
        status.incrementErrorCounterNextMessage();
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
        Status status = new Status(3, 30);
        status.incrementErrorCounterProcessing();
        status.incrementErrorCounterProcessing();
        doReturn(status).when(appStatus).getStatus();

        long diffTmBefore =
                System.currentTimeMillis() - status.getDateLastChangeMs();
        ResponseEntity<AppStatusDto> result = uut.getStatusRest();

        assertNotNull(result.getBody());
        assertEquals(HttpStatus.OK, result.getStatusCode());

        long diffTmAfter =
                System.currentTimeMillis() - status.getDateLastChangeMs();
        verify(appStatus, times(1)).getStatus();
        assertEquals(AppState.ERROR, result.getBody().getStatus());
        assertEquals(new Integer(2), result.getBody().getErrorCounter());
        assertTrue(result.getBody().getTimeSinceLastChange() >= diffTmBefore);
        assertTrue(diffTmAfter >= result.getBody().getTimeSinceLastChange());
    }
    
    /**
     * Test testIsProcessing
     * 
     * @throws Exception
     */
    @Test
    public void testIsProcessing() throws Exception {
        doReturn(true).when(appStatus).isProcessing(Mockito.eq("level_jobs"), Mockito.eq(1234L));
        doReturn(false).when(appStatus).isProcessing(Mockito.eq("level_jobs"), Mockito.eq(1235L));      
        doThrow(new IllegalArgumentException()).when(appStatus).isProcessing(Mockito.eq("level_jobs"), Mockito.eq(-1));
        doThrow(new NoSuchElementException()).when(appStatus).isProcessing(Mockito.eq("not_existent"), Mockito.eq(1234L));

        request(get("/app/level_jobs/process/1234"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("true"));

        request(get("/app/level_jobs/process/1235"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("false"));

        request(get("/app/level_jobs/process/-1"))
			    .andExpect(MockMvcResultMatchers.status().isOk())
			    .andExpect(MockMvcResultMatchers.content().string("false"));
        
        request(get("/app/not_existent/process/1234"))
			    .andExpect(MockMvcResultMatchers.status().isOk())
			    .andExpect(MockMvcResultMatchers.content().string("false"));

    }

}

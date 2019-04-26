package esa.s1pdgs.cpoc.mdcatalog.status;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import esa.s1pdgs.cpoc.common.AppState;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.mdcatalog.rest.RestControllerTest;
import esa.s1pdgs.cpoc.mqi.client.StatusService;

public class StatusRestControllerTest extends RestControllerTest {

	/**
	 * Application status
	 */
	@Mock
	protected AppStatus appStatus;

	/**
	 * MQI service for stopping the MQI
	 */
	@Mock
	private StatusService mqiStatusService;

	/**
	 * Controller to test
	 */
	private StatusRestController controller;

	/**
	 * Initialization
	 * 
	 * @throws IOException
	 * @throws AbstractCodedException
	 */
	@Before
	public void init() throws IOException, AbstractCodedException {
		MockitoAnnotations.initMocks(this);

		doNothing().when(mqiStatusService).stop();

		controller = new StatusRestController(appStatus);

		initMockMvc(this.controller);
	}

	/**
	 * Test get status when fatal error
	 * 
	 * @throws Exception
	 */
	@Test
	public void testUrlStatusWhenFatalError() throws Exception {
		doReturn(AppState.FATALERROR).when(appStatus).getGlobalAppState();
        doReturn(true).when(appStatus).isFatalError();

		StatusPerCategory status1 = new StatusPerCategory(ProductCategory.AUXILIARY_FILES);
		status1.setProcessing(125);
		StatusPerCategory status2 = new StatusPerCategory(ProductCategory.EDRS_SESSIONS);
		status2.setFatalError();

		Map<ProductCategory, StatusPerCategory> catStatus = new HashMap<>();
		catStatus.put(ProductCategory.AUXILIARY_FILES, status1);
		catStatus.put(ProductCategory.EDRS_SESSIONS, status2);

		doReturn(catStatus).when(appStatus).getStatus();

		request(get("/app/status")).andExpect(MockMvcResultMatchers.status().isInternalServerError())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(jsonPath("$.globalStatus", is("FATALERROR")))
				.andExpect(jsonPath("$.statusPerCategory['AUXILIARY_FILES'].status", is("PROCESSING")))
				.andExpect(jsonPath("$.statusPerCategory['EDRS_SESSIONS'].status", is("FATALERROR")));

	}

	/**
	 * Test get status when fatal error
	 * 
	 * @throws Exception
	 */
	@Test
	public void testUrlStatusWhenError() throws Exception {
		doReturn(AppState.ERROR).when(appStatus).getGlobalAppState();

		StatusPerCategory status1 = new StatusPerCategory(ProductCategory.AUXILIARY_FILES);
		status1.setProcessing(125);
		StatusPerCategory status2 = new StatusPerCategory(ProductCategory.EDRS_SESSIONS);
		status2.setErrorProcessing(3);
		status2.setErrorProcessing(3);

		Map<ProductCategory, StatusPerCategory> catStatus = new HashMap<>();
		catStatus.put(ProductCategory.AUXILIARY_FILES, status1);
		catStatus.put(ProductCategory.EDRS_SESSIONS, status2);

		doReturn(catStatus).when(appStatus).getStatus();

		request(get("/app/status")).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(jsonPath("$.globalStatus", is("ERROR")))
				.andExpect(jsonPath("$.statusPerCategory['AUXILIARY_FILES'].status", is("PROCESSING")))
				.andExpect(jsonPath("$.statusPerCategory['EDRS_SESSIONS'].status", is("ERROR")))
				.andExpect(jsonPath("$.statusPerCategory['EDRS_SESSIONS'].status", is("ERROR")));

	}

	/**
	 * Test get status when fatal error
	 * 
	 * @throws Exception
	 */
	@Test
	public void testIsProcessing() throws Exception {
		doReturn(1234L).when(appStatus).getProcessingMsgId(ProductCategory.LEVEL_JOBS);
		doReturn(526L).when(appStatus).getProcessingMsgId(ProductCategory.EDRS_SESSIONS);
		request(get("/app/level_jobs/process/1234")).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().string("true"));
		request(get("/app/edrs_sessions/process/1234")).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().string("false"));
		request(get("/app/edrs_sessions/process/526")).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().string("true"));
		request(get("/app/level_jobs/process/1235")).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().string("false"));
		doReturn(0L).when(appStatus).getProcessingMsgId(ProductCategory.LEVEL_JOBS);
		request(get("/app/level_jobs/process/1235")).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().string("false"));

	}

}

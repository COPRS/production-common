package esa.s1pdgs.cpoc.dissemination.worker.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.dissemination.worker.config.DisseminationWorkerProperties;
import esa.s1pdgs.cpoc.dissemination.worker.config.DisseminationWorkerProperties.OutboxConfiguration;
import esa.s1pdgs.cpoc.dissemination.worker.config.DisseminationWorkerProperties.OutboxConfiguration.Protocol;
import esa.s1pdgs.cpoc.dissemination.worker.config.DisseminationWorkerProperties.OutboxConnection;
import esa.s1pdgs.cpoc.dissemination.worker.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.dissemination.worker.outbox.OutboxClient;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.model.queue.DisseminationJob;
import esa.s1pdgs.cpoc.mqi.model.queue.DisseminationSource;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsServiceException;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;

public class TestDisseminationJobListener {
	
	private static final String USER = "user";
	private static final String PASS = "pass";
	private static final String PATH = "public";
	private static final int PORT = 4321;
	
	private DisseminationJobListener uut;
	
	@Mock
	private AppStatus appStatus;
	
	@Mock
	private GenericMqiClient mqiClient;
	
	@Mock
	private ErrorRepoAppender errorAppender;
	
	@Mock
	private ObsClient obsClient;
	
	

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		
		ProcessConfiguration processConfig = new ProcessConfiguration();
		DisseminationWorkerProperties config = new DisseminationWorkerProperties();
		
		OutboxConfiguration outbox = new OutboxConfiguration();
		outbox.setPort(PORT);
		outbox.setUsername(USER);
		outbox.setPassword(PASS);
		outbox.setProtocol(Protocol.FTP);
		outbox.setPath(PATH);
		
		Map<String, OutboxConfiguration> outboxConfig = new HashMap<>();
		outboxConfig.put("myocean", outbox);
		
		OutboxConnection outboxConnection = new OutboxConnection();
		outboxConnection.setOutboxName("myocean");
		outboxConnection.setMatchRegex("^test$");
		
		config.setOutboxes(outboxConfig);
		config.setOutboxConnections(Arrays.asList(outboxConnection));
		config.setPollingIntervalMs(0);
		
		uut = new DisseminationJobListener(appStatus, mqiClient, errorAppender, obsClient, processConfig, config);
	}
	
	@Test
	public final void onMessage() {
		
		final GenericMessageDto<DisseminationJob> mqiMessage = new GenericMessageDto<DisseminationJob>();
		DisseminationJob job = new DisseminationJob();
		job.setKeyObjectStorage("test");
		mqiMessage.setBody(job);
		
		try {
			uut.onMessage(mqiMessage);
		} catch (Exception e) {
			fail("Exception occurred: " + e.getMessage());
		}
	}

	@Test
	public final void findMatchingOutboxes() {
		uut.initListener();
		Map<OutboxConnection, OutboxClient> outboxes = uut.findMatchingOutboxes("test");
		
		assertNotNull(outboxes);
		assertEquals(1, outboxes.size());
	}
	
	@Test
	public final void assertExistInObs() throws ObsServiceException, SdkClientException {
		
		DisseminationSource ds1 = new DisseminationSource();
		ds1.setKeyObjectStorage("k1");
		ds1.setProductFamily(ProductFamily.L0_ACN);
		
		DisseminationSource ds2 = new DisseminationSource();
		ds2.setKeyObjectStorage("k2");
		ds2.setProductFamily(ProductFamily.L0_SLICE);
		
		doReturn(true).when(obsClient).prefixExists(any());
		
		try {
			uut.assertExistInObs(Arrays.asList(ds1, ds2));
		} catch (Exception e) {
			fail("Exception occurred: " + e.getMessage());
		}
	}
	
}

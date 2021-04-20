package esa.s1pdgs.cpoc.evictionmanagement.worker.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.evictionmanagement.worker.config.WorkerConfigurationProperties;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.MessageFilter;
import esa.s1pdgs.cpoc.mqi.client.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.client.MqiPublishingJob;
import esa.s1pdgs.cpoc.mqi.model.queue.EvictionEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.EvictionManagementJob;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;

public class EvictionManagementJobListenerTest {

	private EvictionManagementJobListener uut;
	
	@Mock
	private AppStatus appStatus;

	@Mock
	private GenericMqiClient mqiClient;

	@Mock
	private List<MessageFilter> messageFilter;

	@Mock
	private ObsClient obsClient;

	@Mock
	private ErrorRepoAppender errorAppender;

	@Mock
	private WorkerConfigurationProperties workerConfig;


	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		uut = new EvictionManagementJobListener(appStatus, mqiClient, messageFilter, obsClient, errorAppender,
				workerConfig);
	}
	
	@Test
	public void createOutputMessage() {
		
		ProductFamily family1 = ProductFamily.AUXILIARY_FILE;
		String key1 = "aux1";
		String operator1 = "operator1";
		long mId = 1;
		UUID uuid = UUID.randomUUID();
		
		
		EvictionManagementJob evictionJob = new EvictionManagementJob();
		evictionJob.setProductFamily(family1);
		evictionJob.setKeyObjectStorage(key1);
		evictionJob.setOperatorName(operator1);
		
		MqiPublishingJob<EvictionEvent> msg = uut.createOutputMessage(mId, evictionJob, uuid);
		EvictionEvent event = (EvictionEvent) msg.getMessages().get(0).getDto();
		
		assertEquals(family1, msg.getMessages().get(0).getFamily());
		assertEquals(family1, event.getProductFamily());
		assertEquals(key1, event.getKeyObjectStorage());
		assertEquals(operator1, event.getOperatorName());
		assertEquals(uuid, event.getUid());
	}
	
	@Test
	public void newMqiConsumer() {
		MqiConsumer<EvictionManagementJob> newMqiConsumer = uut.newMqiConsumer();
		assertTrue(newMqiConsumer.toString().contains(ProductCategory.EVICTION_MANAGEMENT_JOBS.toString()));
	}

}

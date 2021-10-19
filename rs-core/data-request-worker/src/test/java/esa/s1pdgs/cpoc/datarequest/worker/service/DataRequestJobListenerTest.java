package esa.s1pdgs.cpoc.datarequest.worker.service;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.datarequest.worker.config.WorkerConfigurationProperties;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.MessageFilter;
import esa.s1pdgs.cpoc.mqi.client.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.client.MqiPublishingJob;
import esa.s1pdgs.cpoc.mqi.model.queue.DataRequestEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.DataRequestJob;
import esa.s1pdgs.cpoc.mqi.model.queue.DataRequestType;
import esa.s1pdgs.cpoc.mqi.model.queue.util.CompressionEventUtil;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;

public class DataRequestJobListenerTest {

	private DataRequestJobListener uut;

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
		uut = new DataRequestJobListener(appStatus, mqiClient, messageFilter, obsClient, errorAppender, workerConfig);
	}

	@Test
	public void checkAvailableInZipBucket_1() throws Exception {

		ProductFamily family1 = ProductFamily.AUXILIARY_FILE;
		String key1 = "aux1";

		DataRequestJob dJob1 = new DataRequestJob();
		dJob1.setProductFamily(family1);
		dJob1.setKeyObjectStorage(key1);
		
		doReturn(true).when(obsClient)
				.exists(new ObsObject(CompressionEventUtil.composeCompressedProductFamily(family1),
						CompressionEventUtil.composeCompressedKeyObjectStorage(key1)));
		
		assertEquals(true, uut.checkAvailableInZipBucket(dJob1));
	}
	
	@Test
	public void checkAvailableInZipBucket_2() throws Exception {

		ProductFamily family1 = ProductFamily.AUXILIARY_FILE;
		String key2 = "aux2";

		DataRequestJob dJob2 = new DataRequestJob();
		dJob2.setProductFamily(family1);
		dJob2.setKeyObjectStorage(key2);

		doReturn(false).when(obsClient)
		.exists(new ObsObject(CompressionEventUtil.composeCompressedProductFamily(family1),
				CompressionEventUtil.composeCompressedKeyObjectStorage(key2)));
		
		assertEquals(false, uut.checkAvailableInZipBucket(dJob2));
	}
	

	@Test
	public void createOutputMessage_1() {
		
		ProductFamily family1 = ProductFamily.AUXILIARY_FILE;
		String key1 = "aux1";
		String operator1 = "operator1";
		long mId = 1;
		UUID uuid = UUID.randomUUID();
		DataRequestType type = DataRequestType.ORDER;
		
		DataRequestJob dJob1 = new DataRequestJob();
		dJob1.setProductFamily(family1);
		dJob1.setKeyObjectStorage(key1);
		dJob1.setOperatorName(operator1);
		
		MqiPublishingJob<DataRequestEvent> msg = uut.createOutputMessage(mId, dJob1, uuid, type);
		
		DataRequestEvent event = (DataRequestEvent) msg.getMessages().get(0).getDto();
		
		assertEquals(family1, msg.getMessages().get(0).getFamily());
		assertEquals(family1, event.getProductFamily());
		assertEquals(key1, event.getKeyObjectStorage());
		assertEquals(operator1, event.getOperatorName());
		assertEquals(uuid, event.getUid());
		assertEquals(type, event.getDataRequestType());
	}
	
	@Test
	public void createOutputMessage_2() {
		
		ProductFamily family1 = ProductFamily.L0_SEGMENT;
		String key1 = "l0segment";
		String operator1 = "operator1";
		long mId = 1;
		UUID uuid = UUID.randomUUID();
		DataRequestType type = DataRequestType.UNCOMPRESS;
		
		DataRequestJob dJob1 = new DataRequestJob();
		dJob1.setProductFamily(family1);
		dJob1.setKeyObjectStorage(key1);
		dJob1.setOperatorName(operator1);
		
		MqiPublishingJob<DataRequestEvent> msg = uut.createOutputMessage(mId, dJob1, uuid, type);
		
		DataRequestEvent event = (DataRequestEvent) msg.getMessages().get(0).getDto();
		
		assertEquals(family1, msg.getMessages().get(0).getFamily());
		assertEquals(family1, event.getProductFamily());
		assertEquals(key1, event.getKeyObjectStorage());
		assertEquals(operator1, event.getOperatorName());
		assertEquals(uuid, event.getUid());
		assertEquals(type, event.getDataRequestType());
	}
	
	@Test
	public void newMqiConsumer() {
		MqiConsumer<DataRequestJob> newMqiConsumer = uut.newMqiConsumer();
		assertTrue(newMqiConsumer.toString().contains(ProductCategory.DATA_REQUEST_JOBS.toString()));
	}

}

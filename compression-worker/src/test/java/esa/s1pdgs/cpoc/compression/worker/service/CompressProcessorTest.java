package esa.s1pdgs.cpoc.compression.worker.service;

import static org.junit.Assert.fail;

import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.compression.worker.config.ApplicationProperties;
import esa.s1pdgs.cpoc.compression.worker.mqi.OutputProducerFactory;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.MessageFilter;
import esa.s1pdgs.cpoc.mqi.client.StatusService;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionDirection;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionJob;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;

public class CompressProcessorTest {

	private CompressProcessor uut;

	private AppStatus appStatus;

	private ObsClient obsClient;
	private OutputProducerFactory producerFactory;
	private GenericMqiClient mqiClient;
	private List<MessageFilter> messageFilter;
	private ErrorRepoAppender errorAppender;
	private StatusService mqiStatusService;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		ApplicationProperties properties = new ApplicationProperties();
		properties.setWorkingDirectory("workindir");
		properties.setSizeBatchDownload(1000);
		properties.setTmProcAllTasksS(5);
		uut = new CompressProcessor(appStatus, properties, obsClient, producerFactory, mqiClient, messageFilter,
				errorAppender, mqiStatusService, 0, 0);
	}

	@Test
	public final void onMessage_compress() {

		CompressionJob job = new CompressionJob();
		job.setProductFamily(ProductFamily.L1_SLICE);
		job.setUid(UUID.randomUUID());
		job.setKeyObjectStorage("l1");
		job.setOutputKeyObjectStorage("l1.zip");
		job.setOutputProductFamily(ProductFamily.L0_SLICE_ZIP);
		job.setCompressionDirection(CompressionDirection.COMPRESS);
		GenericMessageDto<CompressionJob> inputMessage = new GenericMessageDto<>();
		inputMessage.setBody(job);

		try {
			uut.onMessage(inputMessage);
		} catch (Exception e) {
			fail("Exception occurred: " + e.getMessage());
		}
	}
	
	@Test
	public final void onMessage_uncompress() {

		CompressionJob job = new CompressionJob();
		job.setProductFamily(ProductFamily.L1_SLICE_ZIP);
		job.setUid(UUID.randomUUID());
		job.setKeyObjectStorage("l1.zip");
		job.setOutputKeyObjectStorage("l1");
		job.setOutputProductFamily(ProductFamily.L0_SLICE);
		job.setCompressionDirection(CompressionDirection.UNCOMPRESS);
		GenericMessageDto<CompressionJob> inputMessage = new GenericMessageDto<>();
		inputMessage.setBody(job);

		try {
			uut.onMessage(inputMessage);
		} catch (Exception e) {
			fail("Exception occurred: " + e.getMessage());
		}
	}

}

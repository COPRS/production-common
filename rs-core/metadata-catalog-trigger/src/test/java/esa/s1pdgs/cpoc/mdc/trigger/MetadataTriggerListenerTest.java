package esa.s1pdgs.cpoc.mdc.trigger;

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.databind.node.POJONode;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.mdc.trigger.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.mqi.client.MqiMessageEventHandler;
import esa.s1pdgs.cpoc.mqi.client.MqiPublishingJob;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.report.ReportingUtils;

import static org.junit.Assert.*;

public class MetadataTriggerListenerTest {

	private MetadataTriggerListener uut;

	@Mock
	private ErrorRepoAppender errorAppender;

	@Mock
	private ProcessConfiguration processConfig;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public final void onMessage() throws Exception {

		CatalogJobMapper<IngestionEvent> mapper = (e, r) -> new CatalogJob();

		uut = new MetadataTriggerListener(mapper, errorAppender, processConfig);

		IngestionEvent event = new IngestionEvent(ProductFamily.AUXILIARY_FILE, "key", "path", 0, "station", null,
				"NRT");
		GenericMessageDto<IngestionEvent> message = new GenericMessageDto<>();
		message.setBody(event);
		try {
			uut.onMessage(message);
		} catch (Exception e) {
			fail("Exception occurred: " + e.getMessage());
		}
	}

	@Test
	public final void newPublicationMessage() {
		CatalogJobMapper<IngestionEvent> mapper = (e, r) -> {
			CatalogJob job = new CatalogJob();
			job.setProductFamily(e.getProductFamily());
			job.setKeyObjectStorage(e.getKeyObjectStorage());
			job.setStationName(e.getStationName());
			return job;
		};
		uut = new MetadataTriggerListener(mapper, errorAppender, processConfig);
		IngestionEvent event = new IngestionEvent(ProductFamily.AUXILIARY_FILE, "key", "path", 0, "station", null,
				"NRT");
		GenericMessageDto<IngestionEvent> message = new GenericMessageDto<>();
		message.setBody(event);
		MqiPublishingJob<CatalogJob> outputMessage = uut.newPublicationMessage(
				ReportingUtils.newReportingBuilder(MissionId.UNDEFINED).newReporting("newPublicationMessageTest"), message);

		CatalogJob job = (CatalogJob) outputMessage.getMessages().get(0).getDto();
		assertEquals(ProductFamily.AUXILIARY_FILE, job.getProductFamily());
		assertEquals("key", event.getKeyObjectStorage());
		assertEquals("station", event.getStationName());

	}

}

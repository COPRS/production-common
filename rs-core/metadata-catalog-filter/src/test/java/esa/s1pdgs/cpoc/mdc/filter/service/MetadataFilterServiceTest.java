package esa.s1pdgs.cpoc.mdc.filter.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.springframework.messaging.support.MessageBuilder;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionEvent;
import esa.s1pdgs.cpoc.report.ReportingUtils;

public class MetadataFilterServiceTest {
	
	private MetadataFilterService uut;

	@Test
	public final void onMessage() throws Exception {
		uut = new MetadataFilterService();

		IngestionEvent event = new IngestionEvent(ProductFamily.AUXILIARY_FILE, "S1key", "path", 0, "S1", "station", null,
				"NRT");
		try {
			uut.apply(MessageBuilder.withPayload(event).build());
		} catch (Exception e) {
			fail("Exception occurred: " + e.getMessage());
		}
	}

	@Test
	public final void newPublicationMessage() {
		uut = new MetadataFilterService();
		IngestionEvent event = new IngestionEvent(ProductFamily.AUXILIARY_FILE, "S1key", "path", 0, "S1", "station", null,
				"NRT");
		CatalogJob job = uut.newPublicationMessage(
				ReportingUtils.newReportingBuilder(MissionId.UNDEFINED).newReporting("newPublicationMessageTest"), event);

		assertEquals(ProductFamily.AUXILIARY_FILE, job.getProductFamily());
		assertEquals("S1key", event.getKeyObjectStorage());
		assertEquals("S1", event.getMissionId());
		assertEquals("station", event.getStationName());
	}
}

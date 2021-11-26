package esa.s1pdgs.cpoc.ingestion.filter.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.ParseException;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ingestion.filter.config.IngestionFilterConfigurationProperties;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.MessageFilter;
import esa.s1pdgs.cpoc.mqi.client.MqiPublishingJob;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestIngestionFilterService {

	@Autowired
	private IngestionFilterConfigurationProperties properties;

	@Mock
	private GenericMqiClient mqiClient;

	@Mock
	private List<MessageFilter> messageFilter;

	@Mock
	private AppStatus appStatus;	
	
	@Test
	public final void testAcceptCase() throws ParseException {
		IngestionFilterService service = new IngestionFilterService(mqiClient, messageFilter, appStatus, properties);

		String productName = "DCS_01_S3A_2021112408000009000_dat/ch_1/DCS_01_S3A_2021112408000009000_ch1_DSDB_00001.raw";
		
		IngestionJob job = new IngestionJob(ProductFamily.EDRS_SESSION,
				productName,
				"pickupBase",
				"WILE/S3A/7000/DCS_01_S3A_2021112408000009000_dat/ch_1/DCS_01_S3A_2021112408000009000_ch1_DSDB_00001.raw",
				179, UUID.randomUUID(), "S3", "TEST", null, null, "xbip", null);
		GenericMessageDto<IngestionJob> message = new GenericMessageDto<IngestionJob>(0, "t-pdgs-whatevertest", job);
		
		MqiPublishingJob<IngestionJob> result = service.handleMessage(message, MissionId.S3, productName);
		
		assertEquals(1, result.getMessages().size());
	}
	
	@Test
	public final void testRejectCase() throws ParseException {
		IngestionFilterService service = new IngestionFilterService(mqiClient, messageFilter, appStatus, properties);

		String productName = "DCS_01_S3A_2021112409000009000_dat/ch_1/DCS_01_S3A_2021112409000009000_ch1_DSDB_00001.raw";
		
		IngestionJob job = new IngestionJob(ProductFamily.EDRS_SESSION,
				productName,
				"pickupBase",
				"WILE/S3A/7000/DCS_01_S3A_2021112409000009000_dat/ch_1/DCS_01_S3A_2021112409000009000_ch1_DSDB_00001.raw",
				179, UUID.randomUUID(), "S3", "TEST", null, null, "xbip", null);
		GenericMessageDto<IngestionJob> message = new GenericMessageDto<IngestionJob>(0, "t-pdgs-whatevertest", job);
		
		MqiPublishingJob<IngestionJob> result = service.handleMessage(message, MissionId.S3, productName);
		
		assertEquals(0, result.getMessages().size());
	}
}

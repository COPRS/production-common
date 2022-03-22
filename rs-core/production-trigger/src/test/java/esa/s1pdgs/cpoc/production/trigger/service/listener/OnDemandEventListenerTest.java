package esa.s1pdgs.cpoc.production.trigger.service.listener;

import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.OnDemandEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.production.trigger.config.ProcessSettings;
import esa.s1pdgs.cpoc.production.trigger.service.PreparationJobPublishMessageProducer;
import esa.s1pdgs.cpoc.production.trigger.taskTableMapping.TasktableMapper;

public class OnDemandEventListenerTest {
	
	private OnDemandEventListener uut;
	
	@Mock
	private TasktableMapper taskTableMapper;

	@Mock
	private ErrorRepoAppender errorRepoAppender;

	@Mock
	private MetadataClient metadataClient;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		ProcessSettings processSettings = new ProcessSettings();
		processSettings.setL0EwSlcTaskTableName("ttname");
		Pattern seaCoverageCheckPattern = Pattern.compile("oversea");
		Pattern l0EwSlcCheckPattern = Pattern.compile("l0ewslc");
		PreparationJobPublishMessageProducer pubMessageProducer = new PreparationJobPublishMessageProducer(
				processSettings, seaCoverageCheckPattern, l0EwSlcCheckPattern, metadataClient);
		uut = new OnDemandEventListener(taskTableMapper, "hostname", errorRepoAppender, pubMessageProducer);
	}
	
	@Test
	public final void onMessage() {

		final GenericMessageDto<OnDemandEvent> mqiMessage = new GenericMessageDto<OnDemandEvent>();
		OnDemandEvent event = new OnDemandEvent();
		event.setProductFamily(ProductFamily.L0_SLICE);
		event.setKeyObjectStorage("l0slice");
		event.setProductName("l0slice");
		Map<String, Object> metadata = new HashMap<>();
		metadata.put("startTime", "2017-12-13T14:59:48.123456Z");
		metadata.put("stopTime", "2017-12-13T15:17:25.142536Z");
		metadata.put("productType", "type");
		metadata.put("missionId", "S3");
		event.setMetadata(metadata);
		mqiMessage.setBody(event);

		try {
			uut.onMessage(mqiMessage);
		} catch (Exception e) {
			fail("Exception occurred: " + e.getMessage());
		}

	}

}

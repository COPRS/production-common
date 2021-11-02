package esa.s1pdgs.cpoc.prip.trigger.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.MessageFilter;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

public class IngestionEventListenerTest {
	
	private IngestionEventListener uut;
	
	@Mock
	private GenericMqiClient mqiClient;
	
	@Mock
	private List<MessageFilter> messageFilter;
	
	@Mock
	private AppStatus appStatus;
	
	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		uut = new IngestionEventListener(mqiClient, messageFilter, 0, 0, appStatus);
	}
	
	@Test
	public final void onMessage() {
		
		IngestionEvent event = new IngestionEvent(ProductFamily.L2_SLICE, "S1key", "path", 10, "stationName", "mode", "PT");
		GenericMessageDto<IngestionEvent> inputMessage = new GenericMessageDto<>();
		inputMessage.setBody(event);
		
		try {
			uut.onMessage(inputMessage);
		} catch (Exception e) {
			fail("Exception occurred: " + e.getMessage());
		}
		
	}

}

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
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionDirection;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

public class CompressionEventListenerTest {
	
	private CompressionEventListener uut;
	
	@Mock
	private GenericMqiClient mqiClient;
	
	@Mock
	private List<MessageFilter> messageFilter;
	
	@Mock
	private AppStatus appStatus;

	
	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		uut = new CompressionEventListener(mqiClient, messageFilter, 1000, 1000, appStatus);
	}
	
	@Test
	public final void onMessage_Compress() {
		
		CompressionEvent event = new CompressionEvent(ProductFamily.L1_ACN, "key", CompressionDirection.COMPRESS);
		GenericMessageDto<CompressionEvent> inputMessage = new GenericMessageDto<>();
		inputMessage.setBody(event);
		
		try {
			uut.onMessage(inputMessage);
		} catch (Exception e) {
			fail("Exception occurred: " + e.getMessage());
		}
		
	}
	
	@Test
	public final void onMessage_Uncompress() {
		
		CompressionEvent event = new CompressionEvent(ProductFamily.L1_ACN_ZIP, "key.zip", CompressionDirection.UNCOMPRESS);
		GenericMessageDto<CompressionEvent> inputMessage = new GenericMessageDto<>();
		inputMessage.setBody(event);
		
		try {
			uut.onMessage(inputMessage);
		} catch (Exception e) {
			fail("Exception occurred: " + e.getMessage());
		}
		
	}
	
	
}

package esa.s1pdgs.cpoc.dissemination.trigger.service;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.MessageFilter;
import esa.s1pdgs.cpoc.mqi.client.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

public class ProductionEventListenerTest {
	
	private ProductionEventListener uut;
	
	@Mock
	private GenericMqiClient mqiClient;
	
	@Mock
	private List<MessageFilter> messageFilter;
	
	@Mock
	private MetadataClient metadataClient;
	
	@Mock
	private AppStatus appStatus;
	
	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		uut = new ProductionEventListener(mqiClient, messageFilter, metadataClient, "", 1000, 1000, appStatus);
	}
	
	@Test
	public final void onMessage() throws AbstractCodedException {
		
		ProductionEvent event = new ProductionEvent();
		event.setProductFamily(ProductFamily.L0_SEGMENT);
		event.setKeyObjectStorage("key");
		GenericMessageDto<ProductionEvent> inputMessage = new GenericMessageDto<>();
		inputMessage.setBody(event);
		
//		doReturn(inputMessage).when(mqiClient).next(ProductCategory.LEVEL_PRODUCTS);
		
//		MqiConsumer<ProductionEvent> mqiConsumer = new MqiConsumer<ProductionEvent>(mqiClient, ProductCategory.LEVEL_PRODUCTS, uut, messageFilter,
//				1000, 1000, appStatus);
//		
//		mqiConsumer.run();
//		
		try {
			uut.onMessage(inputMessage);
		} catch (Exception e) {
			fail("Exception occurred: " + e.getMessage());
		}
	}
	
	
}

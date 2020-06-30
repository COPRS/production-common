package esa.s1pdgs.cpoc.mqi.client;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiAckApiError;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.Ack;
import esa.s1pdgs.cpoc.mqi.model.rest.AckMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

public class TestMqiConsumer {	
	@Mock
	private MqiClient fakeClient;
	
	@Mock
	private MqiListener<ProductionEvent> fakeListener;
	
	@Mock
	private AppStatus fakeappStatus;
	
	private final ProductionEvent fakeDto = new ProductionEvent("1", "2", ProductFamily.AUXILIARY_FILE);
	private final GenericMessageDto<ProductionEvent> mess = new GenericMessageDto<ProductionEvent>(1, "test", fakeDto);
	
	@Before
	public final void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public final void testOnNominalHandling_ShallAcknowledgePostively() throws Exception {
		// let it only poll once
		doReturn(true).when(fakeappStatus).isInterrupted();
		doReturn(mess).when(fakeClient).next(Mockito.eq(ProductCategory.AUXILIARY_FILES));
		
		final MqiConsumer<?> uut = new MqiConsumer<>(
				fakeClient, 
				ProductCategory.AUXILIARY_FILES, 
				fakeListener, 
				1000L,
				0L,
				fakeappStatus
		);		
		uut.run();		
		verify(fakeClient).ack(Mockito.eq(new AckMessageDto(1, Ack.OK, null, false)), Mockito.eq(ProductCategory.AUXILIARY_FILES));
	}
	
	@Test
	public final void testOnListenerException_ShallAcknowledgeNegatively() throws Exception {
		// let it only poll once
		doReturn(true).when(fakeappStatus).isInterrupted();
		doReturn(mess).when(fakeClient).next(Mockito.eq(ProductCategory.AUXILIARY_FILES));
		final 	ArgumentCaptor<AckMessageDto> ackArgument = ArgumentCaptor.forClass(AckMessageDto.class);
		doThrow(new RuntimeException("Expected")).when(fakeListener).onMessage(Mockito.any());
		
		final MqiConsumer<?> uut = new MqiConsumer<>(
				fakeClient, 
				ProductCategory.AUXILIARY_FILES, 
				fakeListener, 
				1000L,
				0L,
				fakeappStatus
		);		
		uut.run();		
		verify(fakeClient).ack(ackArgument.capture(),Mockito.eq(ProductCategory.AUXILIARY_FILES));
		assertEquals(Ack.ERROR, ackArgument.getValue().getAck());

	}
	
	@Test
	public final void testWithInitialDelayAndImmediateInterruption() throws Exception {		
		doThrow(new InterruptedException()).when(fakeappStatus).sleep(Mockito.anyLong());		
		final MqiConsumer<?> uut = new MqiConsumer<>(
				fakeClient, 
				ProductCategory.AUXILIARY_FILES, 
				fakeListener, 
				1000L,
				1L,
				fakeappStatus
		);		
		uut.run();		
		verify(fakeappStatus).sleep(Mockito.anyLong());
	}
	
	@Test
	public final void testShutdownBeforePolling() throws Exception {		
		doReturn(true).when(fakeappStatus).isShallBeStopped();
		final MqiConsumer<?> uut = new MqiConsumer<>(
				fakeClient, 
				ProductCategory.AUXILIARY_FILES, 
				fakeListener, 
				1000L,
				1L,
				fakeappStatus
		);		
		uut.run();	
		verify(fakeappStatus).forceStopping();
	}
	
	@Test
	public final void testOnMessageNull_ShallContinuePolling() throws Exception {		
		doReturn(null, mess).when(fakeClient).next(Mockito.eq(ProductCategory.AUXILIARY_FILES));
		doReturn(false, true).when(fakeappStatus).isInterrupted();
		final MqiConsumer<?> uut = new MqiConsumer<>(
				fakeClient, 
				ProductCategory.AUXILIARY_FILES, 
				fakeListener, 
				1000L,
				0L,
				fakeappStatus
		);		
		uut.run();	
		verify(fakeappStatus).sleep(Mockito.anyLong());		
	}
	
	@Test
	public final void testOnMessageBodyNull_ShallContinuePolling() throws Exception {		
		doReturn(new GenericMessageDto<>(2, "test", null), mess).when(fakeClient).next(Mockito.eq(ProductCategory.AUXILIARY_FILES));
		doReturn(false, true).when(fakeappStatus).isInterrupted();
		final MqiConsumer<?> uut = new MqiConsumer<>(
				fakeClient, 
				ProductCategory.AUXILIARY_FILES, 
				fakeListener, 
				1000L,
				0L,
				fakeappStatus
		);		
		uut.run();	
		verify(fakeappStatus).sleep(Mockito.anyLong());		
	}
	
	@Test
	public final void testOnAckError_ShallSetAppStatusToError() throws Exception {	
		doReturn(true).when(fakeappStatus).isInterrupted();
		doReturn(mess).when(fakeClient).next(Mockito.eq(ProductCategory.AUXILIARY_FILES));
		doThrow(new MqiAckApiError(ProductCategory.AUXILIARY_FILES,1, "testAckError","HTTP status code "))
			.when(fakeClient).ack(Mockito.any(), Mockito.eq(ProductCategory.AUXILIARY_FILES));
		
		final MqiConsumer<?> uut = new MqiConsumer<>(
				fakeClient, 
				ProductCategory.AUXILIARY_FILES, 
				fakeListener, 
				1000L,
				0L,
				fakeappStatus
		);		
		uut.run();	
		verify(fakeappStatus, times(1)).setError(Mockito.anyString());
	}
	
	@Test
	public final void testInterruption_ShallExitLoop() throws Exception {	
		doReturn(mess).when(fakeClient).next(Mockito.eq(ProductCategory.AUXILIARY_FILES));
		doThrow(new InterruptedException()).when(fakeappStatus).sleep(Mockito.anyLong());
		
		final MqiConsumer<?> uut = new MqiConsumer<>(
				fakeClient, 
				ProductCategory.AUXILIARY_FILES, 
				fakeListener, 
				1000L,
				0L,
				fakeappStatus
		);		
		uut.run();	
	}
	
	@Test
	public final void testAllowConsumption() {
		
		final MqiMessageFilter filter1 = new MqiMessageFilter();
		filter1.setDisallowRegex("a.*");
		filter1.setProductFamily(ProductFamily.AUXILIARY_FILE);
		
		final MqiMessageFilter filter2 = new MqiMessageFilter();
		filter2.setDisallowRegex("s.*");
		filter2.setProductFamily(ProductFamily.EDRS_SESSION);
		
		final List<MqiMessageFilter> mqiMessageFilter = new ArrayList<>();
		mqiMessageFilter.add(filter1);
		mqiMessageFilter.add(filter2);		
		
		final MqiConsumer<ProductionEvent> uut = new MqiConsumer<>(
				fakeClient, 
				ProductCategory.AUXILIARY_FILES, 
				fakeListener,
				mqiMessageFilter,
				1000L,
				0L,
				fakeappStatus
		);
		
		final ProductionEvent body1 = new ProductionEvent(); 
		body1.setKeyObjectStorage("auxfoo");
		body1.setProductFamily(ProductFamily.AUXILIARY_FILE);
		final GenericMessageDto<ProductionEvent> message1 = new GenericMessageDto<>();
		message1.setBody(body1);
		
		Assert.assertFalse(uut.allowConsumption(message1));
		
		final ProductionEvent body2 = new ProductionEvent(); 
		body2.setKeyObjectStorage("session");
		body2.setProductFamily(ProductFamily.EDRS_SESSION);
		final GenericMessageDto<ProductionEvent> message2 = new GenericMessageDto<>();
		message2.setBody(body2);
		
		Assert.assertFalse(uut.allowConsumption(message2));
		
		final ProductionEvent body3 = new ProductionEvent(); 
		body3.setKeyObjectStorage("notexpectedtomatch");
		body3.setProductFamily(ProductFamily.EDRS_SESSION);
		final GenericMessageDto<ProductionEvent> message3 = new GenericMessageDto<>();
		message3.setBody(body3);
		
		Assert.assertTrue(uut.allowConsumption(message3));
		
		final ProductionEvent body4 = new ProductionEvent(); 
		body4.setKeyObjectStorage("l1foo");
		body4.setProductFamily(ProductFamily.L1_SLICE);
		final GenericMessageDto<ProductionEvent> message4 = new GenericMessageDto<>();
		message4.setBody(body4);
		
		Assert.assertTrue(uut.allowConsumption(message4));		
	}
}

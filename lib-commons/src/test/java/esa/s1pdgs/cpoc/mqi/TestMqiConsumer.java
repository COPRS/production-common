package esa.s1pdgs.cpoc.mqi;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiAckApiError;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;
import esa.s1pdgs.cpoc.mqi.model.rest.Ack;
import esa.s1pdgs.cpoc.mqi.model.rest.AckMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.status.AppStatus;

public class TestMqiConsumer {	
	@Mock
	private MqiClient fakeClient;
	
	@Mock
	private MqiListener<ProductDto> fakeListener;
	
	@Mock
	private AppStatus fakeappStatus;
	
	private final ProductDto fakeDto = new ProductDto("1", "2", ProductFamily.AUXILIARY_FILE);
	private final GenericMessageDto<ProductDto> mess = new GenericMessageDto<ProductDto>(1, "test", fakeDto);
	
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
	

}

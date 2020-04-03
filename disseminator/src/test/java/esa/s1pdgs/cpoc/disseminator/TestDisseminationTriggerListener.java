package esa.s1pdgs.cpoc.disseminator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.amazonaws.SdkClientException;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.obs.ObsException;
import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties;
import esa.s1pdgs.cpoc.disseminator.outbox.OutboxClient;
import esa.s1pdgs.cpoc.disseminator.service.DisseminationException;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsServiceException;
import esa.s1pdgs.cpoc.report.ReportingFactory;

public class TestDisseminationTriggerListener {
	@Test
	public final void testAssertExists_OnNonExistingFile_ShallThrowException() throws ObsException, ObsServiceException, esa.s1pdgs.cpoc.obs_sdk.SdkClientException {
		final FakeObsClient fakeObsClient = new FakeObsClient() {
			@Override public final boolean prefixExists(final ObsObject object) throws SdkClientException, ObsServiceException {
				return false;
			}			
		};		
		final DisseminationTriggerListener<ProductionEvent> uut = new DisseminationTriggerListener<>(fakeObsClient, new DisseminationProperties(), ErrorRepoAppender.NULL);
		final ProductionEvent fakeProduct = new ProductionEvent("fakeProduct", "my/key", ProductFamily.BLANK);
		
		try {
			uut.assertExists(fakeProduct);
			fail();
		} catch (final DisseminationException e) {
			assertEquals("OBS file 'my/key' (BLANK) does not exist", e.getMessage());
		}	
	}
	
	@Test
	public final void testAssertExists_OnExistingFile_ShallNotFail() throws ObsException, ObsServiceException, esa.s1pdgs.cpoc.obs_sdk.SdkClientException {
		final FakeObsClient fakeObsClient = new FakeObsClient() {
			@Override public final boolean prefixExists(final ObsObject object) throws SdkClientException, ObsServiceException {
				return true;
			}			
		};		
		final DisseminationTriggerListener<ProductionEvent> uut = new DisseminationTriggerListener<>(fakeObsClient, new DisseminationProperties(), ErrorRepoAppender.NULL);
		final ProductionEvent fakeProduct = new ProductionEvent("fakeProduct", "my/key", ProductFamily.BLANK);
		uut.assertExists(fakeProduct);
	}
	
	@Test
	public final void testClientForTarget_OnValidTarget_ShallReturnClientForTarget() {
		final DisseminationTriggerListener<ProductionEvent> uut = new DisseminationTriggerListener<>(null, new DisseminationProperties(), ErrorRepoAppender.NULL);
		uut.put("foo", OutboxClient.NULL);		
		assertEquals(OutboxClient.NULL, uut.clientForTarget("foo"));		
	}
	
	@Test
	public final void testClientForTarget_OnInvalidTarget_ShallThrowException() {
		final DisseminationTriggerListener<ProductionEvent> uut = new DisseminationTriggerListener<>(null, new DisseminationProperties(), ErrorRepoAppender.NULL);
		uut.put("foo", OutboxClient.NULL);		
		try {
			uut.clientForTarget("bar");
			fail();
		} catch (final DisseminationException e) {
			assertEquals("No outbox configured for 'bar'. Available are: [foo]", e.getMessage());
		}		
	}
	
	@Test
	public final void testHandleTransferTo_OnSuccessfulTransfer_ShallNotFail() {
		final FakeObsClient fakeObsClient = new FakeObsClient() {
			@Override public final boolean prefixExists(final ObsObject object) throws SdkClientException, ObsServiceException {
				return true;
			}			
		};		
		final DisseminationTriggerListener<ProductionEvent> uut = new DisseminationTriggerListener<>(fakeObsClient, new DisseminationProperties(), ErrorRepoAppender.NULL);
		uut.put("foo", OutboxClient.NULL);		
		final ProductionEvent fakeProduct = new ProductionEvent("fakeProduct", "my/key", ProductFamily.BLANK);
		final GenericMessageDto<ProductionEvent> fakeMessage = new GenericMessageDto<ProductionEvent>(123, "myKey", fakeProduct); 
		uut.handleTransferTo(fakeMessage, "foo");
	}
	
	@Test
	public final void testHandleTransferTo_OnTransferError_ShallThrowException() {
		final FakeObsClient fakeObsClient = new FakeObsClient() {
			@Override public final boolean prefixExists(final ObsObject object) throws SdkClientException, ObsServiceException {
				return true;
			}			
		};		
		final OutboxClient failOuboxClient = new OutboxClient() {			
			@Override
			public String transfer(final ObsObject obsObjext, final ReportingFactory reportingFactory) throws SdkClientException, ObsException {
				throw new SdkClientException("EXPECTED");
			}
		};
		
		final DisseminationTriggerListener<ProductionEvent> uut = new DisseminationTriggerListener<>(fakeObsClient, new DisseminationProperties(), ErrorRepoAppender.NULL);
		uut.put("foo", failOuboxClient);		
		final ProductionEvent fakeProduct = new ProductionEvent("fakeProduct", "my/key", ProductFamily.BLANK);
		final GenericMessageDto<ProductionEvent> fakeMessage = new GenericMessageDto<ProductionEvent>(123, "myKey", fakeProduct); 
		try {
			uut.handleTransferTo(fakeMessage, "foo");
		} catch (final Exception e) {
			assertEquals(true, e.getMessage().startsWith("Error on dissemination of product to outbox foo"));
		}
	}
}

package esa.s1pdgs.cpoc.reqrepo.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Sort;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessing;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.reqrepo.config.RequestRepositoryConfiguration;
import esa.s1pdgs.cpoc.reqrepo.repo.FailedProcessingRepo;

public class RequestRepositoryTest {
	private static final List<String> PROCESSING_TYPES_LIST = Arrays.asList("foo","bar");
	
	private final RequestRepositoryConfiguration config = new RequestRepositoryConfiguration("foo bar");
		
	
	@Mock
	private FailedProcessingRepo failedProcessingRepo;

	
	private RequestRepository uut;
		

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		this.uut = new RequestRepositoryImpl(
				failedProcessingRepo,
				config
		);
	}

	
	@Test
	public void testGetFailedProcessings_OnInvocation_ShallReturnAllElements() {
		doReturn(Arrays.asList(newFailedProcessing("123"), newFailedProcessing("456")))
			.when(failedProcessingRepo)
			.findAll(Mockito.any(Sort.class));

		final List<FailedProcessing> actual = uut.getFailedProcessings();
		
		assertEquals(2, actual.size());
		assertEquals("123", actual.get(0).getId());
		assertEquals("456", actual.get(1).getId());
	}

	@Test
	public void testGetFailedProcessingById_OnExistingId_ShallReturnProduct() {
		doReturn(Optional.of(newFailedProcessing("123")))
			.when(failedProcessingRepo)
			.findById("123");

		final FailedProcessing actual = uut.getFailedProcessingById("123");
		
		assertNotNull(actual);
		assertEquals("123", actual.getId());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetFailedProcessingById_OnNonExistingId_ShallReturnNull() {
		doReturn(Optional.empty())
			.when(failedProcessingRepo)
			.findById("456");
		
		uut.getFailedProcessingById("456");
	}

	@Test
	public void testDeleteFailedProcessing_OnExistingId_ShallDeleteElement() {
		doReturn(Optional.of(newFailedProcessing("123")))
			.when(failedProcessingRepo)
			.findById("123");

		uut.deleteFailedProcessing("123");

		verify(failedProcessingRepo).deleteById("123");
	}

	@Test(expected=IllegalArgumentException.class)
	public void testDeleteFailedProcessing_OnNonExistingId_ShallThrowException() {
		doReturn(Optional.empty())
			.when(failedProcessingRepo)
			.findById("456");
		
		uut.deleteFailedProcessing("456");
	}

	// FIXME: Remove @Ignore
	@Ignore
	@Test
	public void testRestartAndDeleteFailedProcessing_OnExistingTopicAndRequest_ShallResubmitAndDelete() {	
		final FailedProcessing fp = newFailedProcessing("123", new CatalogJob("f","b", ProductFamily.AUXILIARY_FILE)); 
		doReturn(Optional.of(fp))
			.when(failedProcessingRepo)
			.findById("123");

		uut.restartAndDeleteFailedProcessing("123");
		
		verify(failedProcessingRepo, times(1)).findById("123");
		verify(failedProcessingRepo, times(1)).deleteById("123");
		
		// FIXME: Update when replacement for messageProducer is available
		//verify(messageProducer, times(1)).send(fp.getTopic(), fp.getDto());
	}

	@Test(expected = RuntimeException.class)
	public void testRestartAndDeleteFailedProcessing_OnTopicNull_ShallThrowException() {		
		final FailedProcessing fp = newFailedProcessing("456", new CatalogJob("f","b", ProductFamily.AUXILIARY_FILE));
		fp.setTopic(null);

		doReturn(Optional.of(fp))
			.when(failedProcessingRepo)
			.findById("456");
		
		uut.restartAndDeleteFailedProcessing("456");
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testRestartAndDeleteFailedProcessing_OnMissingRequest_ShallThrowException() {
		doReturn(Optional.empty())
			.when(failedProcessingRepo)
			.findById("789");

		uut.restartAndDeleteFailedProcessing("789");
	}
	
	@Test
	public final void testGetProcessingTypes_OnInvocation_ShallReturnProcessingTypes() {
		assertEquals(PROCESSING_TYPES_LIST, uut.getProcessingTypes());
	}
	
	private FailedProcessing newFailedProcessing(final String id) {
		return newFailedProcessing(id, new CatalogJob());
	}
	
	private FailedProcessing newFailedProcessing(final String id, final AbstractMessage mess) {
		final FailedProcessing failedProcessing = new FailedProcessing();
		failedProcessing.setId(id);
		failedProcessing.setMessage("foobar");
		failedProcessing.setTopic("myTopic");
		return failedProcessing;
	}
	
}

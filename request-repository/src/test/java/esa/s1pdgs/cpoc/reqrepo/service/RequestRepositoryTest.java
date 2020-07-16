package esa.s1pdgs.cpoc.reqrepo.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import esa.s1pdgs.cpoc.appcatalog.common.FailedProcessing;
import esa.s1pdgs.cpoc.appcatalog.common.MqiMessage;
import esa.s1pdgs.cpoc.appcatalog.common.Processing;
import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.MessageState;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.reqrepo.kafka.producer.SubmissionClient;
import esa.s1pdgs.cpoc.reqrepo.repo.FailedProcessingRepo;
import esa.s1pdgs.cpoc.reqrepo.repo.MqiMessageRepo;

public class RequestRepositoryTest {
	@Mock
	private FailedProcessingRepo failedProcessingRepo;
	@Mock
	private MqiMessageRepo mqiMessageRepository;	
	@Mock
	private SubmissionClient submissionClient;

	private RequestRepository uut;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		this.uut = new RequestRepositoryImpl(
				mqiMessageRepository, 
				failedProcessingRepo, 
				submissionClient,
				AppStatus.NULL
		);
	}

	
	@Test
	public void testGetFailedProcessings_OnInvocation_ShallReturnAllElements() {
		doReturn(Arrays.asList(newFailedProcessing(123), newFailedProcessing(456)))
			.when(failedProcessingRepo)
			.findAll(Mockito.any(Sort.class));

		final List<FailedProcessing> actual = uut.getFailedProcessings();
		
		assertEquals(2, actual.size());
		assertEquals(123, actual.get(0).getId());
	}

	@Test
	public void testGetFailedProcessingById_OnExistingId_ShallReturnProduct() {
		doReturn(newFailedProcessing(123))
			.when(failedProcessingRepo)
			.findById(123);

		final FailedProcessing actual = uut.getFailedProcessingById(123);
		
		assertNotNull(actual);
		assertEquals(123, actual.getId());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetFailedProcessingById_OnNonExistingId_ShallReturnNull() {
		doReturn(null)
			.when(failedProcessingRepo)
			.findById(456);
		
		uut.getFailedProcessingById(456);
	}

	@Test
	public void testDeleteFailedProcessing_OnExistingId_ShallDeleteElement() {
		doReturn(newFailedProcessing(123))
			.when(failedProcessingRepo)
			.findById(123);

		uut.deleteFailedProcessing(123);

		verify(failedProcessingRepo).deleteById(123L);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testDeleteFailedProcessing_OnNonExistingId_ShallThrowException() {
		doReturn(null)
			.when(failedProcessingRepo)
			.findById(456);
		
		uut.deleteFailedProcessing(456);
	}
	
	@Test
	public void testSaveFailedProcessing_OnExistingMessage_ShallPersistFailedProcessing() {
		doReturn(newMqiMessage(456))
			.when(mqiMessageRepository)
			.findById(456);
		
		uut.saveFailedProcessing(newFailedProcessingDto(456));
		
		verify(mqiMessageRepository, times(1)).findById(456);
		verify(failedProcessingRepo, times(1)).save(Mockito.any());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSaveFailedProcessing_OnMissingMessage_ShallThrowException() {
		doReturn(null)
			.when(failedProcessingRepo)
			.findById(123);
		
		uut.saveFailedProcessing(newFailedProcessingDto(123));
	}

	@Test
	public void testRestartAndDeleteFailedProcessing_OnExistingTopicAndRequest_ShallResubmitAndDelete() {	
		final FailedProcessing fp = newFailedProcessing(123, new ProductionEvent("f","b", ProductFamily.AUXILIARY_FILE)); 
		doReturn(fp)
			.when(failedProcessingRepo)
			.findById(123);

		uut.restartAndDeleteFailedProcessing(123);
		
		verify(failedProcessingRepo, times(1)).findById(123);
		verify(failedProcessingRepo, times(1)).deleteById(123);
		verify(submissionClient, times(1)).resubmit(fp, fp.getDto(), AppStatus.NULL);
	}

	@Test(expected = RuntimeException.class)
	public void testRestartAndDeleteFailedProcessing_OnTopicNull_ShallThrowException() {		
		final FailedProcessing fp = newFailedProcessing(456, new ProductionEvent("f","b", ProductFamily.AUXILIARY_FILE));
		fp.setTopic(null);

		doReturn(fp)
			.when(failedProcessingRepo)
			.findById(456);
		
		uut.restartAndDeleteFailedProcessing(456);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testRestartAndDeleteFailedProcessing_OnMissingRequest_ShallThrowException() {
		doReturn(null)
			.when(failedProcessingRepo)
			.findById(789);

		uut.restartAndDeleteFailedProcessing(789);
	}
	
	@Test
	public final void testGetProcessingTypes_OnInvocation_ShallReturnProcessingTypes() {
		assertEquals(RequestRepository.PROCESSING_TYPES_LIST, uut.getProcessingTypes());
	}
	
	@Test
	public final void testGetProcessingById_OnExistingId_ShallReturnElement()
	{
		doReturn(newMqiMessage(123))
			.when(mqiMessageRepository)
			.findById(123);
		
		final Processing actual = uut.getProcessing(123);
		assertNotNull(actual);
		assertEquals(123, actual.getIdentifier());
	}
	
	@Test
	public final void testGetProcessingById_OnNonExistingId_ShallReturnNull()
	{
		doReturn(null)
			.when(mqiMessageRepository)
			.findById(123);
		
		assertNull(uut.getProcessing(123));
	}
	
	@Test
	public final void testGetProcessings_OnNoFilterAndNoPaging_ShallReturnAllResults()
	{
		doReturn(Arrays.asList(newMqiMessage(1),newMqiMessage(2),newMqiMessage(3),newMqiMessage(4)))
			.when(mqiMessageRepository)
			.findByStateInAndTopicInOrderByCreationDate(
					Mockito.eq(RequestRepository.PROCESSING_STATE_LIST), 
					Mockito.eq(RequestRepository.PROCESSING_TYPES_LIST)
			);
		
		final List<Processing> actual = uut.getProcessings(null, 0, Collections.emptyList(), Collections.emptyList());
		assertEquals(4, actual.size());		
	}
	
	@Test
	public final void testGetProcessings_OnStateFilterAndNoPaging_ShallReturnResultsWithSameState()
	{
		doReturn(Arrays.asList(newMqiMessage(1),newMqiMessage(2),newMqiMessage(3),newMqiMessage(4)))
			.when(mqiMessageRepository)
			.findByStateInAndTopicInOrderByCreationDate(Mockito.any(),Mockito.any());
		
		final List<Processing> actual = uut.getProcessings(null, 0, Collections.emptyList(), Collections.singletonList(MessageState.READ));
		assertEquals(4, actual.size());		
	}
	
	@Test
	public final void testGetProcessings_OnProcessingTypeFilterAndNoPaging_ShallReturnResultsWithSameProcessingType()
	{
		doReturn(Arrays.asList(newMqiMessage(1),newMqiMessage(2),newMqiMessage(3),newMqiMessage(4)))
			.when(mqiMessageRepository)
			.findByStateInAndTopicInOrderByCreationDate(Mockito.any(),Mockito.any());
		
		final List<Processing> actual = uut.getProcessings(null, 0, Collections.singletonList("t-pdgs-aio-l0-segment-production-events"), Collections.emptyList());
		assertEquals(4, actual.size());		
	}
	
	@Test
	public final void testGetProcessings_OnNoFilterAndPaging_ShallReturnResultsOfFirstPage()
	{
		doReturn(new PageImpl<>(Arrays.asList(newMqiMessage(1),newMqiMessage(2)), PageRequest.of(0,2), 2))
			.when(mqiMessageRepository)
			.findByStateInAndTopicIn(Mockito.any(),Mockito.any(), Mockito.any());
		
		final List<Processing> actual = uut.getProcessings(2, 0, Collections.emptyList(), Collections.emptyList());
		assertEquals(2, actual.size());		
	}
	
	@Test
	public final void testGetProcessings_OnRepoReturningNull_ShallReturnEmptyCollection()
	{
		doReturn(null)
			.when(mqiMessageRepository)
			.findByStateInAndTopicInOrderByCreationDate(Mockito.any(),Mockito.any());
		
		final List<Processing> actual = uut.getProcessings(null, 0, Collections.emptyList(), Collections.emptyList());
		assertEquals(0, actual.size());	
	}
	
	
	
	
	
	private final FailedProcessingDto newFailedProcessingDto(final long id) {
		final GenericMessageDto<?> mess = new GenericMessageDto<>();
		mess.setId(id);
		
		final FailedProcessingDto fpDto = new FailedProcessingDto();
		fpDto.setProcessingDetails(mess);
		fpDto.setFailureMessage("expected error");
		return fpDto;
	}
	
	private final FailedProcessing newFailedProcessing(final long id) {
		return newFailedProcessing(id, new ProductionEvent());
	}
	
	private final FailedProcessing newFailedProcessing(final long id, final AbstractMessage mess) {
		final FailedProcessing fpDto = new FailedProcessing();
		fpDto.setId(123);
		fpDto.setDto(Collections.singletonList(mess));
		fpDto.setTopic("myTopic");
		return fpDto;
	}
	
	private final MqiMessage newMqiMessage(final long id) {
		return newMqiMessage(id, MessageState.READ);
	}
	
	private final MqiMessage newMqiMessage(final long id, final MessageState state) {
		final MqiMessage mqiMsg = new MqiMessage();
		mqiMsg.setId(id);
		mqiMsg.setCreationDate(new Date());
		mqiMsg.setState(state);
		mqiMsg.setTopic("t-pdgs-aio-l0-segment-production-events");
		return mqiMsg;
	}
}

package esa.s1pdgs.cpoc.errorrepo.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.appcatalog.common.MqiMessage;
import esa.s1pdgs.cpoc.errorrepo.kafka.producer.SubmissionClient;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.errorrepo.repo.FailedProcessingRepo;
import esa.s1pdgs.cpoc.errorrepo.repo.MqiMessageRepo;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

public class ErrorRepositoryTest {

	@Mock
	private FailedProcessingRepo failedProcessingRepo;

	@Mock
	private MqiMessageRepo mqiMessageRepository;
	
	@Mock
	private SubmissionClient submissionClient;

	private ErrorRepository errorRepository;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		this.errorRepository = new ErrorRepositoryImpl(mqiMessageRepository, failedProcessingRepo, submissionClient);
	}

	@Test
	public void getFailedProcessings() {

		List<FailedProcessingDto<GenericMessageDto<?>>> failedProcessingsToReturn = new ArrayList<>();

		GenericMessageDto<?> levelProductsMsgDto = new GenericMessageDto<>();
		FailedProcessingDto<GenericMessageDto<?>> fpDto = new FailedProcessingDto<>();
		fpDto.setIdentifier(123);
		fpDto.setDto(levelProductsMsgDto);
		failedProcessingsToReturn.add(fpDto);

		doReturn(failedProcessingsToReturn).when(failedProcessingRepo).findAll();

		@SuppressWarnings("rawtypes")
		List<FailedProcessingDto> failedProcessings = errorRepository.getFailedProcessings();

		assertEquals(123, failedProcessings.get(0).getIdentifier());
		assertTrue(failedProcessings.get(0).getDto() instanceof GenericMessageDto);
	}

	@Test
	public void getFailedProcessingById_Existing() {

		GenericMessageDto<?> levelProductsMsgDto = new GenericMessageDto<>();
		FailedProcessingDto<GenericMessageDto<?>> fpDtoToReturn = new FailedProcessingDto<>();
		fpDtoToReturn.setIdentifier(123);
		fpDtoToReturn.setDto(levelProductsMsgDto);

		doReturn(fpDtoToReturn).when(failedProcessingRepo).findByIdentifier(123);

		@SuppressWarnings("unchecked")
		FailedProcessingDto<GenericMessageDto<?>> failedProcessing = errorRepository.getFailedProcessingById(123);

		assertEquals(123, failedProcessing.getIdentifier());
		assertTrue(failedProcessing.getDto() instanceof GenericMessageDto<?>);
	}

	@Test
	public void getFailedProcessingById_NotExisting() {
		doReturn(null).when(failedProcessingRepo).findByIdentifier(456);
		try {
			errorRepository.getFailedProcessingById(456);
			fail("IllegalArguementException expected");
		} catch (IllegalArgumentException e) {
			// Expected
		}

	}

	@Test
	public void deleteFailedProcessing_Existing() {
		GenericMessageDto<?> levelProductsMsgDto = new GenericMessageDto<>();
		FailedProcessingDto<GenericMessageDto<?>> fpDto = new FailedProcessingDto<>();
		fpDto.setIdentifier(123);
		fpDto.setDto(levelProductsMsgDto);

		doReturn(fpDto).when(failedProcessingRepo).findByIdentifier(123);

		errorRepository.deleteFailedProcessing(123);

		verify(failedProcessingRepo).deleteByIdentifier(123L);
	}

	@Test
	public void deleteFailedProcessing_NotExisting() {
		doReturn(null).when(failedProcessingRepo).findByIdentifier(456);
		
		try {
			errorRepository.deleteFailedProcessing(456);
			fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException e) {
			// expected
			verify(failedProcessingRepo, never()).deleteByIdentifier(123L);
		}
	}

	@Test
	public void saveFailedProcessing_MessageExisting() {
		MqiMessage mqiMsg = new MqiMessage();
		mqiMsg.setIdentifier(456);
		
		GenericMessageDto<?> levelProductsMsgDto = new GenericMessageDto<>();
		levelProductsMsgDto.setIdentifier(mqiMsg.getIdentifier());

		FailedProcessingDto<GenericMessageDto<?>> fpDto = new FailedProcessingDto<>();
		fpDto.setIdentifier(123);
		fpDto.setDto(levelProductsMsgDto);

		doReturn(mqiMsg).when(mqiMessageRepository).findByIdentifier(456);

		errorRepository.saveFailedProcessing(fpDto);
		verify(mqiMessageRepository, times(1)).findByIdentifier(456);
		verify(failedProcessingRepo, times(1)).save(fpDto);
	}

	@Test
	public void saveFailedProcessing_MessageNotExisting() {

		GenericMessageDto<?> levelProductsMsgDto = new GenericMessageDto<>();
		levelProductsMsgDto.setIdentifier(789);
		FailedProcessingDto<GenericMessageDto<?>> fpDto = new FailedProcessingDto<>();
		fpDto.setIdentifier(123);
		fpDto.setDto(levelProductsMsgDto);

		doReturn(null).when(failedProcessingRepo).findByIdentifier(123);

		try {
			errorRepository.saveFailedProcessing(fpDto);
			fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException e) {
			// expected
		}
		verify(failedProcessingRepo, never()).save(fpDto);
	}

	@Test
	public void restartAndDeleteFailedProcessing_ExistingWithTopic() {
		GenericMessageDto<?> levelProductsMsgDto = new GenericMessageDto<>();
		FailedProcessingDto<GenericMessageDto<?>> fpDto = new FailedProcessingDto<>();
		fpDto.setIdentifier(123);
		fpDto.setTopic("topic");
		fpDto.setDto(levelProductsMsgDto);

		doReturn(fpDto).when(failedProcessingRepo).findByIdentifier(123);

		errorRepository.restartAndDeleteFailedProcessing(123);
		verify(failedProcessingRepo, times(1)).findByIdentifier(123);
		verify(failedProcessingRepo, times(1)).deleteByIdentifier(123);
		verify(submissionClient, times(1)).resubmit(fpDto, levelProductsMsgDto.getBody());
	}

	@Test
	public void restartAndDeleteFailedProcessing_ExistingWithoutTopic() {
		GenericMessageDto<?> levelProductsMsgDto = new GenericMessageDto<>();
		FailedProcessingDto<GenericMessageDto<?>> fpDto = new FailedProcessingDto<>();
		fpDto.setIdentifier(456);
		fpDto.setDto(levelProductsMsgDto);

		doReturn(fpDto).when(failedProcessingRepo).findByIdentifier(456);

		try {
			errorRepository.restartAndDeleteFailedProcessing(456);
			fail("IllegalArguemtException expected");
		} catch (IllegalArgumentException e) {
			// Expected
		}
		verify(failedProcessingRepo, times(1)).findByIdentifier(456);
		verify(failedProcessingRepo, never()).deleteByIdentifier(456);
		verify(submissionClient, never()).resubmit(fpDto, levelProductsMsgDto.getBody());
	}

	@Test
	public void restartAndDeleteFailedProcessing_NotExisting() {
		doReturn(null).when(failedProcessingRepo).findByIdentifier(789);

		try {
			errorRepository.restartAndDeleteFailedProcessing(789);
			fail("IllegalArguemtException expected");
		} catch (IllegalArgumentException e) {
			// Expected
		}
		verify(failedProcessingRepo, times(1)).findByIdentifier(789);
		verify(failedProcessingRepo, never()).deleteByIdentifier(789);
		verify(submissionClient, never()).resubmit(any(), any());
	}
}

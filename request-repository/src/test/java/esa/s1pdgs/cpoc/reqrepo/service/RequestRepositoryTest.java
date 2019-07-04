package esa.s1pdgs.cpoc.reqrepo.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Sort;

import esa.s1pdgs.cpoc.appcatalog.common.FailedProcessing;
import esa.s1pdgs.cpoc.appcatalog.common.MqiMessage;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;
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

	private RequestRepository requestRepository;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		this.requestRepository = new RequestRepositoryImpl(mqiMessageRepository, failedProcessingRepo, submissionClient);
	}

	@Test
	public void getFailedProcessings() {
		List<FailedProcessing> failedProcessingsToReturn = new ArrayList<>();
		
		GenericMessageDto<?> levelProductsMsgDto = new GenericMessageDto<>();
		FailedProcessing fpDto = new FailedProcessing();
		fpDto.setId(123);
		fpDto.setDto(levelProductsMsgDto);
		failedProcessingsToReturn.add(fpDto);

		doReturn(failedProcessingsToReturn)
			.when(failedProcessingRepo)
			.findAll(Mockito.any(Sort.class));

		List<FailedProcessing> failedProcessings = requestRepository.getFailedProcessings();

		assertEquals(123, failedProcessings.get(0).getId());
		assertTrue(failedProcessings.get(0).getDto() instanceof GenericMessageDto);
	}

	@Test
	public void getFailedProcessingById_Existing() {

		GenericMessageDto<?> levelProductsMsgDto = new GenericMessageDto<>();
		FailedProcessing fpDtoToReturn = new FailedProcessing();
		fpDtoToReturn.setId(123);
		fpDtoToReturn.setDto(levelProductsMsgDto);

		doReturn(fpDtoToReturn).when(failedProcessingRepo).findById(123);

		FailedProcessing failedProcessing = requestRepository.getFailedProcessingById(123);

		assertEquals(123, failedProcessing.getId());
		assertTrue(failedProcessing.getDto() instanceof GenericMessageDto<?>);
	}

	@Test
	public void getFailedProcessingById_NotExisting() {
		doReturn(null).when(failedProcessingRepo).findById(456);
		try {
			requestRepository.getFailedProcessingById(456);
			fail("IllegalArguementException expected");
		} catch (IllegalArgumentException e) {
			// Expected
		}

	}

	@Test
	public void deleteFailedProcessing_Existing() {
		GenericMessageDto<?> levelProductsMsgDto = new GenericMessageDto<>();
		FailedProcessing fpDto = new FailedProcessing();
		fpDto.setId(123);
		fpDto.setDto(levelProductsMsgDto);

		doReturn(fpDto).when(failedProcessingRepo).findById(123);

		requestRepository.deleteFailedProcessing(123);

		verify(failedProcessingRepo).deleteById(123L);
	}

	@Test
	public void deleteFailedProcessing_NotExisting() {
		doReturn(null).when(failedProcessingRepo).findById(456);
		
		try {
			requestRepository.deleteFailedProcessing(456);
			fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException e) {
			// expected
			verify(failedProcessingRepo, never()).deleteById(123L);
		}
	}

	@Test
	public void saveFailedProcessing_MessageExisting() {
		MqiMessage mqiMsg = new MqiMessage();
		mqiMsg.setIdentifier(456);
		
		GenericMessageDto<?> levelProductsMsgDto = new GenericMessageDto<>();
		levelProductsMsgDto.setIdentifier(mqiMsg.getIdentifier());

		FailedProcessingDto fpDto = new FailedProcessingDto();	
		fpDto.setProcessingDetails(levelProductsMsgDto);

		doReturn(mqiMsg).when(mqiMessageRepository).findByIdentifier(456);

		requestRepository.saveFailedProcessing(fpDto);
		verify(mqiMessageRepository, times(1)).findByIdentifier(456);
		verify(failedProcessingRepo, times(1)).save(Mockito.any());
	}

	@Test
	public void saveFailedProcessing_MessageNotExisting() {

		GenericMessageDto<?> levelProductsMsgDto = new GenericMessageDto<>();
		levelProductsMsgDto.setIdentifier(789);
		FailedProcessingDto fpDto = new FailedProcessingDto();
		fpDto.setProcessingDetails(levelProductsMsgDto);

		doReturn(null).when(failedProcessingRepo).findById(123);

		try {
			requestRepository.saveFailedProcessing(fpDto);
			fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException e) {
			// expected
		}
		verify(failedProcessingRepo, never()).save(Mockito.any());
	}

	@Test
	public void restartAndDeleteFailedProcessing_ExistingWithTopic() {
		final MqiMessage mqiMsg = new MqiMessage();
		mqiMsg.setIdentifier(123);
		mqiMsg.setTopic("myTopic");
		
		final ProductDto dto = new ProductDto("fooBar","123", ProductFamily.AUXILIARY_FILE);
		
		final GenericMessageDto<ProductDto> levelProductsMsgDto = new GenericMessageDto<>();
		levelProductsMsgDto.setBody(dto);
		
		final FailedProcessing fp = FailedProcessing.valueOf(
				mqiMsg, 
				new FailedProcessingDto("localHost", new Date(), "Expected", levelProductsMsgDto)
		);

		doReturn(fp).when(failedProcessingRepo).findById(123);

		requestRepository.restartAndDeleteFailedProcessing(123);
		verify(failedProcessingRepo, times(1)).findById(123);
		verify(failedProcessingRepo, times(1)).deleteById(123);
		verify(submissionClient, times(1)).resubmit(fp, dto);
	}

	@Test
	public void restartAndDeleteFailedProcessing_ExistingWithoutTopic() {
		GenericMessageDto<?> levelProductsMsgDto = new GenericMessageDto<>();
		FailedProcessing fpDto = new FailedProcessing();
		fpDto.setId(456);
		fpDto.setDto(levelProductsMsgDto);

		doReturn(fpDto).when(failedProcessingRepo).findById(456);

		try {
			requestRepository.restartAndDeleteFailedProcessing(456);
			fail("IllegalArguemtException expected");
		} catch (RuntimeException e) {
			// Expected
		}
		verify(failedProcessingRepo, times(1)).findById(456);
		verify(failedProcessingRepo, never()).deleteById(456);
		verify(submissionClient, never()).resubmit(fpDto, levelProductsMsgDto.getBody());
	}

	@Test
	public void restartAndDeleteFailedProcessing_NotExisting() {
		doReturn(null).when(failedProcessingRepo).findById(789);

		try {
			requestRepository.restartAndDeleteFailedProcessing(789);
			fail("IllegalArguemtException expected");
		} catch (IllegalArgumentException e) {
			// Expected
		}
		verify(failedProcessingRepo, times(1)).findById(789);
		verify(failedProcessingRepo, never()).deleteById(789);
		verify(submissionClient, never()).resubmit(any(), any());
	}
}

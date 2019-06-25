package esa.s1pdgs.cpoc.errorrepo.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.mongodb.client.result.DeleteResult;

import esa.s1pdgs.cpoc.appcatalog.common.MqiMessage;
import esa.s1pdgs.cpoc.errorrepo.kafka.producer.SubmissionClient;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.errorrepo.repo.FailedProcessingRepo;
import esa.s1pdgs.cpoc.errorrepo.repo.MqiMessageRepo;
import esa.s1pdgs.cpoc.mqi.model.rest.LevelProductsMessageDto;

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

		List<FailedProcessingDto<LevelProductsMessageDto>> failedProcessingsToReturn = new ArrayList<>();

		LevelProductsMessageDto levelProductsMsgDto = new LevelProductsMessageDto();
		FailedProcessingDto<LevelProductsMessageDto> fpDto = new FailedProcessingDto<>();
		fpDto.setIdentifier(123);
		fpDto.setDto(levelProductsMsgDto);
		failedProcessingsToReturn.add(fpDto);

		doReturn(failedProcessingsToReturn).when(failedProcessingRepo).findAll();

		@SuppressWarnings("rawtypes")
		List<FailedProcessingDto> failedProcessings = errorRepository.getFailedProcessings();

		assertEquals(123, failedProcessings.get(0).getIdentifier());
		assertTrue(failedProcessings.get(0).getDto() instanceof LevelProductsMessageDto);
	}

	@Test
	public void getFailedProcessingById() {

		LevelProductsMessageDto levelProductsMsgDto = new LevelProductsMessageDto();
		FailedProcessingDto<LevelProductsMessageDto> fpDtoToReturn = new FailedProcessingDto<>();
		fpDtoToReturn.setIdentifier(123);
		fpDtoToReturn.setDto(levelProductsMsgDto);

		doReturn(fpDtoToReturn).when(failedProcessingRepo).findByIdentifier(anyLong());

		@SuppressWarnings("unchecked")
		FailedProcessingDto<LevelProductsMessageDto> failedProcessing = errorRepository.getFailedProcessingById(123);

		assertEquals(123, failedProcessing.getIdentifier());
		assertTrue(failedProcessing.getDto() instanceof LevelProductsMessageDto);
	}

	@Test
	public void getFailedProcessingByIdWhenNotFound() {

		try {
			errorRepository.getFailedProcessingById(123);
			fail("IllegalArguementException expected");
		} catch (IllegalArgumentException e) {
			// Expected
		}

	}

	@Test
	public void deleteFailedProcessing() {

		LevelProductsMessageDto levelProductsMsgDto = new LevelProductsMessageDto();
		FailedProcessingDto<LevelProductsMessageDto> fpDto = new FailedProcessingDto<>();
		fpDto.setIdentifier(123);
		fpDto.setDto(levelProductsMsgDto);

		doReturn(fpDto).when(failedProcessingRepo).findByIdentifier(anyLong());
		DeleteResult deleteResult = new DeleteResult() {

			@Override
			public boolean wasAcknowledged() {
				return true;
			}

			@Override
			public long getDeletedCount() {

				return 1;
			}
		};

		doReturn(deleteResult).when(failedProcessingRepo).deleteByIdentifier(anyLong());

		errorRepository.deleteFailedProcessing(123);

		doReturn(null).when(failedProcessingRepo).deleteByIdentifier(anyLong());

		try {
			errorRepository.deleteFailedProcessing(4);
			fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	public void deleteFailedProcessingWhenNotSucceeded() {

		LevelProductsMessageDto levelProductsMsgDto = new LevelProductsMessageDto();
		FailedProcessingDto<LevelProductsMessageDto> fpDto = new FailedProcessingDto<>();
		fpDto.setIdentifier(123);
		fpDto.setDto(levelProductsMsgDto);

		doReturn(fpDto).when(failedProcessingRepo).findByIdentifier(anyLong());
		DeleteResult deleteResult = new DeleteResult() {

			@Override
			public boolean wasAcknowledged() {
				return true;
			}

			@Override
			public long getDeletedCount() {

				return 0;
			}
		};

		doReturn(deleteResult).when(failedProcessingRepo).deleteByIdentifier(fpDto.getIdentifier());

		try {
			errorRepository.deleteFailedProcessing(123);
			fail("IllegalArgumentException expected");
		} catch (RuntimeException e) {
			// expected
		}
	}

	@Test
	public void saveFailedProcessing() {

		LevelProductsMessageDto levelProductsMsgDto = new LevelProductsMessageDto();
		levelProductsMsgDto.setIdentifier(1);
		FailedProcessingDto<LevelProductsMessageDto> fpDto = new FailedProcessingDto<>();
		fpDto.setIdentifier(123);
		fpDto.setDto(levelProductsMsgDto);

		MqiMessage mqiMsg = new MqiMessage();
		doReturn(mqiMsg).when(failedProcessingRepo).findByIdentifier(anyLong());

		errorRepository.saveFailedProcessing(fpDto);
		verify(failedProcessingRepo, times(1)).insert(fpDto);
	}

	@Test
	public void saveFailedProcessingWhenMessageNotFound() {

		LevelProductsMessageDto levelProductsMsgDto = new LevelProductsMessageDto();
		levelProductsMsgDto.setIdentifier(1);
		FailedProcessingDto<LevelProductsMessageDto> fpDto = new FailedProcessingDto<>();
		fpDto.setIdentifier(123);
		fpDto.setDto(levelProductsMsgDto);

		doReturn(null).when(failedProcessingRepo).findByIdentifier(anyLong());

		try {
			errorRepository.saveFailedProcessing(fpDto);
			fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException e) {
			// expected
		}
		verify(failedProcessingRepo, times(0)).insert(fpDto);
	}

	@Test
	public void restartAndDeleteFailedProcessing() {
		LevelProductsMessageDto levelProductsMsgDto = new LevelProductsMessageDto();
		FailedProcessingDto<LevelProductsMessageDto> fpDto = new FailedProcessingDto<>();
		fpDto.setIdentifier(123);
		fpDto.setTopic("topic");
		fpDto.setDto(levelProductsMsgDto);

		doReturn(fpDto).when(failedProcessingRepo).findByIdentifier(anyLong());

		DeleteResult deleteResult = new DeleteResult() {

			@Override
			public boolean wasAcknowledged() {
				return true;
			}

			@Override
			public long getDeletedCount() {

				return 1;
			}
		};

		doReturn(deleteResult).when(failedProcessingRepo).deleteByIdentifier(anyLong());

		errorRepository.restartAndDeleteFailedProcessing(123);
		verify(failedProcessingRepo, times(1)).findByIdentifier(anyLong());
		verify(failedProcessingRepo, times(1)).deleteByIdentifier(anyLong());
		verify(submissionClient, times(1)).resubmit(fpDto, levelProductsMsgDto.getBody());
	}

	@Test
	public void restartAndDeleteFailedProcessingNoTopic() {
		LevelProductsMessageDto levelProductsMsgDto = new LevelProductsMessageDto();
		FailedProcessingDto<LevelProductsMessageDto> fpDto = new FailedProcessingDto<>();
		fpDto.setIdentifier(123);
		fpDto.setDto(levelProductsMsgDto);

		doReturn(fpDto).when(failedProcessingRepo).findByIdentifier(anyLong());

		DeleteResult deleteResult = new DeleteResult() {

			@Override
			public boolean wasAcknowledged() {
				return true;
			}

			@Override
			public long getDeletedCount() {

				return 1;
			}
		};

		doReturn(deleteResult).when(failedProcessingRepo).deleteByIdentifier(anyLong());

		try {
			errorRepository.restartAndDeleteFailedProcessing(123);
			fail("IllegalArguemtException expected");
		} catch (IllegalArgumentException e) {
			// Expected
		}
		verify(failedProcessingRepo, times(1)).findByIdentifier(anyLong());
		verify(failedProcessingRepo, times(0)).deleteByIdentifier(anyLong());
		verify(submissionClient, times(0)).resubmit(fpDto, levelProductsMsgDto.getBody());

	}

}

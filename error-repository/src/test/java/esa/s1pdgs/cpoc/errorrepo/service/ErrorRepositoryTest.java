package esa.s1pdgs.cpoc.errorrepo.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.mongodb.core.MongoTemplate;

import esa.s1pdgs.cpoc.errorrepo.kafka.producer.SubmissionClient;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.mqi.model.rest.LevelProductsMessageDto;

public class ErrorRepositoryTest {

	@Mock
	private MongoTemplate mongoTemplate;

	private ErrorRepository errorRepository;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		this.errorRepository = new ErrorRepositoryImpl(mongoTemplate, SubmissionClient.NULL);
	}

	@Test
	public void getFailedProcessings() {

		List<FailedProcessingDto<LevelProductsMessageDto>> failedProcessingsToReturn = new ArrayList<>();

		LevelProductsMessageDto levelProductsMsgDto = new LevelProductsMessageDto();
		FailedProcessingDto<LevelProductsMessageDto> fpDto = new FailedProcessingDto<>();
		fpDto.setIdentifier(123);
		fpDto.setDto(levelProductsMsgDto);
		failedProcessingsToReturn.add(fpDto);

		doReturn(failedProcessingsToReturn).when(mongoTemplate).findAll(FailedProcessingDto.class);

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

		doReturn(fpDtoToReturn).when(mongoTemplate).findById("123", FailedProcessingDto.class);

		FailedProcessingDto<LevelProductsMessageDto> failedProcessing = errorRepository.getFailedProcessingsById("123");

		assertEquals(123, failedProcessing.getIdentifier());
		assertTrue(failedProcessing.getDto() instanceof LevelProductsMessageDto);
	}

}

package esa.s1pdgs.cpoc.errorrepo.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.appcatalog.common.MqiMessage;
import esa.s1pdgs.cpoc.errorrepo.model.rest.ProcessingDto;
import esa.s1pdgs.cpoc.errorrepo.repo.MqiMessageRepo;

@Component
public class ProcessingsRepositoryImpl implements ProcessingsRepository {

	private final MqiMessageRepo processingRepo;
	
	@Autowired
	public ProcessingsRepositoryImpl(final MqiMessageRepo processingRepo) {
		this.processingRepo = processingRepo;
	}

	@Override
	public List<String> getProcessingTypes() {
		return Arrays.asList("t-pdgs-edrs-sessions", "t-pdgs-auxiliary-files", "t-pdgs-l0-jobs",
				"t-pdgs-l0-segment-jobs", "t-pdgs-l0-segments", "t-pdgs-l0-slices-nrt", "t-pdgs-l0-acns-nrt",
				"t-pdgs-l0-slices-fast", "t-pdgs-l0-acns-fast", "t-pdgs-l0-reports", "t-pdgs-l0-segment-reports",
				"t-pdgs-l0-blanks", "t-pdgs-l1-slices-nrt", "t-pdgs-l1-acns-nrt", "t-pdgs-l1-slices-fast",
				"t-pdgs-l1-acns-fast", "t-pdgs-l1-reports", "t-pdgs-l1-jobs-nrt", "t-pdgs-l1-jobs-fast",
				"t-pdgs-l2-acns-fast", "t-pdgs-l2-slices-fast", "t-pdgs-l2-jobs-fast", "t-pdgs-l2-reports",
				"t-pdgs-compressed-products");
	}
	
	@Override
	public ProcessingDto getProcessing(long id) {		
		final MqiMessage mess = processingRepo.findByIdentifier(id);
		
		if (mess == null)
		{
			return null;
		}	
		return toProcessingDto(mess);
	}
	
	@Override
	public List<ProcessingDto> getProcessings() {
		return toExternal(processingRepo.findAllOrderByCreationDate	());
	}

	private final List<ProcessingDto> toExternal(final List<MqiMessage> messages)	{
		if (messages == null || messages.size() == 0)
		{
			return Collections.emptyList();
		}		
		return messages.stream()
				.map(m -> toProcessingDto(m))
				.sorted(ProcessingDto.ASCENDING_CREATION_TIME_COMPERATOR)
				.collect(Collectors.toList());
	}
	
	private final ProcessingDto toProcessingDto(final MqiMessage message)
	{
		return new ProcessingDto()
				.identifier(message.getIdentifier())
				.processingType(message.getTopic())
				.processingStatus(message.getState())
				.productCategory(message.getCategory())
				.partition(message.getPartition())
				.offset(message.getOffset())
				.group(message.getGroup())
				.assignedPod(message.getReadingPod())
				.lastAssignmentDate(message.getLastReadDate())
				.sendingPod(message.getSendingPod())
				.lastSendDate(message.getLastSendDate())
				.lastAckDate(message.getLastAckDate())
				.nbRetries(message.getNbRetries())
				.creationDate(message.getCreationDate())
				.processingDetails(message.getDto());
	}

}

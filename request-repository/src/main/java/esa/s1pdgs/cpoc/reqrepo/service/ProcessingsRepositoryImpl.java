package esa.s1pdgs.cpoc.reqrepo.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.appcatalog.common.MqiMessage;
import esa.s1pdgs.cpoc.appcatalog.common.Processing;
import esa.s1pdgs.cpoc.reqrepo.repo.MqiMessageRepo;

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
	public Processing getProcessing(long id) {		
		final MqiMessage mess = processingRepo.findByIdentifier(id);
		
		if (mess == null) {
			return null;
		}	
		return new Processing(mess);
	}
	
	@Override
	public List<Processing> getProcessings(Pageable pageable) {		
		final Page<MqiMessage> page = processingRepo.findAll(pageable);
		return toExternal(page.getContent());
	}
	
	private final List<Processing> toExternal(final List<MqiMessage> messages)	{
		if (messages == null || messages.size() == 0)
		{
			return Collections.emptyList();
		}		
		return messages.stream()
				.map(m -> new Processing(m))
				.collect(Collectors.toList());
	}
}

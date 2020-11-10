package esa.s1pdgs.cpoc.reqrepo.service;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.appcatalog.common.FailedProcessing;
import esa.s1pdgs.cpoc.appcatalog.common.MqiMessage;
import esa.s1pdgs.cpoc.common.MessageState;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

final class FailedProcessingFactory
{
	static final Logger LOG = LogManager.getLogger(FailedProcessingFactory.class);
	
	private final FailedProcessingDto failedProcDto;
	
	private MqiMessage message; 
	private MqiMessage predecessorMessage;
	
	// to be created in 'newFailedProcessing()', initialized with defaults
	private long id;
	private String topic;
	private Object dto;		
	private String predecessorTopic = null;
	private Object predecessorDto = null;
	private String failedPod;
	private Date failureDate;
	private String failureMessage;
	
	private ProductCategory category = ProductCategory.UNDEFINED;
	private int partition = -1;
	private long offset = -1;
	private String group = AbstractMessage.NOT_DEFINED;
	private MessageState state = MessageState.ACK_KO;
	private String sendingPod = AbstractMessage.NOT_DEFINED;
	private Date lastSendDate = new Date(0L);
	private Date lastAckDate = new Date(0L);
	private Date creationDate = new Date(0L);
	private Date lastAssignmentDate = new Date(0L);
	private int nbRetries = -1;
	
	public FailedProcessingFactory(final FailedProcessingDto failedProc) {
		this.failedProcDto = failedProc;
	}
	
	public FailedProcessingFactory message(final MqiMessage message) {
		this.message = message;
		return this;
	}
	
	public FailedProcessingFactory predecessorMessage(final MqiMessage predecessorMessage) {
		this.predecessorMessage = predecessorMessage;
		return this;
	}
	
	public FailedProcessing newFailedProcessing() {
		final GenericMessageDto<?> originalMessageDto = failedProcDto.getProcessingDetails();
		
		dto = originalMessageDto.getBody();
		failedPod = failedProcDto.getFailedPod();
		failureDate = failedProcDto.getFailedDate();
		failureMessage = failedProcDto.getFailureMessage();			
					
		// nominal: original message has been found in persistence
		if (message != null) {
			id = message.getId(); 
			topic = message.getTopic(); 
		
			category = message.getCategory(); 
			partition = message.getPartition();
			offset = message.getOffset(); 
			group = message.getGroup(); 
			state = message.getState(); 
			sendingPod = message.getSendingPod(); 
			lastSendDate = message.getLastSendDate(); 
			lastAckDate = message.getLastAckDate(); 
			nbRetries = message.getNbRetries(); 	
			creationDate = message.getCreationDate();
			lastAssignmentDate = message.getLastReadDate();
			LOG.debug("Extracted values from {}", message);
		}
		// off-nominal: There is no original message stored. Trying to derive information from DTO
		// as good as possible
		else {	
			id = originalMessageDto.getId();
			topic = originalMessageDto.getInputKey();
			LOG.warn("Input message {} not found. Using the one provided with request so only a "
					+ "subset of parameters will be available", id);				
		}
		
		if (predecessorMessage != null) {
			predecessorTopic = predecessorMessage.getTopic();				
		}		
		return new FailedProcessing(
				id,
				category,
				topic,
				partition,
				offset,
				group,
				state,
				sendingPod,
				lastSendDate,
				lastAckDate,
				nbRetries,
				dto,
				creationDate,
				failedPod,
				lastAssignmentDate,
				failureDate,
				failureMessage,
				predecessorDto,
				predecessorTopic
		);
	}	
}
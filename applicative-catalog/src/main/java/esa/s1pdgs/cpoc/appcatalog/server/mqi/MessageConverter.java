package esa.s1pdgs.cpoc.appcatalog.server.mqi;

import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.appcatalog.common.MqiMessage;
import esa.s1pdgs.cpoc.appcatalog.rest.AppCatMessageDto;

@Component
public class MessageConverter {	

	public <T> AppCatMessageDto<T> toAppCatMessageDto(MqiMessage message, T dto)
	{
		final AppCatMessageDto<T> result = new AppCatMessageDto<>();		
		result.setCategory(message.getCategory());
		result.setGroup(message.getGroup());
		result.setIdentifier(message.getIdentifier());
		result.setLastAckDate(message.getLastAckDate());
		result.setLastReadDate(message.getLastReadDate());
		result.setLastSendDate(message.getLastSendDate());
		result.setNbRetries(message.getNbRetries());
		result.setOffset(message.getOffset());
		result.setPartition(message.getPartition());
		result.setReadingPod(message.getReadingPod());
		result.setSendingPod(message.getSendingPod());
		result.setState(message.getState());
		result.setTopic(message.getTopic());
		
		// check if this is okay
		result.setDto(dto);
		return result;
	}
}

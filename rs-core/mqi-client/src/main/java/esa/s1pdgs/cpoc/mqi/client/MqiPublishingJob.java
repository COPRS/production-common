package esa.s1pdgs.cpoc.mqi.client;

import java.util.Collections;
import java.util.List;

import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;

public class MqiPublishingJob<E extends AbstractMessage> {

	@SuppressWarnings("unchecked")
	public static final MqiPublishingJob<?> NULL = new MqiPublishingJob(Collections.emptyList()) {};

	private final List<GenericPublicationMessageDto<? extends AbstractMessage>> messages;
	private final String warning;

	public MqiPublishingJob(List<GenericPublicationMessageDto<? extends AbstractMessage>> messages) {
		this(messages, "");
	}
	
	public MqiPublishingJob(List<GenericPublicationMessageDto<? extends AbstractMessage>> messages, String warning) {
		this.messages = messages;
		this.warning = warning;
	}

	public List<GenericPublicationMessageDto<? extends AbstractMessage>> getMessages() {
		return messages;
	}

	public String getWarning() {
		return warning;
	}

}

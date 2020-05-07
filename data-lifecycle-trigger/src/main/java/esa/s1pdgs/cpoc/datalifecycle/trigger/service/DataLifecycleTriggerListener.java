package esa.s1pdgs.cpoc.datalifecycle.trigger.service;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.datalifecycle.trigger.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.mqi.client.MqiClient;
import esa.s1pdgs.cpoc.mqi.client.MqiListener;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

public class DataLifecycleTriggerListener<E extends AbstractMessage> implements MqiListener<E> {

	private static final Logger LOG = LogManager.getLogger(DataLifecycleTriggerListener.class);

	private final MqiClient mqiClient;
	private final ErrorRepoAppender errorRepoAppender;
	private final ProcessConfiguration processConfig;

	public DataLifecycleTriggerListener(final MqiClient mqiClient, final ErrorRepoAppender errorRepoAppender,
			final ProcessConfiguration processConfig) {

		this.mqiClient = mqiClient;
		this.errorRepoAppender = errorRepoAppender;
		this.processConfig = processConfig;
	}

	@Override
	public void onMessage(GenericMessageDto<E> message) throws Exception {

		final E dto = message.getBody();
	}

	@Override
	public final void onTerminalError(final GenericMessageDto<E> message, final Exception error) {
		LOG.error(error);
		errorRepoAppender
				.send(new FailedProcessingDto(processConfig.getHostname(), new Date(), error.getMessage(), message));
	}

}

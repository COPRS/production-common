package esa.s1pdgs.cpoc.dissemination.worker.service;

import java.util.Date;

import org.apache.commons.lang.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.dissemination.worker.config.DisseminationWorkerProperties;
import esa.s1pdgs.cpoc.dissemination.worker.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.MqiListener;
import esa.s1pdgs.cpoc.mqi.client.MqiMessageEventHandler;
import esa.s1pdgs.cpoc.mqi.model.queue.DisseminationJob;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;

public class DisseminationJobListener implements MqiListener<DisseminationJob> {

	private static final Logger LOG = LogManager.getLogger(DisseminationJobListener.class);

	private final AppStatus appStatus;
	private final GenericMqiClient mqiClient;
	private final ErrorRepoAppender errorAppender;
	private final ObsClient obsClient;
	private final ProcessConfiguration processConfig;
	private final DisseminationWorkerProperties config;

	// --------------------------------------------------------------------------

	@Autowired
	public DisseminationJobListener(final AppStatus appStatus, final GenericMqiClient mqiClient,
			final ErrorRepoAppender errorAppender, final ObsClient obsClient,
			final ProcessConfiguration processConfig, final DisseminationWorkerProperties config) {
		this.appStatus = appStatus;
		this.mqiClient = mqiClient;
		this.errorAppender = errorAppender;
		this.obsClient = obsClient;

		this.processConfig = processConfig;
		this.config = config;
	}

	// --------------------------------------------------------------------------

	@Override
	public MqiMessageEventHandler onMessage(GenericMessageDto<DisseminationJob> message) throws Exception {
		// TODO Auto-generated method stub
		LOG.info("message received: " + message);
		throw new NotImplementedException("work in progress");
	}

	@Override
	public void onTerminalError(GenericMessageDto<DisseminationJob> message, Exception error) {
		LOG.error(error);
		this.errorAppender.send(new FailedProcessingDto( //
				this.processConfig.getHostname(),
				new Date(),
				error.getMessage(),
				message));
	}

}

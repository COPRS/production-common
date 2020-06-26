package esa.s1pdgs.cpoc.ipf.preparation.worker.service;

import java.util.Date;
import java.util.Map;

import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.ipf.preparation.worker.dispatch.JobDispatcher;
import esa.s1pdgs.cpoc.mqi.client.MqiListener;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfPreparationJob;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

public class IpfPreparationService implements MqiListener<IpfPreparationJob> {
    private final JobDispatcher jobDispatcher;
    private final ErrorRepoAppender errorAppender;
    private final ProcessConfiguration processConfiguration;
    
    /**
     * Available job generators (one per task tables)
     */
    protected Map<String, AbstractJobsGenerator> generators;

    public IpfPreparationService(
			final JobDispatcher jobDispatcher,
			final ErrorRepoAppender errorAppender,
			final ProcessConfiguration processConfiguration
	) {
		this.jobDispatcher = jobDispatcher;
		this.errorAppender = errorAppender;
		this.processConfiguration = processConfiguration;
	}
		
	@Override
	public void onMessage(final GenericMessageDto<IpfPreparationJob> message) throws Exception {
		jobDispatcher.dispatch(message);
	}

	@Override
	public void onTerminalError(final GenericMessageDto<IpfPreparationJob> message, final Exception error) {
		errorAppender.send(new FailedProcessingDto(
				processConfiguration.getHostname(), 
				new Date(), 
				LogUtils.toString(error), 
				message
		));
	}
}

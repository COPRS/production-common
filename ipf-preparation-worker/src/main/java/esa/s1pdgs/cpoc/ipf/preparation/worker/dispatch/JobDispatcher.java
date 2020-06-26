package esa.s1pdgs.cpoc.ipf.preparation.worker.dispatch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.mqi.model.queue.IpfPreparationJob;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

public interface JobDispatcher {
    static final Logger LOGGER = LogManager.getLogger(JobDispatcher.class);
    
	public void dispatch(final GenericMessageDto<IpfPreparationJob> message) throws Exception;
}

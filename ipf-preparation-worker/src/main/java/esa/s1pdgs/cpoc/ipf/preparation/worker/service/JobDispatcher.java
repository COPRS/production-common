package esa.s1pdgs.cpoc.ipf.preparation.worker.service;

import esa.s1pdgs.cpoc.mqi.model.queue.IpfPreparationJob;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

public interface JobDispatcher {
	void dispatch(GenericMessageDto<IpfPreparationJob> message);
}

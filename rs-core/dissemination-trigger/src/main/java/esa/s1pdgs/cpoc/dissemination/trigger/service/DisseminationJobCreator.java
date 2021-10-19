package esa.s1pdgs.cpoc.dissemination.trigger.service;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.queue.DisseminationJob;

public interface DisseminationJobCreator {
	
	public DisseminationJob createJob(AbstractMessage event) throws AbstractCodedException;

}

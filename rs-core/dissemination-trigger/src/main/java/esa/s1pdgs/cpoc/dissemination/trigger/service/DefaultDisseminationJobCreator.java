package esa.s1pdgs.cpoc.dissemination.trigger.service;

import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.queue.DisseminationJob;

public class DefaultDisseminationJobCreator implements DisseminationJobCreator {
	
	public static final String TYPE = DisseminationTriggerType.DEFAULT.name().toLowerCase();

	@Override
	public DisseminationJob createJob(AbstractMessage event) {
		final DisseminationJob disseminationJob = new DisseminationJob();
		disseminationJob.setKeyObjectStorage(event.getKeyObjectStorage());
		disseminationJob.setProductFamily(event.getProductFamily());
		disseminationJob.addDisseminationSource(event.getProductFamily(), event.getKeyObjectStorage());
		return disseminationJob;
	}

}

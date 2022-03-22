package esa.s1pdgs.cpoc.ipf.execution.worker.job.oqc;

import java.io.File;
import java.util.concurrent.Callable;

import esa.s1pdgs.cpoc.ipf.execution.worker.config.ApplicationProperties;
import esa.s1pdgs.cpoc.mqi.model.queue.OQCFlag;

public class OQCDefaultTaskFactory extends OQCTaskFactory {

	@Override
	public Callable<OQCFlag> createOQCTask(final ApplicationProperties properties, final File originalProduct) {
		return new OQCTask(properties, originalProduct);
	}

}

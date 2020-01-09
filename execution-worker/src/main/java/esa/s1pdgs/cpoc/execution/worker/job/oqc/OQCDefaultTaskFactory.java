package esa.s1pdgs.cpoc.execution.worker.job.oqc;

import java.nio.file.Path;
import java.util.concurrent.Callable;

import esa.s1pdgs.cpoc.execution.worker.config.ApplicationProperties;
import esa.s1pdgs.cpoc.mqi.model.queue.OQCFlag;

public class OQCDefaultTaskFactory extends OQCTaskFactory {

	@Override
	public Callable<OQCFlag> createOQCTask(ApplicationProperties properties, Path originalProduct) {
		return new OQCTask(properties, originalProduct);
	}

}

package esa.s1pdgs.cpoc.wrapper.job.oqc;

import java.nio.file.Path;
import java.util.concurrent.Callable;

import esa.s1pdgs.cpoc.mqi.model.queue.OQCFlag;
import esa.s1pdgs.cpoc.wrapper.config.ApplicationProperties;

public class OQCDefaultTaskFactory extends OQCTaskFactory {

	@Override
	public Callable<OQCFlag> createOQCTask(ApplicationProperties properties, Path originalProduct) {
		return new OQCTask(properties, originalProduct);
	}

}

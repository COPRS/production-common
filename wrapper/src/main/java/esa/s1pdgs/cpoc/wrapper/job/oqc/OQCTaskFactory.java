package esa.s1pdgs.cpoc.wrapper.job.oqc;

import java.nio.file.Path;
import java.util.concurrent.Callable;

import esa.s1pdgs.cpoc.mqi.model.queue.OQCFlag;
import esa.s1pdgs.cpoc.wrapper.config.ApplicationProperties;

public abstract class OQCTaskFactory {
	public abstract Callable<OQCFlag> createOQCTask(ApplicationProperties properties, Path originalProduct); 
}

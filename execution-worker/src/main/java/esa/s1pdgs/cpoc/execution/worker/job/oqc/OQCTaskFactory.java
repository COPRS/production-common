package esa.s1pdgs.cpoc.execution.worker.job.oqc;

import java.nio.file.Path;
import java.util.concurrent.Callable;

import esa.s1pdgs.cpoc.execution.worker.config.ApplicationProperties;
import esa.s1pdgs.cpoc.mqi.model.queue.OQCFlag;

public abstract class OQCTaskFactory {
	public abstract Callable<OQCFlag> createOQCTask(ApplicationProperties properties, Path originalProduct); 
}

package esa.s1pdgs.cpoc.ipf.execution.worker.job.oqc;

import java.io.File;
import java.util.concurrent.Callable;

import esa.s1pdgs.cpoc.ipf.execution.worker.config.ApplicationProperties;
import esa.s1pdgs.cpoc.mqi.model.queue.OQCFlag;

public abstract class OQCTaskFactory {
	public abstract Callable<OQCFlag> createOQCTask(ApplicationProperties properties, File originalProduct); 
}

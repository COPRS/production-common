package esa.s1pdgs.cpoc.ipf.preparation.worker.generator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface JobGenerator extends Runnable {
	static final Logger LOGGER = LogManager.getLogger(JobGenerator.class);
}

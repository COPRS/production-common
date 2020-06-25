package esa.s1pdgs.cpoc.ipf.preparation.worker.type;

import java.io.File;
import java.util.Arrays;

import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.JobsGeneratorFactory.JobGenType;
import esa.s1pdgs.cpoc.ipf.preparation.worker.service.JobDispatcher;
import esa.s1pdgs.cpoc.ipf.preparation.worker.service.JobGenerator;

public interface ProductTypeFactory {
	
	public JobGenType type();
	
	public JobGenerator newJobGenerator(final File taskTableFile);	
	
	public JobDispatcher newJobDispatcher();

	public static ProductTypeFactory forLevel(final ApplicationLevel level) {
		switch (level) {
			case L0:
				return new EdrsSessionTypeFactory();
			case L0_SEGMENT:
				return null; // FIXME
			case L1:
			case L2:
				return null; //FIXME
			default:
				// fall through to throw exception
		}
		throw new IllegalArgumentException(
				String.format(
						"Unsupported Application Level '%s'. Available are: %s", 
						level,
						Arrays.asList(ApplicationLevel.values())
				)
		);		
	}
}

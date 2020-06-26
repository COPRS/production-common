package esa.s1pdgs.cpoc.ipf.preparation.worker.type;

import java.util.Arrays;

import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.mapper.TasktableMapper;

public interface ProductTypeFactory {
	
	public ApplicationLevel level();
	
	public ProductTypeAdapter typeAdapter();
	
	//public JobDispatcher newJobDispatcher(final Map<String, JobGenerator> generators);
	
	public TasktableMapper tasktableMapper();

	public static ProductTypeFactory forLevel(final ApplicationLevel level) {
		switch (level) {
			case L0:
				return new EdrsSessionFactory(level);
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

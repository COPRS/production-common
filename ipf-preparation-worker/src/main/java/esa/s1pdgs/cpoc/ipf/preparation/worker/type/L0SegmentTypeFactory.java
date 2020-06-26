package esa.s1pdgs.cpoc.ipf.preparation.worker.type;

import esa.s1pdgs.cpoc.ipf.preparation.worker.generator.JobsGeneratorFactory.JobGenType;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.mapper.SingleTasktableMapper;

public class L0SegmentTypeFactory extends AbstractProductTypeFactory implements ProductTypeFactory {
    private static final String TASK_TABLE_NAME = "TaskTable.L0ASP.xml";
    
	public L0SegmentTypeFactory() {
		super(JobGenType.LEVEL_SEGMENT, new SingleTasktableMapper(TASK_TABLE_NAME));
	}

	@Override
	public ProductTypeAdapter typeAdapter() {
		// TODO Auto-generated method stub
		return null;
	}
}

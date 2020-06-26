package esa.s1pdgs.cpoc.ipf.preparation.worker.type;

import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.mapper.SingleTasktableMapper;

public class EdrsSessionFactory extends AbstractProductTypeFactory implements ProductTypeFactory {		
	// TODO make configurable
    private static final String TASK_TABLE_NAME = "TaskTable.AIOP.xml";
    
	public EdrsSessionFactory() {
		super(ApplicationLevel.L0, new SingleTasktableMapper(TASK_TABLE_NAME));
	}

}

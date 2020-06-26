package esa.s1pdgs.cpoc.ipf.preparation.worker.type;

import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.mapper.TasktableMapper;

public abstract class AbstractProductTypeFactory implements ProductTypeFactory {	
	protected final ApplicationLevel level;
	protected final TasktableMapper taskTableMapper;
	
	public AbstractProductTypeFactory(final ApplicationLevel level, final TasktableMapper taskTableMapper) {
		this.level = level;
		this.taskTableMapper = taskTableMapper;
	}
	
	@Override
	public final ApplicationLevel level() {
		return level;
	}

	@Override
	public final TasktableMapper tasktableMapper() {
		return taskTableMapper;
	}	
}

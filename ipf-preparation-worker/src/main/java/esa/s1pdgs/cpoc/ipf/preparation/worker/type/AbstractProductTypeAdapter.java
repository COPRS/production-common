package esa.s1pdgs.cpoc.ipf.preparation.worker.type;

import esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.JobOrder;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.JobOrderProcParam;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.mapper.TasktableMapper;

public abstract class AbstractProductTypeAdapter implements ProductTypeAdapter {
	private final TasktableMapper taskTableMapper;
	
	public AbstractProductTypeAdapter(final TasktableMapper taskTableMapper) {
		this.taskTableMapper = taskTableMapper;
	}

	@Override
	public final TasktableMapper taskTableMapper() {
		return taskTableMapper;
	}
	
    final void updateProcParam(final JobOrder jobOrder, final String name, final String newValue) {
        boolean update = false;
        for (final JobOrderProcParam param : jobOrder.getConf().getProcParams()) {
            if (name.equals(param.getName())) {
                param.setValue(newValue);
                update = true;
            }
        }
        if (!update) {
            jobOrder.getConf().addProcParam(new JobOrderProcParam(name, newValue));
        }
    }
}

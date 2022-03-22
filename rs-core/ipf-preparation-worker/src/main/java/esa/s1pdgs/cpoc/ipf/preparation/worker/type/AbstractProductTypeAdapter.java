package esa.s1pdgs.cpoc.ipf.preparation.worker.type;

import esa.s1pdgs.cpoc.xml.model.joborder.JobOrder;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderProcParam;

public abstract class AbstractProductTypeAdapter implements ProductTypeAdapter {		
	
	protected final void updateProcParam(final JobOrder jobOrder, final String name, final String newValue) {
        if (jobOrder.getConf().getProcParams() == null) {
        	return;
        }
        // simply add parameter, if it has not been defined before
        if (!doUpdateProcParamIfDefined(jobOrder, name, newValue)) {
            jobOrder.getConf().addProcParam(new JobOrderProcParam(name, newValue));
        }
    }
	
	protected final void updateProcParamIfDefined(final JobOrder jobOrder, final String name, final String newValue) {
        if (jobOrder.getConf().getProcParams() == null) {
        	return;
        }
        doUpdateProcParamIfDefined(jobOrder, name, newValue);
	}
	
	private final boolean doUpdateProcParamIfDefined(final JobOrder jobOrder, final String name, final String newValue) {
        boolean update = false;
        for (final JobOrderProcParam param : jobOrder.getConf().getProcParams()) {
            if (name.equals(param.getName())) {
                param.setValue(newValue);
                update = true;
                break;
            }
        }
        return update;
	}
}

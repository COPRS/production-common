package esa.s1pdgs.cpoc.ipf.preparation.worker.type;

import esa.s1pdgs.cpoc.xml.model.joborder.JobOrder;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderProcParam;

public abstract class AbstractProductTypeAdapter implements ProductTypeAdapter {		
    protected final void updateProcParam(final JobOrder jobOrder, final String name, final String newValue) {
        if (jobOrder.getConf().getProcParams() == null) {
        	return;
        }
        
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

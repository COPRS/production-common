/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package esa.s1pdgs.cpoc.preparation.worker.type;

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

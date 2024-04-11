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

package esa.s1pdgs.cpoc.preparation.worker.type.spp;

import java.util.ArrayList;
import java.util.List;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobTaskInputs;
import esa.s1pdgs.cpoc.appcatalog.util.AppDataJobProductAdapter;
import esa.s1pdgs.cpoc.preparation.worker.type.AbstractProduct;

public class AuxResorbProduct extends AbstractProduct {
	private final List<AppDataJobTaskInputs> overridingInputs = new ArrayList<>();

    public AuxResorbProduct(final AppDataJobProductAdapter product) {
        super(product);
    }

    public static AuxResorbProduct of(final AppDataJob job) {
        return new AuxResorbProduct(new AppDataJobProductAdapter(job.getProduct()));
    }

    public void setStartTime(final String startTime) {
        product.setStartTime(startTime);
    }

    public void setStopTime(final String stopTime) {
        product.setStopTime(stopTime);
    }

    public void setSelectedOrbitFirstAzimuthTimeUtc(final String value) {
        product.setStringValue("selectedOrbitFirstAzimuthTimeUtc", value);
    }

    public String getSelectedOrbitFirstAzimuthTimeUtc() {
        return product.getStringValue("selectedOrbitFirstAzimuthTimeUtc");
    }
    
    public final void overridingInputs(final List<AppDataJobTaskInputs> inputs) {
    	overridingInputs.addAll(inputs);
    }
    
    @Override
	public List<AppDataJobTaskInputs> overridingInputs() {
		return overridingInputs;
	}
}

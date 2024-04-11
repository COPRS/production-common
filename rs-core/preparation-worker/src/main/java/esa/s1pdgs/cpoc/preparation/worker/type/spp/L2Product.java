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
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobTaskInputs;
import esa.s1pdgs.cpoc.appcatalog.util.AppDataJobProductAdapter;
import esa.s1pdgs.cpoc.preparation.worker.type.AbstractProduct;

public class L2Product extends AbstractProduct {
	
	private final List<AppDataJobTaskInputs> overridingInputs = new ArrayList<>();
	
	public L2Product(final AppDataJobProductAdapter product) {
		super(product);
	}

	public static final L2Product of(final AppDataJob job) {
		return of(job.getProduct());
	}
	
	public static final L2Product of(final AppDataJobProduct product) {
		return new L2Product(
				new AppDataJobProductAdapter(product)
		);
	}
	
	public void setStartTime(final String startTime) {
        product.setStartTime(startTime);
    }

    public void setStopTime(final String stopTime) {
        product.setStopTime(stopTime);
    }

    public final void overridingInputs(final List<AppDataJobTaskInputs> overridingInputs) {
    	this.overridingInputs.addAll(overridingInputs);
    }
    
    @Override
	public List<AppDataJobTaskInputs> overridingInputs() {
		return overridingInputs;
	}
}
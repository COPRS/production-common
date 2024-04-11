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

import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobPreselectedInput;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobTaskInputs;

public interface Product {
	public static Product nullProduct(final AppDataJob job) {
		return new Product() {
			@Override
			public AppDataJobProduct toProduct() {
				return job.getProduct();
			}
		};
	}

	Logger LOGGER = LogManager.getLogger(Product.class);

	AppDataJobProduct toProduct();

	default List<AppDataJobTaskInputs> overridingInputs() {
		return Collections.emptyList();
	}
	
	default List<AppDataJobPreselectedInput> preselectedInputs() {
		return Collections.emptyList();
	}
}
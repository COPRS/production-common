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

import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.util.AppDataJobProductAdapter;

public abstract class AbstractProduct implements Product {
	protected final AppDataJobProductAdapter product;

	public AbstractProduct(final AppDataJobProductAdapter product) {
		this.product = product;
	}	
	
	public final void setProductName(final String productName) {
		product.setProductName(productName);		
	}
	
	public final String getProductName() {
		return product.getProductName();
	}

	public final String getProductType() {
		return product.getProductType();
	}
	
	public final String getMissionId() {
		return product.getMissionId();
	}

	public final String getSatelliteId() {
		return product.getSatelliteId();
	}

	public final String getStartTime() {
		return product.getStartTime();
	}

	public String getStopTime() {
		return product.getStopTime();
	}	
	
	public final String getProcessMode() {
		return product.getProcessMode();
	}
	
	@Override
	public final AppDataJobProduct toProduct() {
		return product.toProduct();
	}	
}

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

package esa.s1pdgs.cpoc.datalifecycle.client.domain.model;

import esa.s1pdgs.cpoc.common.ProductFamily;

public class RetentionPolicy {

	private ProductFamily productFamily;
	private String filePattern;
	private int retentionTimeDays = -1;

	public ProductFamily getProductFamily() {
		return productFamily;
	}

	public void setProductFamily(ProductFamily productFamily) {
		this.productFamily = productFamily;
	}

	public String getFilePattern() {
		return filePattern;
	}

	public void setFilePattern(String filePattern) {
		this.filePattern = filePattern;
	}

	public int getRetentionTimeDays() {
		return this.retentionTimeDays;
	}

	public void setRetentionTimeDays(int retentionTimeDays) {
		this.retentionTimeDays = retentionTimeDays;
	}

	@Override
	public String toString() {
		return String.format("RetentionPolicy [productFamily=%s, filePattern=%s, retentionTimeDays=%s]", productFamily,
				filePattern, retentionTimeDays);
	}

}

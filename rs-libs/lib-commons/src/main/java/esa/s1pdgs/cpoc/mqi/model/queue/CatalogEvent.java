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

package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonIgnore;

import esa.s1pdgs.cpoc.mqi.model.control.AllowedAction;

public class CatalogEvent extends AbstractMessage {
	private static final String PRODUCT_NAME_KEY = "productName";
	private static final String PRODUCT_TYPE_KEY = "productType";

	public CatalogEvent() {
		super();
		setAllowedActions(Arrays.asList(AllowedAction.RESUBMIT));
	}

	@JsonIgnore
	public String getProductName() {
		return keyObjectStorage;
	}

	@JsonIgnore
	public String getMetadataProductName() {
		return metadata.getOrDefault(PRODUCT_NAME_KEY, "").toString();
	}

	public void setMetadataProductName(final String productName) {
		this.metadata.put(PRODUCT_NAME_KEY, productName);
	}

	@JsonIgnore
	public String getMetadataProductType() {
		return metadata.getOrDefault(PRODUCT_TYPE_KEY, "").toString();
	}

	public void setMetadataProductType(final String productType) {
		this.metadata.put(PRODUCT_TYPE_KEY, productType);
	}

	@Override
	public String toString() {
		return "CatalogEvent [productName=" + metadata.get(PRODUCT_NAME_KEY) + ", productType="
				+ metadata.get(PRODUCT_TYPE_KEY) + ", metadata=" + metadata + ", productFamily=" + productFamily
				+ ", keyObjectStorage=" + keyObjectStorage + ", storagePath=" + storagePath + ", creationDate="
				+ creationDate + ", podName=" + podName + ", uid=" + uid + ", rsChainVersion=" + rsChainVersion + "]";
	}
}
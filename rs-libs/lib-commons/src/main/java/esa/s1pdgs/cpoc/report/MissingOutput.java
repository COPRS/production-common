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

package esa.s1pdgs.cpoc.report;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MissingOutput {
	
	@JsonProperty("product_metadata_custom_object")
	private Map<String, Object> productMetadataCustomObject = new HashMap<>();
	
	@JsonProperty("end_to_end_product_boolean")
	private boolean endToEndProductBoolean;
	
	@JsonProperty("estimated_count_integer") 
	private int estimatedCountInteger;
	
	
	public boolean isEndToEndProductBoolean() {
		return endToEndProductBoolean;
	}

	public void setEndToEndProductBoolean(boolean endToEndProductBoolean) {
		this.endToEndProductBoolean = endToEndProductBoolean;
	}

	public int getEstimatedCountInteger() {
		return estimatedCountInteger;
	}

	public void setEstimatedCountInteger(int estimatedCountInteger) {
		this.estimatedCountInteger = estimatedCountInteger;
	}

	public Map<String, Object> getProductMetadataCustomObject() {
		return productMetadataCustomObject;
	}

	public void setProductMetadataCustomObject(Map<String, Object> productMetadataCustomObject) {
		this.productMetadataCustomObject = productMetadataCustomObject;
	}
	
}

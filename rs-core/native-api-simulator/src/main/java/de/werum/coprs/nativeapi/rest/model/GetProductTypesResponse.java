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

package de.werum.coprs.nativeapi.rest.model;

import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ProductTypes")
public class GetProductTypesResponse {

	@Schema(example = "[\"l1\", \"l2\", \"aux-safe\"]", description = "the names of the product types for a particular mission")
	private List<String> productTypes = new ArrayList<>();

	public GetProductTypesResponse(final List<String> productTypes) {
		this.productTypes = productTypes;
	}

	public List<String> getProductTypes() {
		return productTypes;
	}

	public void setProductTypes(final List<String> productTypes) {
		this.productTypes = productTypes;
	}
}

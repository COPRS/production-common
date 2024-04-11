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

@Schema(name = "Attributes")
public class GetAttributesResponse {

	@Schema(example = "[\"name\", \"publicationDate\", \"attr_beginningDateTime_date\", \"attr_orbitDirection_string\"]", description = "the attribute names for a particular mission or product type")
	private List<String> attributes = new ArrayList<>();

	public GetAttributesResponse(final List<String> attributes) {
		this.attributes = attributes;
	}

	public List<String> getAttributes() {
		return this.attributes;
	}

	public void setAttributes(final List<String> attributes) {
		this.attributes = attributes;
	}

}

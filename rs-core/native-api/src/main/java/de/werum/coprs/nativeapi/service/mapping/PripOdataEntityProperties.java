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

package de.werum.coprs.nativeapi.service.mapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum PripOdataEntityProperties {

	Id,
	Name,
	Online,
	ContentType,
	ContentLength,
	PublicationDate,
	EvictionDate,
	ProductionType,
	ContentDate,
	Start,
	End,
	Checksum,
	Algorithm,
	Value,
	ChecksumDate,
	Footprint,
	Attributes,
	StringAttributes,
	IntegerAttributes,
	DoubleAttributes,
	DateTimeOffsetAttributes,
	BooleanAttributes;

	public static List<PripOdataEntityProperties> getAttributesCollectionProperties() {
		return new ArrayList<>(Arrays.asList(
				Attributes,
				StringAttributes,
				IntegerAttributes,
				DoubleAttributes,
				DateTimeOffsetAttributes,
				BooleanAttributes));
	}

}

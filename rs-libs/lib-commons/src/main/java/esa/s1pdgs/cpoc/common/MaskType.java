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

package esa.s1pdgs.cpoc.common;

public enum MaskType {
	EW_SLC("MSK_EW_SLC"),
	LAND("MSK__LAND_"),
    OCEAN("MSK_OCEAN_"),
	OVERPASS("MSK_OVRPAS");
	
	private String productType;
	
	private MaskType(String productType) {
		this.productType = productType;
	}
	
	public String getProductType() {
		return productType;
	}
	
	@Override
	public String toString() {
		return name().toLowerCase() + " mask"; // friendly name ("land mask", "ocean mask", "overpass mask")
	}
	
	public static MaskType of(String productType) {	
		for (MaskType maskType : MaskType.values()) {
			if (maskType.getProductType().equals(productType)) {
				return maskType;
			}
		}
		throw new IllegalArgumentException(String.format("Cannot determine mask type for product type %s", productType));
	}

}

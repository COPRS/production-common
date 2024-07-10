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

package esa.s1pdgs.cpoc.prip.model;

public enum ProductionType {
	
	SYSTEMATIC_PRODUCTION(0, "systematic_production"),
	ON_DEMAND_DEFAULT(1, "on_demand_default"),
	ON_DEMAND_NON_DEFAULT(2, "on_demand_non_default");
	
	private static final long serialVersionUID = -2974165362740296325L;
	
	private final int value;
	private final String name;
	
	private ProductionType(final int value, final String name) {
        this.value = value;
        this.name = name;
    }
	
	public int getValue() {
        return value;
    }

	public String getName() {
        return name;
    }

}

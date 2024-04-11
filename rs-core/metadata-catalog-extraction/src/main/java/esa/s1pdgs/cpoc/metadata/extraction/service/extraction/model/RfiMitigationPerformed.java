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

package esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model;

public enum RfiMitigationPerformed {

	NOT_SUPPORTED("NotSupported"), NEVER("Never"), BASED_ON_NOISE_MEAS("BasedOnNoiseMeas"), ALWAYS("Always");
	
	private final String strRepresentation;
	
	RfiMitigationPerformed(String str) {
		this.strRepresentation = str;
	}
	
	public String stringRepresentation() {
		return strRepresentation;
	}
	
	public static RfiMitigationPerformed fromString(String str) {
		if (NOT_SUPPORTED.strRepresentation.equalsIgnoreCase(str)) {
			return NOT_SUPPORTED;
		} else if (NEVER.strRepresentation.equalsIgnoreCase(str)) {
			return NEVER;
		} else if (BASED_ON_NOISE_MEAS.strRepresentation.equalsIgnoreCase(str)) {
			return BASED_ON_NOISE_MEAS;
		} else if (ALWAYS.strRepresentation.equalsIgnoreCase(str)) {
			return ALWAYS;
		} else {
			throw new IllegalArgumentException("Not supported: " + str);
		}
	}
}

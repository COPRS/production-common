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

package esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files;

import java.util.regex.Matcher;

public class AuxFilenameMetadataExtractor {
	private final Matcher matcher;
	
	public AuxFilenameMetadataExtractor(final Matcher matcher) {
		this.matcher = matcher;
	}

	public boolean matches() {
		return matcher.matches();
	}
	
	public final String getMissionId() {
		return matcher.group(1);
	}
	
	public final String getSatelliteId() {
		return matcher.group(2);
	}
	
	public final String getProductClass() {
		return matcher.group(4);
	}
	
	public final String getFileType() {
		final String typeString = matcher.group(5);

		if (FileDescriptorBuilder.AUX_ECE_TYPES.contains(typeString)) {
			return "AUX_ECE";
		}
		return typeString;
	}
	
	public final String getExtension() {
		return matcher.group(6);
	}
}

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

package esa.s1pdgs.cpoc.report.message;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import esa.s1pdgs.cpoc.report.ReportingFilenameEntries;

public abstract class AbstractFilenameReportingProduct {
	@JsonIgnore
	private final List<String> filenames;

	protected AbstractFilenameReportingProduct(final ReportingFilenameEntries entries) {
		this.filenames = entries.getFilenames();
	}

	@JsonAnyGetter
	public final Map<String, Object> getFilenames() {
		if (filenames == null || filenames.size() == 0) {
			return null;
		} else if (filenames.size() == 1) {
			return Collections.singletonMap("filename_string", filenames.get(0));
		} else {
			return Collections.singletonMap("filename_strings", filenames);
		}
	}
}

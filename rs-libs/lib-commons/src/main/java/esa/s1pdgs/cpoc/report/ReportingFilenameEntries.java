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

import java.util.Collections;
import java.util.List;

public final class ReportingFilenameEntries {
	private final List<ReportingFilenameEntry> reportingEntries;

	public ReportingFilenameEntries(final ReportingFilenameEntry entry) {
		this.reportingEntries = Collections.singletonList(entry);
	}
	
	public ReportingFilenameEntries(final List<ReportingFilenameEntry> reportingEntries) {
		this.reportingEntries = reportingEntries;
	}
	
	public final List<String> getFilenames() {
		return ReportingUtils.filenamesOf(reportingEntries);
	}
	
	final List<ReportingFilenameEntry> entries() {
		return reportingEntries;
	}
}

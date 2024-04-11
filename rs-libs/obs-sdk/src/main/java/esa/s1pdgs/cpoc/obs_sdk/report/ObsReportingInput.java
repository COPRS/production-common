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

package esa.s1pdgs.cpoc.obs_sdk.report;

import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.report.ReportingFilenameEntries;
import esa.s1pdgs.cpoc.report.ReportingInput;
import esa.s1pdgs.cpoc.report.message.AbstractFilenameReportingProduct;

public final class ObsReportingInput extends AbstractFilenameReportingProduct implements ReportingInput {
	@JsonProperty("bucket_string")
	private String bucketName;
	
	public ObsReportingInput(final ReportingFilenameEntries entries, final String bucketName) {
		super(entries);
		this.bucketName = bucketName;
	}

	public String getBucketName() {
		return bucketName;
	}
}

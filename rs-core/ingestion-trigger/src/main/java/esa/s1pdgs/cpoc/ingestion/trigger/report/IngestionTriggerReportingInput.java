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

package esa.s1pdgs.cpoc.ingestion.trigger.report;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.report.ReportingFilenameEntries;
import esa.s1pdgs.cpoc.report.ReportingFilenameEntry;
import esa.s1pdgs.cpoc.report.ReportingInput;
import esa.s1pdgs.cpoc.report.message.AbstractFilenameReportingProduct;

public final class IngestionTriggerReportingInput extends AbstractFilenameReportingProduct implements ReportingInput {
	
	@JsonProperty("pickup_point_detection_date")
	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'000Z'", timezone="UTC")
	private Date pollingDate = new Date();
	
	@JsonProperty("pickup_point_available_date")
	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'000Z'", timezone="UTC")
	private Date availDate = new Date();

	public IngestionTriggerReportingInput(final ReportingFilenameEntries entry, final Date pollingDate, final Date availDate) {
		super(entry);
		this.pollingDate = pollingDate;
		this.availDate = availDate;
	}
	
	@JsonIgnore
	public static final IngestionTriggerReportingInput newInstance(
			final String productName,
			final ProductFamily family,
			final Date availDate
	) {
		return new IngestionTriggerReportingInput(
				new ReportingFilenameEntries(new ReportingFilenameEntry(family, productName)), 
				new Date(), 
				availDate
		);
	}

	public Date getPollingDate() {
		return pollingDate;
	}

	public void setPollingDate(final Date pollingDate) {
		this.pollingDate = pollingDate;
	}

	public Date getAvailDate() {
		return availDate;
	}

	public void setAvailDate(final Date availDate) {
		this.availDate = availDate;
	}
	
}

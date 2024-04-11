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

package esa.s1pdgs.cpoc.ingestion.worker.product.report;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.ingestion.worker.product.IngestionJobs;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;
import esa.s1pdgs.cpoc.report.ReportingFilenameEntries;
import esa.s1pdgs.cpoc.report.ReportingFilenameEntry;
import esa.s1pdgs.cpoc.report.ReportingOutput;
import esa.s1pdgs.cpoc.report.message.AbstractFilenameReportingProduct;

public class IngestionWorkerReportingOutput extends AbstractFilenameReportingProduct implements ReportingOutput {
	@JsonProperty("ingestion_date")
	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'000Z'", timezone="UTC")
	private Date ingestionDate = new Date();

	public IngestionWorkerReportingOutput(final ReportingFilenameEntries entry, final Date ingestionDate) {
		super(entry);
		this.ingestionDate = ingestionDate;
	}
	
	@JsonIgnore
	public static final IngestionWorkerReportingOutput newInstance(
			final IngestionJob ingestion, 
			final Date ingestionFinishedDate
	) {
		return new IngestionWorkerReportingOutput(
				new ReportingFilenameEntries(
						new ReportingFilenameEntry(ingestion.getProductFamily(), IngestionJobs.filename(ingestion))
				), 
				ingestionFinishedDate
		);
	}

	public Date getIngestionDate() {
		return ingestionDate;
	}
}

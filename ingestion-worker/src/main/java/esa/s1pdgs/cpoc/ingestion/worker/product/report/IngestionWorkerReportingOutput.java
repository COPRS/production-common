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

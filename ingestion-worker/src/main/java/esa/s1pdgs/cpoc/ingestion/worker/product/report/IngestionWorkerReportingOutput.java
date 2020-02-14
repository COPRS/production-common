package esa.s1pdgs.cpoc.ingestion.worker.product.report;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.report.message.output.FilenameReportingOutput;

public class IngestionWorkerReportingOutput extends FilenameReportingOutput {

	@JsonProperty("ingestion_date")
	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'000Z'", timezone="UTC")
	private Date ingestionDate = new Date();
	
	public IngestionWorkerReportingOutput() {		
	}

	public IngestionWorkerReportingOutput(final String filename, final Date ingestionDate) {
		super(filename);
		this.ingestionDate = ingestionDate;
	}

	public Date getIngestionDate() {
		return ingestionDate;
	}

	public void setIngestionDate(final Date ingestionDate) {
		this.ingestionDate = ingestionDate;
	}
}

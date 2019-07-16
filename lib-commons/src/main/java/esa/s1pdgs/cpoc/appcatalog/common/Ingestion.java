package esa.s1pdgs.cpoc.appcatalog.common;

import java.util.Date;
import java.util.Objects;

import org.springframework.data.annotation.Id;

import esa.s1pdgs.cpoc.mqi.model.queue.IngestionDto;

public class Ingestion {
	
	@Id
	private long id;
	
	private Date pollingDate;
	
	private IngestionDto ingestion;
	
	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final Ingestion other = (Ingestion) o;

		return Objects.equals(this.id, other.id)
				&& Objects.equals(this.pollingDate, other.pollingDate)
				&& Objects.equals(this.ingestion, other.ingestion);
	}
	
	@Override
	public String toString() {
		return "Ingestion [id=" + id + ", pollingDate=" + pollingDate + ", ingestion=" + ingestion + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, pollingDate, ingestion);
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Date getPollingDate() {
		return pollingDate;
	}

	public void setPollingDate(Date pollingDate) {
		this.pollingDate = pollingDate;
	}

	public IngestionDto getIngestion() {
		return ingestion;
	}

	public void setIngestion(IngestionDto ingestion) {
		this.ingestion = ingestion;
	}
	
	
	
	
}

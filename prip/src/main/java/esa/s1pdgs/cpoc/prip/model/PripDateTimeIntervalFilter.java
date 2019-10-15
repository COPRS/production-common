package esa.s1pdgs.cpoc.prip.model;

import java.time.LocalDateTime;

public class PripDateTimeIntervalFilter {

	private LocalDateTime dateTimeStart;
	private LocalDateTime dateTimeStop;

	public LocalDateTime getDateTimeStart() {
		return dateTimeStart;
	}

	public void setDateTimeStart(LocalDateTime dateTimeStart) {
		this.dateTimeStart = dateTimeStart;
	}

	public LocalDateTime getDateTimeStop() {
		return dateTimeStop;
	}

	public void setDateTimeStop(LocalDateTime dateTimeStop) {
		this.dateTimeStop = dateTimeStop;
	}
}

package esa.s1pdgs.cpoc.mqi.model.queue;

import java.time.LocalDateTime;

public class PripJob extends AbstractMessage {
	private LocalDateTime evictionDate;

	public LocalDateTime getEvictionDate() {
		return evictionDate;
	}

	public void setEvictionDate(LocalDateTime evictionDate) {
		this.evictionDate = evictionDate;
	}

}

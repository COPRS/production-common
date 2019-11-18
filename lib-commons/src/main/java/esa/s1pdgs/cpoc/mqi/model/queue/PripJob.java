package esa.s1pdgs.cpoc.mqi.model.queue;

import java.time.LocalDateTime;

public class PripJob {

	private String obsKey;
	private LocalDateTime evictionDate;

	public String getObsKey() {
		return obsKey;
	}

	public void setObsKey(String obsKey) {
		this.obsKey = obsKey;
	}

	public LocalDateTime getEvictionDate() {
		return evictionDate;
	}

	public void setEvictionDate(LocalDateTime evictionDate) {
		this.evictionDate = evictionDate;
	}

}

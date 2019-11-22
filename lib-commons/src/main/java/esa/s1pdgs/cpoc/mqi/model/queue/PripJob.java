package esa.s1pdgs.cpoc.mqi.model.queue;

import java.time.LocalDateTime;

public class PripJob extends AbstractMessage {
	private String keyObjectStorage;
	private LocalDateTime evictionDate;

	public String getKeyObjectStorage() {
		return keyObjectStorage;
	}

	public void setKeyObjectStorage(String keyObjectStorage) {
		this.keyObjectStorage = keyObjectStorage;
	}

	public LocalDateTime getEvictionDate() {
		return evictionDate;
	}

	public void setEvictionDate(LocalDateTime evictionDate) {
		this.evictionDate = evictionDate;
	}

}

package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Date;
import java.util.Objects;

public class PripJob extends AbstractMessage {
	private Date evictionDate;

	public Date getEvictionDate() {
		return evictionDate;
	}

	public void setEvictionDate(final Date evictionDate) {
		this.evictionDate = evictionDate;
	}

	@Override
	public int hashCode() {
		return Objects.hash(creationDate, evictionDate, hostname, keyObjectStorage, productFamily);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final PripJob other = (PripJob) obj;
		return Objects.equals(creationDate, other.creationDate) 
				&& Objects.equals(evictionDate, other.evictionDate)
				&& Objects.equals(hostname, other.hostname) 
				&& Objects.equals(keyObjectStorage, other.keyObjectStorage)
				&& productFamily == other.productFamily;
	}

	@Override
	public String toString() {
		return "PripJob [productFamily=" + productFamily + ", keyObjectStorage=" + keyObjectStorage + ", creationDate="
				+ creationDate + ", hostname=" + hostname + ", evictionDate=" + evictionDate + "]";
	}
}

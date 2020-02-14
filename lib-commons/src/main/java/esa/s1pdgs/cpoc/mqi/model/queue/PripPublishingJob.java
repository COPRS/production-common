package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Date;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

public class PripPublishingJob extends AbstractMessage {	
	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone="UTC")
	private Date evictionDate;

	public Date getEvictionDate() {
		return evictionDate;
	}

	public void setEvictionDate(final Date evictionDate) {
		this.evictionDate = evictionDate;
	}

	@Override
	public int hashCode() {
		return Objects.hash(creationDate, evictionDate, hostname, keyObjectStorage, productFamily, uid);
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
		final PripPublishingJob other = (PripPublishingJob) obj;
		return Objects.equals(creationDate, other.creationDate) 
				&& Objects.equals(evictionDate, other.evictionDate)
				&& Objects.equals(hostname, other.hostname) 
				&& Objects.equals(keyObjectStorage, other.keyObjectStorage)
				&& Objects.equals(uid, other.uid)
				&& productFamily == other.productFamily;
	}

	@Override
	public String toString() {
		return "PripPublishingJob [productFamily=" + productFamily + ", keyObjectStorage=" + keyObjectStorage + ", creationDate="
				+ creationDate + ", hostname=" + hostname + ", evictionDate=" + evictionDate + ", uid=" + uid +"]";
	}
}

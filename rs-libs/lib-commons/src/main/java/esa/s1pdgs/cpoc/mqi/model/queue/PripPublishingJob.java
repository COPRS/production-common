package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Arrays;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import esa.s1pdgs.cpoc.mqi.model.control.AllowedAction;

public class PripPublishingJob extends AbstractMessage {
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
	private Date evictionDate;

	public PripPublishingJob() {
		super();
		setAllowedActions(Arrays.asList(AllowedAction.RESTART));
	}

	public Date getEvictionDate() {
		return evictionDate;
	}

	public void setEvictionDate(final Date evictionDate) {
		this.evictionDate = evictionDate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((evictionDate == null) ? 0 : evictionDate.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		PripPublishingJob other = (PripPublishingJob) obj;
		if (evictionDate == null) {
			if (other.evictionDate != null)
				return false;
		} else if (!evictionDate.equals(other.evictionDate))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PripPublishingJob [productFamily=" + productFamily + ", keyObjectStorage=" + keyObjectStorage
				+ ", storagePath=" + storagePath + ", creationDate=" + creationDate + ", podName=" + podName
				+ ", evictionDate=" + evictionDate + ", uid=" + uid + + ", rsChainVersion=" + rsChainVersion "]";
	}

}

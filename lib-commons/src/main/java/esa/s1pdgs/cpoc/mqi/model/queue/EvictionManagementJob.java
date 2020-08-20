package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

import esa.s1pdgs.cpoc.mqi.model.control.ControlAction;

public class EvictionManagementJob extends AbstractMessage {

	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
	private Date evictionDate;

	private boolean unlimited;
	
	public EvictionManagementJob() {
		super();
		setAllowedControlActions(Arrays.asList(ControlAction.RESTART));
	}

	public Date getEvictionDate() {
		return evictionDate;
	}

	public void setEvictionDate(final Date evictionDate) {
		this.evictionDate = evictionDate;
	}

	public boolean isUnlimited() {
		return unlimited;
	}

	public void setUnlimited(boolean unlimited) {
		this.unlimited = unlimited;
	}

	@Override
	public int hashCode() {
		return Objects.hash(evictionDate, unlimited, creationDate, hostname, keyObjectStorage, productFamily, uid);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof EvictionManagementJob))
			return false;
		EvictionManagementJob other = (EvictionManagementJob) obj;
		return Objects.equals(evictionDate, other.evictionDate) && Objects.equals(creationDate, other.creationDate)
				&& Objects.equals(hostname, other.hostname) && Objects.equals(keyObjectStorage, other.keyObjectStorage)
				&& Objects.equals(uid, other.uid) && productFamily == other.productFamily
				&& unlimited == other.unlimited;
	}

	@Override
	public String toString() {
		return String.format(
				"EvictionManagementJob [productFamily=%s, keyObjectStorage=%s, creationDate=%s, unlimited=%s, evictionDate=%s]",
				productFamily, keyObjectStorage, creationDate, unlimited, evictionDate);
	}

}

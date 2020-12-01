package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Objects;

public class DisseminationJob extends AbstractMessage {
	
	public DisseminationJob() {
		super();
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(creationDate, hostname, keyObjectStorage, productFamily, uid,
				allowedActions, demandType, debug, retryCounter);
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
		final DisseminationJob other = (DisseminationJob) obj;
		return Objects.equals(creationDate, other.creationDate) 
				&& Objects.equals(hostname, other.hostname) 
				&& Objects.equals(keyObjectStorage, other.keyObjectStorage)
				&& Objects.equals(uid, other.uid)
				&& productFamily == other.productFamily
				&& Objects.equals(allowedActions, other.getAllowedActions())
		        && demandType == other.demandType
		        && debug == other.debug
		        && retryCounter == other.retryCounter;
	}
	
	@Override
	public String toString() {
		return "DisseminatonJob [productFamily=" + productFamily + ", keyObjectStorage=" + keyObjectStorage + ", uid="
				+ uid + ", creationDate=" + creationDate + ", hostname=" + hostname + ", allowedActions=" + allowedActions 
				+ ", demandType=" + demandType + ", retryCounter=" + retryCounter + ", debug=" + debug + "]";
	}	

}

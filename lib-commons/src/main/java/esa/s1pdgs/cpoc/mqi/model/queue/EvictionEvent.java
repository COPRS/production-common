package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Arrays;
import java.util.Objects;

import esa.s1pdgs.cpoc.mqi.model.control.AllowedAction;

public class EvictionEvent extends AbstractMessage {
	
	private String operatorName;
	
	public EvictionEvent() {
		super();
		setAllowedActions(Arrays.asList(AllowedAction.RESTART));
	}

	public String getOperatorName() {
		return operatorName;
	}

	public void setOperatorName(String operatorName) {
		this.operatorName = operatorName;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(creationDate, hostname, keyObjectStorage, productFamily, uid,
				allowedActions, demandType, debug, retryCounter, operatorName);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof EvictionEvent))
			return false;
		EvictionEvent other = (EvictionEvent) obj;
		return Objects.equals(creationDate, other.creationDate) 
				&& Objects.equals(hostname, other.hostname)
				&& Objects.equals(keyObjectStorage, other.keyObjectStorage) 
				&& Objects.equals(uid, other.uid)
				&& productFamily == other.productFamily
		        && Objects.equals(allowedActions, other.getAllowedActions())
		        && demandType == other.demandType
		        && debug == other.debug
		        && retryCounter == other.retryCounter
				&& Objects.equals(operatorName, other.operatorName);
	}

	@Override
	public String toString() {
		return "EvictionEvent [operatorName=" + operatorName + ", productFamily=" + productFamily
				+ ", keyObjectStorage=" + keyObjectStorage + ", uid=" + uid + ", creationDate=" + creationDate
				+ ", hostname=" + hostname + ", allowedActions=" + allowedActions + ", demandType=" + demandType
				+ ", retryCounter=" + retryCounter + ", debug=" + debug + "]";
	}
	
	

}

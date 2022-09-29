package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Arrays;

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
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((operatorName == null) ? 0 : operatorName.hashCode());
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
		EvictionEvent other = (EvictionEvent) obj;
		if (operatorName == null) {
			if (other.operatorName != null)
				return false;
		} else if (!operatorName.equals(other.operatorName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "EvictionEvent [operatorName=" + operatorName + ", productFamily=" + productFamily
				+ ", keyObjectStorage=" + keyObjectStorage + ", storagePath=" + storagePath + ", uid=" + uid
				+ ", creationDate=" + creationDate + ", podName=" + podName + ", allowedActions=" + allowedActions
				+ ", demandType=" + demandType + ", retryCounter=" + retryCounter + ", debug=" + debug + "]";
	}

}

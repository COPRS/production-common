package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Arrays;
import java.util.Objects;

import esa.s1pdgs.cpoc.mqi.model.control.AllowedAction;

public class DataRequestEvent extends AbstractMessage {
	
	private DataRequestType dataRequestType;
	
	public DataRequestEvent() {
		super();
		setAllowedActions(Arrays.asList(AllowedAction.RESTART));
	}

	public DataRequestType getDataRequestType() {
		return dataRequestType;
	}

	public void setDataRequestType(DataRequestType dataRequestType) {
		this.dataRequestType = dataRequestType;
	}

	@Override
	public int hashCode() {
		return Objects.hash(creationDate, hostname, keyObjectStorage, productFamily, uid,
				allowedActions, demandType, debug, retryCounter, dataRequestType);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof DataRequestEvent))
			return false;
		DataRequestEvent other = (DataRequestEvent) obj;
		return Objects.equals(creationDate, other.creationDate) 
				&& Objects.equals(hostname, other.hostname)
				&& Objects.equals(keyObjectStorage, other.keyObjectStorage) 
				&& Objects.equals(uid, other.uid)
				&& productFamily == other.productFamily
		        && Objects.equals(allowedActions, other.getAllowedActions())
		        && demandType == other.demandType
		        && debug == other.debug
		        && retryCounter == other.retryCounter
				&& dataRequestType == other.dataRequestType;
	}

	@Override
	public String toString() {
		return "DataRequestEvent [dataRequestType=" + dataRequestType + ", productFamily=" + productFamily
				+ ", keyObjectStorage=" + keyObjectStorage + ", uid=" + uid + ", creationDate=" + creationDate
				+ ", hostname=" + hostname + ", allowedActions=" + allowedActions + ", demandType=" + demandType
				+ ", retryCounter=" + retryCounter + ", debug=" + debug + "]";
	}

}

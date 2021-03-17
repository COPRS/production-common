package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Objects;

public class DataRequestJob extends AbstractMessage {

	private String operatorName;

	// --------------------------------------------------------------------------

	public DataRequestJob() {
		super();
	}

	// --------------------------------------------------------------------------

	@Override
	public int hashCode() {
		return Objects.hash(this.operatorName, this.creationDate, this.hostname, this.keyObjectStorage, this.productFamily, this.uid, this.allowedActions,
				this.demandType, this.debug, this.retryCounter);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof DataRequestJob)) {
			return false;
		}
		final DataRequestJob other = (DataRequestJob) obj;
		return Objects.equals(this.operatorName, other.operatorName) && Objects.equals(this.creationDate, other.creationDate)
				&& Objects.equals(this.hostname, other.hostname) && Objects.equals(this.keyObjectStorage, other.keyObjectStorage)
				&& Objects.equals(this.uid, other.uid) && this.productFamily == other.productFamily
				&& Objects.equals(this.allowedActions, other.getAllowedActions()) && this.demandType == other.demandType && this.debug == other.debug
				&& this.retryCounter == other.retryCounter;
	}

	@Override
	public String toString() {
		return String.format("EvictionManagementJob [productFamily=%s, keyObjectStorage=%s, creationDate=%s, operatorName=%s]", this.productFamily,
				this.keyObjectStorage, this.creationDate, this.operatorName);
	}

	// --------------------------------------------------------------------------

	public String getOperatorName() {
		return this.operatorName;
	}

	public void setOperatorName(String operatorName) {
		this.operatorName = operatorName;
	}

}

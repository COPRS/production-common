package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Arrays;

import esa.s1pdgs.cpoc.mqi.model.control.AllowedAction;

public class DataRequestJob extends AbstractMessage {

	private String operatorName;

	// --------------------------------------------------------------------------

	public DataRequestJob() {
		super();
		this.setAllowedActions(Arrays.asList(AllowedAction.RESTART));
	}

	// --------------------------------------------------------------------------
	
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
		DataRequestJob other = (DataRequestJob) obj;
		if (operatorName == null) {
			if (other.operatorName != null)
				return false;
		} else if (!operatorName.equals(other.operatorName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("DataRequestJob [productFamily=%s, keyObjectStorage=%s, creationDate=%s, operatorName=%s]", this.productFamily,
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

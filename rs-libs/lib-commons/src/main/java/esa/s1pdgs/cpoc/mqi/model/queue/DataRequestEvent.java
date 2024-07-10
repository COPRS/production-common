/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Arrays;

import esa.s1pdgs.cpoc.mqi.model.control.AllowedAction;

public class DataRequestEvent extends AbstractMessage {

	private DataRequestType dataRequestType;
	private String operatorName;

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
		result = prime * result + ((dataRequestType == null) ? 0 : dataRequestType.hashCode());
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
		DataRequestEvent other = (DataRequestEvent) obj;
		if (dataRequestType != other.dataRequestType)
			return false;
		if (operatorName == null) {
			if (other.operatorName != null)
				return false;
		} else if (!operatorName.equals(other.operatorName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DataRequestEvent [operatorName=" + operatorName + ", dataRequestType=" + dataRequestType
				+ ", productFamily=" + productFamily + ", keyObjectStorage=" + keyObjectStorage + ", storagePath="
				+ storagePath + ", uid=" + uid + ", creationDate=" + creationDate + ", podName=" + podName
				+ ", allowedActions=" + allowedActions + ", demandType=" + demandType + ", retryCounter=" + retryCounter
				+ ", debug=" + debug + ", rsChainVersion=" + rsChainVersion + "]";
	}

}

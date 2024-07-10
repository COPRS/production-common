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

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mqi.model.control.AllowedAction;
import esa.s1pdgs.cpoc.mqi.model.control.DemandType;

/**
 * DTO used to (re)submit specific workflows and steps to be executed.
 * 
 * @author nicolas_kukolja
 */
public class OnDemandEvent extends AbstractMessage {

	private String productName;
	private String mode = "NOMINAL";
	private ApplicationLevel productionType;
	private String productType;
	private Map<String, Object> metadata;
	private String tasktableName = null;
	private String outputProductType = null;

	public OnDemandEvent() {
		super();
	}

	public OnDemandEvent(final ProductFamily productFamily, final String keyObjectStorage, final String productName,
			final ApplicationLevel productionType, final String mode) {
		super(productFamily, keyObjectStorage);

		this.productName = productName;
		this.productionType = productionType;
		this.mode = mode;

		this.uid = UUID.randomUUID();

		this.allowedActions = Collections.singletonList(AllowedAction.RESUBMIT);
		this.demandType = DemandType.OPERATOR_DEMAND;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(final String productName) {
		this.productName = productName;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(final String mode) {
		this.mode = mode;
	}

	public ApplicationLevel getProductionType() {
		return productionType;
	}

	public void setProductionType(final ApplicationLevel productionType) {
		this.productionType = productionType;
	}

	public String getProductType() {
		return productType;
	}

	public void setProductType(final String productType) {
		this.productType = productType;
	}

	public String getTasktableName() {
		return tasktableName;
	}

	public void setTasktableName(final String tasktableName) {
		this.tasktableName = tasktableName;
	}

	public String getOutputProductType() {
		return outputProductType;
	}

	public void setOutputProductType(final String outputProductType) {
		this.outputProductType = outputProductType;
	}

	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(final Map<String, Object> metadata) {
		this.metadata = metadata;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((metadata == null) ? 0 : metadata.hashCode());
		result = prime * result + ((mode == null) ? 0 : mode.hashCode());
		result = prime * result + ((outputProductType == null) ? 0 : outputProductType.hashCode());
		result = prime * result + ((productName == null) ? 0 : productName.hashCode());
		result = prime * result + ((productType == null) ? 0 : productType.hashCode());
		result = prime * result + ((productionType == null) ? 0 : productionType.hashCode());
		result = prime * result + ((tasktableName == null) ? 0 : tasktableName.hashCode());
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
		OnDemandEvent other = (OnDemandEvent) obj;
		if (metadata == null) {
			if (other.metadata != null)
				return false;
		} else if (!metadata.equals(other.metadata))
			return false;
		if (mode == null) {
			if (other.mode != null)
				return false;
		} else if (!mode.equals(other.mode))
			return false;
		if (outputProductType == null) {
			if (other.outputProductType != null)
				return false;
		} else if (!outputProductType.equals(other.outputProductType))
			return false;
		if (productName == null) {
			if (other.productName != null)
				return false;
		} else if (!productName.equals(other.productName))
			return false;
		if (productType == null) {
			if (other.productType != null)
				return false;
		} else if (!productType.equals(other.productType))
			return false;
		if (productionType != other.productionType)
			return false;
		if (tasktableName == null) {
			if (other.tasktableName != null)
				return false;
		} else if (!tasktableName.equals(other.tasktableName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "OnDemandEvent [productName=" + productName + ", mode=" + mode + ", productionType=" + productionType
				+ ", productType=" + productType + ", metadata=" + metadata + ", productFamily=" + productFamily
				+ ", keyObjectStorage=" + keyObjectStorage + ", storagePath=" + storagePath + ", uid=" + uid
				+ ", creationDate=" + creationDate + ", podName=" + podName + ", allowedActions=" + allowedActions
				+ ", demandType=" + demandType + ", retryCounter=" + retryCounter + ", debug=" + debug
				+ ", tasktableName=" + tasktableName + ", outputProductType=" + outputProductType + ", rsChainVersion="
				+ rsChainVersion + "]";
	}

}

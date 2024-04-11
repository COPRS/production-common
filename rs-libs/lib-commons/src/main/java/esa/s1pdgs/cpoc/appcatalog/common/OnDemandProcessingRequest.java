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

package esa.s1pdgs.cpoc.appcatalog.common;

public class OnDemandProcessingRequest {

	private String productName;
	private boolean debug = false;
	private String mode;
	private String productionType;
	private String tasktableName = null;
	private String outputProductType = null;

	public OnDemandProcessingRequest() {
	}

	public OnDemandProcessingRequest(final String productName, final boolean debug, final String mode, final String productionType) {
		super();
		this.productName = productName;
		this.debug = debug;
		this.mode = mode;
		this.productionType = productionType;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(final String productName) {
		this.productName = productName;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(final boolean debug) {
		this.debug = debug;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(final String mode) {
		this.mode = mode;
	}

	public String getProductionType() {
		return productionType;
	}

	public void setProductionType(final String productionType) {
		this.productionType = productionType;
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

	@Override
	public String toString() {
		return "OnDemandProcessingRequest [productName=" + productName + ", debug=" + debug + ", mode=" + mode
				+ ", productionType=" + productionType 
				+ ", tasktableName=" + tasktableName 
				+ ", outputProductType=" + outputProductType + "]";
	}

}

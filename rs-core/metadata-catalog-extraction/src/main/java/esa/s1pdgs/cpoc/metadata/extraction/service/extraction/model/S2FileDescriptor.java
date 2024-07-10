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

package esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model;

import java.util.Objects;

public class S2FileDescriptor extends AbstractFileDescriptor {

	private String instrumentShortName;
	
	public String getInstrumentShortName() {
		return instrumentShortName;
	}

	public void setInstrumentShortName(String instrumentShortName) {
		this.instrumentShortName = instrumentShortName;
	}

	@Override
	public int hashCode() {
		return Objects.hash(extension, filename, keyObjectStorage, missionId, mode, productClass, productFamily,
				productName, productType, relativePath, satelliteId, instrumentShortName);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		S2FileDescriptor other = (S2FileDescriptor) obj;
		return extension == other.extension && Objects.equals(filename, other.filename)
				&& Objects.equals(keyObjectStorage, other.keyObjectStorage)
				&& Objects.equals(missionId, other.missionId) && Objects.equals(mode, other.mode)
				&& Objects.equals(productClass, other.productClass) && productFamily == other.productFamily
				&& Objects.equals(productName, other.productName) && Objects.equals(productType, other.productType)
				&& Objects.equals(relativePath, other.relativePath) && Objects.equals(satelliteId, other.satelliteId)
				&& Objects.equals(instrumentShortName, other.instrumentShortName);
	}

	@Override
	public String toString() {
		return "S2FileDescriptor [productType=" + productType + ", productClass=" + productClass + ", relativePath="
				+ relativePath + ", filename=" + filename + ", extension=" + extension + ", productName=" + productName
				+ ", missionId=" + missionId + ", satelliteId=" + satelliteId + ", keyObjectStorage=" + keyObjectStorage
				+ ", productFamily=" + productFamily + ", mode=" + mode + ", instrumentShortName" + instrumentShortName
				+ "]";
	}

}

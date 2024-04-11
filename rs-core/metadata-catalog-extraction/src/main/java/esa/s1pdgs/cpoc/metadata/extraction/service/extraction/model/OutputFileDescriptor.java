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

/**
 * Class describing a output file
 * 
 * @author Olivier Bex-Chauvet
 *
 */
public class OutputFileDescriptor extends AbstractFileDescriptor {
	private String resolution;
	private String swathtype;
	private String polarisation;
	private String dataTakeId;

	@Override
	public String getProductType() {
		return productType;
	}

	@Override
	public void setProductType(final String productType) {
		this.productType = productType;
	}

	@Override
	public String getProductClass() {
		return productClass;
	}

	@Override
	public void setProductClass(final String productClass) {
		this.productClass = productClass;
	}

	public String getResolution() {
		return resolution;
	}

	public void setResolution(final String resolution) {
		this.resolution = resolution;
	}

	public String getSwathtype() {
		return swathtype;
	}

	public void setSwathtype(final String swathtype) {
		this.swathtype = swathtype;
	}

	public String getPolarisation() {
		return polarisation;
	}

	public void setPolarisation(final String polarisation) {
		this.polarisation = polarisation;
	}
	
	public String getDataTakeId() {
		return dataTakeId;
	}

	public void setDataTakeId(final String dataTakeId) {
		this.dataTakeId = dataTakeId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(dataTakeId, extension, filename, keyObjectStorage, missionId, mode, polarisation,
				productClass, productFamily, productName, productType, relativePath, resolution, satelliteId, swathtype);
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
		final OutputFileDescriptor other = (OutputFileDescriptor) obj;
		return Objects.equals(dataTakeId, other.dataTakeId) && extension == other.extension
				&& Objects.equals(filename, other.filename) && Objects.equals(keyObjectStorage, other.keyObjectStorage)
				&& Objects.equals(missionId, other.missionId) && Objects.equals(mode, other.mode)
				&& Objects.equals(polarisation, other.polarisation) && Objects.equals(productClass, other.productClass)
				&& productFamily == other.productFamily && Objects.equals(productName, other.productName)
				&& Objects.equals(productType, other.productType) && Objects.equals(relativePath, other.relativePath)
				&& Objects.equals(resolution, other.resolution) && Objects.equals(satelliteId, other.satelliteId)
				&& Objects.equals(swathtype, other.swathtype);
	}

	@Override
	public String toString() {
		return "OutputFileDescriptor [productType=" + productType + ", productClass=" + productClass + ", relativePath="
				+ relativePath + ", filename=" + filename + ", extension=" + extension + ", productName=" + productName
				+ ", missionId=" + missionId + ", satelliteId=" + satelliteId + ", keyObjectStorage=" + keyObjectStorage
				+ ", productFamily=" + productFamily + ", mode=" + mode + ", resolution=" + resolution + ", swathtype="
				+ swathtype + ", polarisation=" + polarisation + ", dataTakeId=" + dataTakeId + "]";
	}

}

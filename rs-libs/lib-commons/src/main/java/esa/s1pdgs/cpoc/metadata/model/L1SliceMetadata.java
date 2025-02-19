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

package esa.s1pdgs.cpoc.metadata.model;

import java.util.Objects;

/**
 * 
 * @author Cyrielle Gailliard
 *
 */
public class L1SliceMetadata extends AbstractMetadata {    
	/**
	 * Instrument configuration identifier
	 */
	private int instrumentConfigurationId;

	/**
	 * Slice number
	 */
	private int numberSlice;

	/**
	 * Data take identifier
	 */
	private String datatakeId;

	/**
	 * @param instrumentConfigurationId
	 * @param numberSlice
	 */
	public L1SliceMetadata(final String productName, final String productType, final String keyObjectStorage,
			final String validityStart, final String validityStop, final String missionId, final String satelliteId, final String stationCode,
			final int instrumentConfigurationId, final int numberSlice, final String dataTakeId) {
		super(productName, productType, keyObjectStorage, validityStart, validityStop, missionId, satelliteId, stationCode);
		this.instrumentConfigurationId = instrumentConfigurationId;
		this.numberSlice = numberSlice;
		this.datatakeId = dataTakeId;
	}
	
	public L1SliceMetadata(final String productName, final String productType, final String keyObjectStorage,
			final String validityStart, final String validityStop, final String missionId, final String satelliteId, final String stationCode) {
		super(productName, productType, keyObjectStorage, validityStart, validityStop, missionId, satelliteId, stationCode);
	}
	
	public L1SliceMetadata() {
		
	}
	/**
	 * @return the instrumentConfigurationId
	 */
	public int getInstrumentConfigurationId() {
		return instrumentConfigurationId;
	}

	/**
	 * @param instrumentConfigurationId
	 *            the instrumentConfigurationId to set
	 */
	public void setInstrumentConfigurationId(final int instrumentConfigurationId) {
		this.instrumentConfigurationId = instrumentConfigurationId;
	}

	/**
	 * @return the numberSlice
	 */
	public int getNumberSlice() {
		return numberSlice;
	}

	/**
	 * @param numberSlice
	 *            the numberSlice to set
	 */
	public void setNumberSlice(final int numberSlice) {
		this.numberSlice = numberSlice;
	}

	/**
	 * @return the datatakeId
	 */
	public String getDatatakeId() {
		return datatakeId;
	}

	/**
	 * @param datatakeId
	 *            the datatakeId to set
	 */
	public void setDatatakeId(final String datatakeId) {
		this.datatakeId = datatakeId;
	}

	public String toJsonString() {
		final String superToString = super.toAbstractString();
		return String.format("{%s,\"instrumentConfigurationId\":%s,\"numberSlice\":%s,\"datatakeId\":\"%s\"}", superToString,
				instrumentConfigurationId, numberSlice, datatakeId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(
				additionalProperties,
				datatakeId,
				instrumentConfigurationId,
				keyObjectStorage,
				missionId,
				numberSlice,
				productName,
				productType,
				satelliteId,
				stationCode,
				swathtype,
				validityStart,
				validityStop);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final L1SliceMetadata other = (L1SliceMetadata) obj;
		return Objects.equals(additionalProperties, other.additionalProperties)
				&& Objects.equals(datatakeId, other.datatakeId)
				&& instrumentConfigurationId == other.instrumentConfigurationId
				&& Objects.equals(keyObjectStorage, other.keyObjectStorage)
				&& Objects.equals(missionId, other.missionId) 
				&& numberSlice == other.numberSlice
				&& Objects.equals(productName, other.productName) 
				&& Objects.equals(productType, other.productType)
				&& Objects.equals(satelliteId, other.satelliteId) 
				&& Objects.equals(stationCode, other.stationCode)
				&& Objects.equals(swathtype, other.swathtype) 
				&& Objects.equals(validityStart, other.validityStart)
				&& Objects.equals(validityStop, other.validityStop);
	}

	@Override
	public String toString() {
		return "L1SliceMetadata [instrumentConfigurationId=" + instrumentConfigurationId + ", numberSlice="
				+ numberSlice + ", datatakeId=" + datatakeId + ", productName=" + productName + ", productType="
				+ productType + ", keyObjectStorage=" + keyObjectStorage + ", validityStart=" + validityStart
				+ ", validityStop=" + validityStop + ", missionId=" + missionId + ", satelliteId=" + satelliteId
				+ ", stationCode=" + stationCode + "]";
	}
	
	
}

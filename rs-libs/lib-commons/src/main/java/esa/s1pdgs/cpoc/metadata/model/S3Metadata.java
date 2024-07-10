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
 * Describes metadata returned by queries for the S3 mission
 * 
 * @author Julian Kaping
 *
 */
public class S3Metadata extends AbstractMetadata {
	private String absoluteStartOrbit;
	private String anxTime;
	private String anx1Time;
	private String creationTime;
	private String dumpStart;
	private int granuleNumber;
	private String granulePosition;
	private String insertionTime;

	public S3Metadata(final String productName, final String productType, final String keyObjectStorage,
			final String validityStart, final String validityStop, final String missionId, final String satelliteId,
			final String stationCode, final int granuleNumber, final String granulePosition, final String creationTime,
			final String anxTime, final String anx1Time, final String dumpStart) {
		super(productName, productType, keyObjectStorage, validityStart, validityStop, missionId, satelliteId,
				stationCode);
		this.granuleNumber = granuleNumber;
		this.granulePosition = granulePosition;
		this.creationTime = creationTime;
		this.anxTime = anxTime;
		this.anx1Time = anx1Time;
		this.dumpStart = dumpStart;
	}

	public S3Metadata() {
	}

	public String getAbsoluteStartOrbit() {
		return absoluteStartOrbit;
	}

	public void setAbsoluteStartOrbit(final String absoluteStartOrbit) {
		this.absoluteStartOrbit = absoluteStartOrbit;
	}
	
	public String getAnxTime() {
		return anxTime;
	}

	public void setAnxTime(final String anxTime) {
		this.anxTime = anxTime;
	}

	public String getAnx1Time() {
		return anx1Time;
	}

	public void setAnx1Time(final String anx1Time) {
		this.anx1Time = anx1Time;
	}
	
	public String getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(final String creationTime) {
		this.creationTime = creationTime;
	}

	public String getDumpStart() {
		return dumpStart;
	}

	public void setDumpStart(final String dumpStart) {
		this.dumpStart = dumpStart;
	}

	public int getGranuleNumber() {
		return granuleNumber;
	}

	public void setGranuleNumber(final int granuleNumber) {
		this.granuleNumber = granuleNumber;
	}

	public String getGranulePosition() {
		return granulePosition;
	}

	public void setGranulePosition(final String granulePosition) {
		this.granulePosition = granulePosition;
	}
	
	public String getInsertionTime() {
		return insertionTime;
	}

	public void setInsertionTime(final String insertionTime) {
		this.insertionTime = insertionTime;
	}

	@Override
	public int hashCode() {
		return Objects.hash(
				absoluteStartOrbit,
				additionalProperties,
				anx1Time,
				anxTime,
				creationTime,
				dumpStart,
				granuleNumber,
				granulePosition,
				insertionTime,
				keyObjectStorage,
				missionId,
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
		final S3Metadata other = (S3Metadata) obj;
		return Objects.equals(absoluteStartOrbit, other.absoluteStartOrbit)
				&& Objects.equals(additionalProperties, other.additionalProperties)
				&& Objects.equals(anx1Time, other.anx1Time) 
				&& Objects.equals(anxTime, other.anxTime)
				&& Objects.equals(creationTime, other.creationTime) 
				&& Objects.equals(dumpStart, other.dumpStart)
				&& granuleNumber == other.granuleNumber 
				&& Objects.equals(granulePosition, other.granulePosition)
				&& Objects.equals(insertionTime, other.insertionTime)
				&& Objects.equals(keyObjectStorage, other.keyObjectStorage)
				&& Objects.equals(missionId, other.missionId) 
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
		return "S3Metadata [absoluteStartOrbit=" + absoluteStartOrbit + ", anxTime=" + anxTime + ", anx1Time="
				+ anx1Time + ", creationTime=" + creationTime + ", granuleNumber=" + granuleNumber
				+ ", granulePosition=" + granulePosition + ", insertionTime=" + insertionTime + ", productName="
				+ productName + ", productType=" + productType + ", keyObjectStorage=" + keyObjectStorage
				+ ", validityStart=" + validityStart + ", validityStop=" + validityStop + ", missionId=" + missionId
				+ ", satelliteId=" + satelliteId + ", stationCode=" + stationCode + "]";
	}
}

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
 * Object containing the metadata from ES
 *
 * @author Viveris Technologies
 */
public class LevelSegmentMetadata extends AbstractMetadata {
	public static final LevelSegmentMetadata NULL = new LevelSegmentMetadata();

    private String polarisation;

    private String consolidation;

    private String productSensingConsolidation;
    
    private String insertionTime;
    
	private String datatakeId;

    public LevelSegmentMetadata(final String productName,
            final String productType, final String keyObjectStorage,
            final String validityStart, final String validityStop,
            final String missionId, final String satelliteId, final String stationCode,
            final String polarisation, final String consolidation, final String productSensingConsolidation,
            final String datatakeId) {
        super(productName, productType, keyObjectStorage, validityStart,
                validityStop, missionId, satelliteId, stationCode);
        this.polarisation = polarisation;
        this.consolidation = consolidation;
        this.productSensingConsolidation = productSensingConsolidation;
        this.datatakeId = datatakeId;
    }
    
    public LevelSegmentMetadata(final String productName,
            final String productType, final String keyObjectStorage,
            final String validityStart, final String validityStop,
            final String missionId, final String satelliteId, final String stationCode) {
        super(productName, productType, keyObjectStorage, validityStart,
                validityStop, missionId, satelliteId, stationCode);
    }
    
    public LevelSegmentMetadata() {
    	
    }

    /**
     * @return the polarisation
     */
    public String getPolarisation() {
        return polarisation;
    }

    /**
     * @param polarisation
     *            the polarisation to set
     */
    public void setPolarisation(final String polarisation) {
        this.polarisation = polarisation;
    }

    /**
     * @return the datatakeId
     */
    public String getDatatakeId() {
        return datatakeId;
    }

    /**
     * @return the consolidation
     */
    public String getConsolidation() {
        return consolidation;
    }

    /**
     * @param consolidation
     *            the consolidation to set
     */
    public void setConsolidation(final String consolidation) {
        this.consolidation = consolidation;
    }

    /**
     * @param datatakeId
     *            the datatakeId to set
     */
    public void setDatatakeId(final String datatakeId) {
        this.datatakeId = datatakeId;
    }
    
    public String getProductSensingConsolidation() {
		return productSensingConsolidation;
	}

	public void setProductSensingConsolidation(final String productSensingConsolidation) {
		this.productSensingConsolidation = productSensingConsolidation;
	}
	
	public String getInsertionTime() {
		return insertionTime;
	}

	public void setInsertionTime(final String insertionTime) {
		this.insertionTime = insertionTime;
	}

	public String toJsonString() {
		final String superToString = super.toAbstractString();
		return String.format("{%s,\"datatakeId\":\"%s\",\"polarisation\":\"%s\",\"consolidation\":\"%s\",\"productSensingConsolidation\":\"%s\"}",
				superToString, datatakeId, polarisation, consolidation, productSensingConsolidation);
	}

	@Override
	public int hashCode() {
		return Objects.hash(
				additionalProperties,
				consolidation,
				datatakeId,
				insertionTime,
				keyObjectStorage,
				missionId,
				polarisation,
				productName,
				productSensingConsolidation,
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
		final LevelSegmentMetadata other = (LevelSegmentMetadata) obj;
		return Objects.equals(additionalProperties, other.additionalProperties)
				&& Objects.equals(consolidation, other.consolidation) && Objects.equals(datatakeId, other.datatakeId)
				&& Objects.equals(insertionTime, other.insertionTime)
				&& Objects.equals(keyObjectStorage, other.keyObjectStorage)
				&& Objects.equals(missionId, other.missionId) && Objects.equals(polarisation, other.polarisation)
				&& Objects.equals(productName, other.productName)
				&& Objects.equals(productSensingConsolidation, other.productSensingConsolidation)
				&& Objects.equals(productType, other.productType) && Objects.equals(satelliteId, other.satelliteId)
				&& Objects.equals(stationCode, other.stationCode) && Objects.equals(swathtype, other.swathtype)
				&& Objects.equals(validityStart, other.validityStart)
				&& Objects.equals(validityStop, other.validityStop);
	}

	@Override
	public String toString() {
		return "LevelSegmentMetadata [polarisation=" + polarisation + ", consolidation=" + consolidation
				+ ", productSensingConsolidation=" + productSensingConsolidation + ", datatakeId=" + datatakeId 
				+ ", productName=" + productName + ", productType=" + productType + ", keyObjectStorage=" 
				+ keyObjectStorage + ", validityStart=" + validityStart + ", validityStop="	+ validityStop
				+ ", missionId=" + missionId + ", satelliteId=" + satelliteId + ", stationCode=" + stationCode + "]";
	}
}

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

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), datatakeId, polarisation,
                consolidation,productSensingConsolidation, insertionTime);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        boolean ret;
        if (this == obj) {
            ret = true;
        } else if (obj == null || getClass() != obj.getClass()) {
            ret = false;
        } else {
            final LevelSegmentMetadata other = (LevelSegmentMetadata) obj;
            ret = super.equals(obj)
                    && Objects.equals(datatakeId, other.datatakeId)
                    && Objects.equals(polarisation, other.polarisation)
                    && Objects.equals(consolidation, other.consolidation)
                    && Objects.equals(insertionTime, other.insertionTime)                    
                    && Objects.equals(productSensingConsolidation, other.productSensingConsolidation);
        }
        return ret;
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

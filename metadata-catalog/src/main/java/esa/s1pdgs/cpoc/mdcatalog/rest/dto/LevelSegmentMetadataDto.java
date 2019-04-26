package esa.s1pdgs.cpoc.mdcatalog.rest.dto;

import java.util.Objects;

/**
 * Class describing the metadata of a level segment
 * 
 * @author Viveris Technologies
 */
public class LevelSegmentMetadataDto extends MetadataDto {

    /**
     * Datatake identifier
     */
    private String datatakeId;

    /**
     * Polarisation
     */
    private String polarisation;

    /**
     * Product consolidation
     */
    private String consolidation;

    /**
     * Constructor using fields
     * 
     * @param productName
     * @param productType
     * @param keyObjectStorage
     * @param validityStart
     * @param validityStop
     */
    public LevelSegmentMetadataDto(final String productName,
            final String productType, final String keyObjectStorage,
            final String validityStart, final String validityStop) {
        super(productName, productType, keyObjectStorage, validityStart,
                validityStop);
    }

    /**
     * Constructor using clone
     * 
     * @param obj
     */
    public LevelSegmentMetadataDto(final LevelSegmentMetadataDto obj) {
        this(obj.getProductName(), obj.getProductType(),
                obj.getKeyObjectStorage(), obj.getValidityStart(),
                obj.getValidityStop());
        this.datatakeId = obj.getDatatakeId();
        this.polarisation = obj.getPolarisation();
        this.consolidation = obj.getConsolidation();
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
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format(
                "{productName: %s, productType: %s, keyObjectStorage: %s, validityStart: %s, validityStop: %s, datatakeId: %s, polarisation: %s, consolidation: %s}",
                productName, productType, keyObjectStorage, validityStart,
                validityStop, datatakeId, polarisation, consolidation);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), datatakeId, polarisation,
                consolidation);
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
            LevelSegmentMetadataDto other = (LevelSegmentMetadataDto) obj;
            ret = super.equals(obj)
                    && Objects.equals(datatakeId, other.datatakeId)
                    && Objects.equals(polarisation, other.polarisation)
                    && Objects.equals(consolidation, other.consolidation);
        }
        return ret;
    }

}

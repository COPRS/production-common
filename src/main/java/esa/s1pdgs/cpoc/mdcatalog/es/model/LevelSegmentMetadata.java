package esa.s1pdgs.cpoc.mdcatalog.es.model;

import java.util.Objects;

/**
 * Object containing the metadata from ES
 *
 * @author Viveris Technologies
 */
public class LevelSegmentMetadata extends AbstractMetadata {

    private String polarisation;

    private String consolidation;

    private String datatakeId;

    public LevelSegmentMetadata() {
        super();
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
    public void setPolarisation(String polarisation) {
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
    public void setConsolidation(String consolidation) {
        this.consolidation = consolidation;
    }

    /**
     * @param datatakeId
     *            the datatakeId to set
     */
    public void setDatatakeId(String datatakeId) {
        this.datatakeId = datatakeId;
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
            LevelSegmentMetadata other = (LevelSegmentMetadata) obj;
            ret = super.equals(obj)
                    && Objects.equals(datatakeId, other.datatakeId)
                    && Objects.equals(polarisation, other.polarisation)
                    && Objects.equals(consolidation, other.consolidation);
        }
        return ret;
    }

}

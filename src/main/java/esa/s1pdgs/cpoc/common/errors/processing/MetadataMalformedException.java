package esa.s1pdgs.cpoc.common.errors.processing;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;

/**
 * @author Viveris Technlogies
 */
public class MetadataMalformedException extends AbstractCodedException {

    /**
     * 
     */
    private static final long serialVersionUID = 2939784030412076416L;

    /**
     * Generic message
     */
    private static final String MESSAGE = "Metadata malformed";

    /**
     * Nalme of missing field
     */
    private final String missingField;

    /**
     * @param productName
     */
    public MetadataMalformedException(final String missingField) {
        super(ErrorCode.METADATA_MALFORMED_ERROR, MESSAGE);
        this.missingField = missingField;
    }

    /**
     * @return the missingField
     */
    public String getMissingField() {
        return missingField;
    }

    /**
     * 
     */
    @Override
    public String getLogMessage() {
        return String.format("[missingField %s] [msg %s]", missingField,
                getMessage());
    }

}

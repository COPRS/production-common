package int_.esa.s1pdgs.cpoc.common.errors.processing;

import int_.esa.s1pdgs.cpoc.common.errors.AbstractCodedException;

/**
 * @author Viveris Technlogies
 */
public class MetadataExtractionException extends AbstractCodedException {

    /**
     * 
     */
    private static final long serialVersionUID = 2134771514034032034L;

    /**
     * @param cause
     */
    public MetadataExtractionException(final Throwable cause) {
        super(ErrorCode.METADATA_EXTRACTION_ERROR, cause.getMessage(), cause);
    }

    /**
     * 
     */
    @Override
    public String getLogMessage() {
        return String.format("[msg %s]", getMessage());
    }

}

package esa.s1pdgs.cpoc.common.errors.processing;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;

/**
 * @author Viveris Technlogies
 */
public class MetadataIllegalFileExtension extends AbstractCodedException {

    /**
     * 
     */
    private static final long serialVersionUID = 2663452897332948566L;

    /**
     * Generic message
     */
    private static final String MESSAGE =
            "Cannot retrieve ERDS session file type from extension %s";

    /**
     * @param productName
     */
    public MetadataIllegalFileExtension(final String extension) {
        super(ErrorCode.METADATA_FILE_EXTENSION,
                String.format(MESSAGE, extension));
    }

    /**
     * 
     */
    @Override
    public String getLogMessage() {
        return String.format("[msg %s]", getMessage());
    }

}

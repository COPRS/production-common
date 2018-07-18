package esa.s1pdgs.cpoc.common.errors.processing;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;

/**
 * 
 */
public class IngestorFilePathException extends AbstractCodedException {

    private static final long serialVersionUID = 2694835373130815240L;

    /**
     * Custom message
     */
    private static final String MESSAGE = "Description extraction failed: %s";

    /**
     * Path of the concerned file
     */
    private final String path;

    /**
     * Wanted type of the file
     */
    private final String family;

    /**
     * @param productName
     * @param path
     * @param msg
     */
    public IngestorFilePathException(final String path, final String family,
            final String msg) {
        super(ErrorCode.INGESTOR_INVALID_PATH, String.format(MESSAGE, msg));
        this.path = path;
        this.family = family;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @return the family
     */
    public String getFamily() {
        return family;
    }

    /**
     * 
     */
    @Override
    public String getLogMessage() {
        return String.format("[path %s] [family %s] [msg %s]", path, family,
                getMessage());
    }

}

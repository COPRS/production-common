package esa.s1pdgs.cpoc.archives.model.exception;

import esa.s1pdgs.cpoc.archives.model.ProductFamily;

/**
 * Exception concerning the object storage
 * 
 * @author Cyrielle Gailliard
 */
public class ObsUnknownObjectException extends AbstractCodedException {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -3680895691846942569L;

    /**
     * Key in object storage
     */
    private final String key;

    /**
     * Bucket in object storage
     */
    private final ProductFamily family;

    /**
     * @param key
     * @param bucket
     * @param message
     */
    public ObsUnknownObjectException(final ProductFamily family,
            final String key) {
        super(ErrorCode.OBS_UNKOWN_OBJ, "Object not found");
        this.key = key;
        this.family = family;
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @return the family
     */
    public ProductFamily getFamily() {
        return family;
    }

    /**
     * 
     */
    @Override
    public String getLogMessage() {
        return String.format("[family %s] [key %s] [msg %s]", family, key,
                getMessage());
    }

}

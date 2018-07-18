package esa.s1pdgs.cpoc.common.errors.obs;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;

/**
 * Exception concerning the object storage
 * 
 * @author Viveris Technologies
 */
public class ObsException extends AbstractCodedException {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -3680895691846942569L;

    /**
     * Key in object storage
     */
    private final String key;

    /**
     * Family
     */
    private final ProductFamily family;

    /**
     * @param key
     * @param bucket
     * @param e
     */
    public ObsException(final ProductFamily family, final String key,
            final Throwable cause) {
        this(ErrorCode.OBS_ERROR, family, key, cause.getMessage(), cause);
    }

    /**
     * @param key
     * @param bucket
     * @param e
     */
    protected ObsException(final ErrorCode error, final ProductFamily family,
            final String key, final String message, final Throwable cause) {
        super(error, message, cause);
        this.key = key;
        this.family = family;
    }

    /**
     * @param key
     * @param bucket
     * @param e
     */
    protected ObsException(final ErrorCode error, final ProductFamily family,
            final String key, final String message) {
        super(error, message);
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

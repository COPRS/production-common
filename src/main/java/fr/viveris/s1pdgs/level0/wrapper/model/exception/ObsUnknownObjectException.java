package fr.viveris.s1pdgs.level0.wrapper.model.exception;

import fr.viveris.s1pdgs.level0.wrapper.model.ProductFamily;

/**
 * Exception concerning the object storage
 * 
 * @author Viveris Technologies
 */
public class ObsUnknownObjectException extends AbstractCodedException {

    /**
     * 
     */
    private static final long serialVersionUID = -6993780109942662268L;

    /**
     * Key in object storage
     */
    private final String key;

    /**
     * Bucket in object storage
     */
    private final ProductFamily family;

    /**
     * Constructor
     * 
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
     * @see AbstractCodedException#getLogMessage()
     */
    @Override
    public String getLogMessage() {
        return String.format("[family %s] [key %s] [msg %s]", family, key,
                getMessage());
    }

}

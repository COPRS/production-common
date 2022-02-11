package esa.s1pdgs.cpoc.common.errors.obs;

import esa.s1pdgs.cpoc.common.ProductFamily;

/**
 * Exception raised when object already exist in object storage
 */
public class ObsAlreadyExist extends ObsException {

    /**
     * 
     */
    private static final long serialVersionUID = -5331517744542218021L;

    /**
     * Custom message
     */
    private static final String MESSAGE =
            "Object already exists in object storage";

    /**
     * @param productName
     * @param cause
     */
    public ObsAlreadyExist(final ProductFamily family, final String key,
            final Throwable cause) {
        super(ErrorCode.OBS_ALREADY_EXIST, family, key,
                MESSAGE + ": " + cause.getMessage(), cause);
    }
}

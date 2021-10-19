package esa.s1pdgs.cpoc.common.errors;

/**
 * Exception concerning an EDRS session file management
 * 
 * @author Viveris Technologies
 */
public class InvalidFormatProduct extends AbstractCodedException {

    private static final long serialVersionUID = 3720211649870339168L;

    /**
     * @param message
     */
    public InvalidFormatProduct(final String message) {
        super(ErrorCode.INVALID_PRODUCT_FORMAT, message);
    }

    /**
     * 
     */
    @Override
    public String getLogMessage() {
        return String.format("[msg %s]", getMessage());
    }
}

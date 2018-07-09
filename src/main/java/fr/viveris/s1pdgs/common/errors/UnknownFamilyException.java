package fr.viveris.s1pdgs.common.errors;

/**
 * @author Viveris Technologies
 */
public class UnknownFamilyException extends AbstractCodedException {

    /**
     * 
     */
    private static final long serialVersionUID = 9140236445794096614L;

    /**
     * Invalid family
     */
    private final String family;

    /**
     * @param message
     * @param family
     */
    public UnknownFamilyException(final String family, final String message) {
        super(ErrorCode.UNKNOWN_FAMILY, message);
        this.family = family;
    }

    /**
     * @return the family
     */
    public String getFamily() {
        return family;
    }

    /**
     * @see AbstractCodedException#getLogMessage()
     */
    @Override
    public String getLogMessage() {
        return String.format("[family %s] [msg %s]", family, getMessage());
    }

}

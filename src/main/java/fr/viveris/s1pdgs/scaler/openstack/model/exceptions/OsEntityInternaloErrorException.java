package fr.viveris.s1pdgs.scaler.openstack.model.exceptions;

/**
 * @author Viveris Technologies
 */
public class OsEntityInternaloErrorException extends OsEntityException {

    private static final long serialVersionUID = 1694474051225086865L;

    /**
     * @param type
     * @param id
     * @param message
     * @param cause
     */
    public OsEntityInternaloErrorException(final String type,
            final String identifier, final String message,
            final Throwable cause) {
        super(type, identifier, ErrorCode.INTERNAL_ERROR, message, cause);
    }

}

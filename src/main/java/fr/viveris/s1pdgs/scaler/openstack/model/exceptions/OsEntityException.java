package fr.viveris.s1pdgs.scaler.openstack.model.exceptions;

import fr.viveris.s1pdgs.scaler.AbstractCodedException;

/**
 * @author Viveris Technologies
 */
public class OsEntityException extends AbstractCodedException {

    private static final long serialVersionUID = 1302470342554468202L;

    /**
     * Entity identifier
     */
    private final String identifier;

    /**
     * Type of the entity
     */
    private final String type;

    /**
     * @param type
     * @param id
     * @param code
     * @param message
     */
    public OsEntityException(final String type, final String identifier,
            final ErrorCode code, final String message) {
        super(code, message);
        this.type = type;
        this.identifier = identifier;
    }

    /**
     * @param type
     * @param id
     * @param code
     * @param message
     * @param cause
     */
    public OsEntityException(final String type, final String identifier,
            final ErrorCode code, final String message, final Throwable cause) {
        super(code, message, cause);
        this.type = type;
        this.identifier = identifier;
    }

    /**
     * 
     */
    @Override
    public String getLogMessage() {
        return String.format("[%s %s] [msg %s]", type, identifier,
                getMessage());
    }

    /**
     * @return the id
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

}

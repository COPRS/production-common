package esa.s1pdgs.cpoc.scaler.openstack.model.exceptions;

/**
 * @author Viveris Technologies
 */
public class OsServerNotActiveException extends OsEntityException {

    private static final long serialVersionUID = 1694474051225086865L;

    /**
     * @param serverId
     * @param message
     */
    public OsServerNotActiveException(final String serverId,
            final String message) {
        super("serverId", serverId, ErrorCode.OS_SERVER_NOT_ACTIVE, message);
    }

}

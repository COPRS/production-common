package esa.s1pdgs.cpoc.scaler.openstack.model.exceptions;

/**
 * @author Viveris Technologies
 */
public class OsServerNotDeletedException extends OsEntityException {

    private static final long serialVersionUID = 1694474051225086865L;

    /**
     * @param serverId
     * @param message
     */
    public OsServerNotDeletedException(final String serverId,
            final String message) {
        super("serverId", serverId, ErrorCode.OS_SERVER_NOT_DELETED, message);
    }

}

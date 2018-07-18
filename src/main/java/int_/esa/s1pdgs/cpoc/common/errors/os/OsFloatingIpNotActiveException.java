package int_.esa.s1pdgs.cpoc.common.errors.os;

/**
 * @author Viveris Technologies
 */
public class OsFloatingIpNotActiveException extends OsEntityException {

    private static final long serialVersionUID = 1694474051225086865L;

    /**
     * @param serverId
     * @param message
     */
    public OsFloatingIpNotActiveException(final String serverId,
            final String message) {
        super("serverId", serverId, ErrorCode.OS_FLOATING_IP_NOT_ACTIVE,
                message);
    }

}

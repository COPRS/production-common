package fr.viveris.s1pdgs.common.errors.os;

/**
 * @author Viveris Technologies
 */
public class OsVolumeNotAvailableException extends OsEntityException {

    private static final long serialVersionUID = 1694474051225086865L;

    /**
     * @param serverId
     * @param message
     */
    public OsVolumeNotAvailableException(final String serverId,
            final String message) {
        super("volumeName", serverId, ErrorCode.OS_VOLUME_NOT_CREATED, message);
    }

}

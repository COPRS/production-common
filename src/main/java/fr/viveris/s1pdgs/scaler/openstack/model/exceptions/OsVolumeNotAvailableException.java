package fr.viveris.s1pdgs.scaler.openstack.model.exceptions;

public class OsVolumeNotAvailableException extends OsEntityException {

	private static final long serialVersionUID = 1694474051225086865L;

	public OsVolumeNotAvailableException(String serverId, String message) {
		super("volumeName", serverId, ErrorCode.OS_VOLUME_NOT_CREATED, message);
	}

}

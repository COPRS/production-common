package fr.viveris.s1pdgs.scaler.openstack.model.exceptions;

public class OsFloatingIpNotActiveException extends OsEntityException {

	private static final long serialVersionUID = 1694474051225086865L;

	public OsFloatingIpNotActiveException(String serverId, String message) {
		super("serverId", serverId, ErrorCode.OS_FLOATING_IP_NOT_ACTIVE, message);
	}

}

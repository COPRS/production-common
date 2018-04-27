package fr.viveris.s1pdgs.scaler.openstack.model.exceptions;

public class OsServerNotActiveException extends OsEntityException {

	private static final long serialVersionUID = 1694474051225086865L;

	public OsServerNotActiveException(String serverId, String message) {
		super("serverId", serverId, ErrorCode.SERVER_NOT_ACTIVE, message);
	}

}

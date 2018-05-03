package fr.viveris.s1pdgs.scaler.openstack.model.exceptions;

public class OsServerNotDeletedException extends OsEntityException {

	private static final long serialVersionUID = 1694474051225086865L;

	public OsServerNotDeletedException(String serverId, String message) {
		super("serverId", serverId, ErrorCode.SERVER_NOT_ACTIVE, message);
	}

}

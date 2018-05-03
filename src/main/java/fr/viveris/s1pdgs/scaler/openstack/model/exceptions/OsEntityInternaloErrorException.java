package fr.viveris.s1pdgs.scaler.openstack.model.exceptions;

public class OsEntityInternaloErrorException extends OsEntityException {

	private static final long serialVersionUID = 1694474051225086865L;

	public OsEntityInternaloErrorException(String type, String id, String message) {
		super(type, id, ErrorCode.INTERNAL_ERROR, message);
	}

	public OsEntityInternaloErrorException(String type, String id, String message, Throwable cause) {
		super(type, id, ErrorCode.INTERNAL_ERROR, message,cause);
	}

}

package fr.viveris.s1pdgs.scaler.openstack.model.exceptions;

import fr.viveris.s1pdgs.scaler.AbstractCodedException;

public class OsEntityException extends AbstractCodedException {

	private static final long serialVersionUID = 1302470342554468202L;
	
	private String id;
	private String type;

	public OsEntityException(String type, String id, ErrorCode code, String message) {
		super(code, message);
		this.type = type;
		this.id = id;
	}

	public OsEntityException(String type, String id, ErrorCode code, String message, Throwable cause) {
		super(code, message, cause);
		this.type = type;
		this.id = id;
	}

	@Override
	public String getLogMessage() {
		return String.format("[%s %s] [msg %s]", type, id, getMessage());
	}

}

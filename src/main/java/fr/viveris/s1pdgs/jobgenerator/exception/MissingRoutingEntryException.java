package fr.viveris.s1pdgs.jobgenerator.exception;

public class MissingRoutingEntryException extends AbstractCodedException {

	private static final long serialVersionUID = -191284916049233409L;

	public MissingRoutingEntryException(String message) {
		super(ErrorCode.MISSING_ROUTING_ENTRY, message);
	}

	@Override
	public String getLogMessage() {
		return String.format("[msg %s]", getMessage());
	}

}

package esa.s1pdgs.cpoc.preparation.worker.model.exception;

public final class JobStateTransistionFailed extends RuntimeException {
	private static final long serialVersionUID = 8290563130778242676L;

	public JobStateTransistionFailed(final String message) {
		super(message);
	}

	public JobStateTransistionFailed(final String message, final Throwable cause) {
		super(message, cause);
	}
}

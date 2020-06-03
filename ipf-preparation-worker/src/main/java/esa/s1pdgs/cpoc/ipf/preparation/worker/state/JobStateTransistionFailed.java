package esa.s1pdgs.cpoc.ipf.preparation.worker.state;

public final class JobStateTransistionFailed extends RuntimeException {
	private static final long serialVersionUID = 8290563130778242676L;

	public JobStateTransistionFailed(final String message) {
		super(message);
	}
}

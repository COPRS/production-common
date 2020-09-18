package esa.s1pdgs.cpoc.odip.kafka.producer;

import esa.s1pdgs.cpoc.appstatus.AppStatus;

public interface SubmissionClient {
	
	public static final SubmissionClient NULL = new SubmissionClient() {
		@Override
		public void resubmit( final Object message, final AppStatus appStatus) {}
	};	
	void resubmit(final Object message, final AppStatus appStatus);
}

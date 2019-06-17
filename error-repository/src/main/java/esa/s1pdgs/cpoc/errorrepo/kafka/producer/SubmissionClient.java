package esa.s1pdgs.cpoc.errorrepo.kafka.producer;

import esa.s1pdgs.cpoc.appcatalog.common.MqiMessage;

public interface SubmissionClient {

	void resubmit(final MqiMessage message);
}

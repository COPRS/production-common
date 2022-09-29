package esa.s1pdgs.cpoc.errorrepo;

import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessing;

public interface ErrorRepoAppender {

	ErrorRepoAppender NULL = errorRequest -> {};

	void send(FailedProcessing errorRequest);
}

package esa.s1pdgs.cpoc.errorrepo;

import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;

public interface ErrorRepoAppender {

	ErrorRepoAppender NULL = errorRequest -> {};

	void send(FailedProcessingDto errorRequest);
}

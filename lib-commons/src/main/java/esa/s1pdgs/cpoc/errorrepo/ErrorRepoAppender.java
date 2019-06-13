package esa.s1pdgs.cpoc.errorrepo;

import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;

public interface ErrorRepoAppender {

	public void send(FailedProcessingDto<?> errorRequest);	
}

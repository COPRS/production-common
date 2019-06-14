package esa.s1pdgs.cpoc.errorrepo;

import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;

public interface ErrorRepoAppender {	
	public static final ErrorRepoAppender NULL = new ErrorRepoAppender(){
		@Override
		public final void send(FailedProcessingDto<?> errorRequest) {		
		}
	};

	public void send(FailedProcessingDto<?> errorRequest);	
}

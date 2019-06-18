package esa.s1pdgs.cpoc.errorrepo.seq;

public class SequenceException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	public SequenceException(String errMsg) {
		super(errMsg);
	}
}

package esa.s1pdgs.cpoc.common.mongodb.sequence;

public class SequenceException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	public SequenceException(String errMsg) {
		super(errMsg);
	}
}

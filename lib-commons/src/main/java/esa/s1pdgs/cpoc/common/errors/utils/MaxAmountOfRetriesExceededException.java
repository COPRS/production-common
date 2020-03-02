package esa.s1pdgs.cpoc.common.errors.utils;

public class MaxAmountOfRetriesExceededException extends Exception {

	private static final long serialVersionUID = -6186850184508142232L;
	
	public MaxAmountOfRetriesExceededException(String message, Throwable cause) {
        super(message, cause);
    }

}

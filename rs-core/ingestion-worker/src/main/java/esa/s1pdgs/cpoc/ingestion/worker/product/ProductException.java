package esa.s1pdgs.cpoc.ingestion.worker.product;

@SuppressWarnings("serial")
public class ProductException extends RuntimeException {	
	public ProductException(String message) {
		super(message);
	}

	public ProductException(String message, Throwable cause) {
		super(message, cause);
	}
}

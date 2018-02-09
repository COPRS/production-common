package fr.viveris.s1pdgs.ingestor.model.exception;

public abstract class FileException extends Exception {

	private static final long serialVersionUID = -3911928196431571871L;
	
	private String productName;
	
	public FileException(String msg, String productName) {
		super(msg);
		this.productName = productName;
	}
	
	public FileException(String msg, String productName, Throwable cause) {
		super(msg, cause);
		this.productName = productName;
	}

	/**
	 * @return the productName
	 */
	public String getProductName() {
		return productName;
	}

	/**
	 * @param productName the productName to set
	 */
	public void setProductName(String productName) {
		this.productName = productName;
	}

}

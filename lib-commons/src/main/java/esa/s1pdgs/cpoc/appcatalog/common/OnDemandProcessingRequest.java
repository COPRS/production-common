package esa.s1pdgs.cpoc.appcatalog.common;

public class OnDemandProcessingRequest {

	private String productName;
	private boolean debug = false;
	private String mode;
	private String productionType;

	public OnDemandProcessingRequest() {
	}

	public OnDemandProcessingRequest(String productName, boolean debug, String mode, String productionType) {
		super();
		this.productName = productName;
		this.debug = debug;
		this.mode = mode;
		this.productionType = productionType;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getProductionType() {
		return productionType;
	}

	public void setProductionType(String productionType) {
		this.productionType = productionType;
	}

	@Override
	public String toString() {
		return "OnDemandProcessingRequest [productName=" + productName + ", debug=" + debug + ", mode=" + mode
				+ ", productionType=" + productionType + "]";
	}

}

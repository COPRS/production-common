package esa.s1pdgs.cpoc.appcatalog.common;

public class OnDemandProcessingRequest {

	private String productName;
	private boolean debug = false;
	private String mode;
	private String productionType;
	private String tasktableName = null;
	private String outputProductType = null;

	public OnDemandProcessingRequest() {
	}

	public OnDemandProcessingRequest(final String productName, final boolean debug, final String mode, final String productionType) {
		super();
		this.productName = productName;
		this.debug = debug;
		this.mode = mode;
		this.productionType = productionType;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(final String productName) {
		this.productName = productName;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(final boolean debug) {
		this.debug = debug;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(final String mode) {
		this.mode = mode;
	}

	public String getProductionType() {
		return productionType;
	}

	public void setProductionType(final String productionType) {
		this.productionType = productionType;
	}
		
	public String getTasktableName() {
		return tasktableName;
	}

	public void setTasktableName(final String tasktableName) {
		this.tasktableName = tasktableName;
	}

	public String getOutputProductType() {
		return outputProductType;
	}

	public void setOutputProductType(final String outputProductType) {
		this.outputProductType = outputProductType;
	}

	@Override
	public String toString() {
		return "OnDemandProcessingRequest [productName=" + productName + ", debug=" + debug + ", mode=" + mode
				+ ", productionType=" + productionType 
				+ ", tasktableName=" + tasktableName 
				+ ", outputProductType=" + outputProductType + "]";
	}

}

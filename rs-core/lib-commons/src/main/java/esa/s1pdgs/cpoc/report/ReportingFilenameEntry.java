package esa.s1pdgs.cpoc.report;

import esa.s1pdgs.cpoc.common.ProductFamily;

public class ReportingFilenameEntry {
	private final ProductFamily family;
	private final String productName;
	
	public ReportingFilenameEntry(final ProductFamily family, final String productName) {
		this.family = family;
		this.productName = productName;
	}

	public ProductFamily getFamily() {
		return family;
	}

	public String getProductName() {
		return productName;
	}
}

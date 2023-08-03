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
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((family == null) ? 0 : family.hashCode());
		result = prime * result + ((productName == null) ? 0 : productName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReportingFilenameEntry other = (ReportingFilenameEntry) obj;
		if (family != other.family)
			return false;
		if (productName == null) {
			if (other.productName != null)
				return false;
		} else if (!productName.equals(other.productName))
			return false;
		return true;
	}
}

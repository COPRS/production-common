package fr.viveris.s1pdgs.archives.controller.dto;

import fr.viveris.s1pdgs.archives.model.ProductFamily;

/**
 * DTO class for L0 reports
 * @author Cyrielle Gailliard
 *
 */
public class ReportDto {
	/**
	 * Product name of the reports
	 */
	private String productName;
	
	/**
	 * Content of the report
	 */
	private String content;
	/**
	 * Family name of the reports (l0 or l1)
	 */
	private ProductFamily family;
	

	/**
	 * Default constructor
	 */
	public ReportDto() {
		
	}

	/**
	 * @param productName
	 * @param content
	 */
	public ReportDto(String productName, String content, ProductFamily family) {
		this();
		this.productName = productName;
		this.content = content;
		this.family = family;
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

	/**
	 * @return the content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * @param content the content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * @return the familyName
	 */
	public ProductFamily getFamily() {
		return family;
	}

	/**
	 * @param familyName the familyName to set
	 */
	public void setFamily(ProductFamily family) {
		this.family = family;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ReportDto [productName=" + productName + ", content=" + content + ", familyName=" + family + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result + ((family == null) ? 0 : family.hashCode());
		result = prime * result + ((productName == null) ? 0 : productName.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReportDto other = (ReportDto) obj;
		if (content == null) {
			if (other.content != null)
				return false;
		} else if (!content.equals(other.content))
			return false;
		if (family == null) {
			if (other.family != null)
				return false;
		} else if (!family.equals(other.family))
			return false;
		if (productName == null) {
			if (other.productName != null)
				return false;
		} else if (!productName.equals(other.productName))
			return false;
		return true;
	}

}

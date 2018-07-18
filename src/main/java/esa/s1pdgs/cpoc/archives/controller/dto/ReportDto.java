package esa.s1pdgs.cpoc.archives.controller.dto;

import java.util.Objects;

import esa.s1pdgs.cpoc.archives.model.ProductFamily;

/**
 * DTO class for L0 reports
 * 
 * @author Viveris Technologies
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
	public ReportDto() {}

    /**
     * @param productName
     * @param content
     */
    public ReportDto(final String productName, final String content,
            final ProductFamily family) {
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
     * @param productName
     *            the productName to set
     */
    public void setProductName(final String productName) {
        this.productName = productName;
    }

    /**
     * @return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * @param content
     *            the content to set
     */
    public void setContent(final String content) {
        this.content = content;
    }

    /**
     * @return the familyName
     */
    public ProductFamily getFamily() {
        return family;
    }

    /**
     * @param familyName
     *            the familyName to set
     */
    public void setFamily(final ProductFamily family) {
        this.family = family;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("{productName: %s, content: %s, family: %s}",
                productName, content, family);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(productName, content, family);
    }

    /**
     * @see java.lang.Object#equals()
     */
    @Override
    public boolean equals(final Object obj) {
        boolean ret;
        if (this == obj) {
            ret = true;
        } else if (obj == null || getClass() != obj.getClass()) {
            ret = false;
        } else {
            final ReportDto other = (ReportDto) obj;
            // field comparison
            ret = Objects.equals(productName, other.productName)
                    && Objects.equals(content, other.content)
                    && Objects.equals(family, other.family);
        }
        return ret;
    }

}

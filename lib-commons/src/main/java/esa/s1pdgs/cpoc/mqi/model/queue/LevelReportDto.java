package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Objects;

import esa.s1pdgs.cpoc.common.ProductFamily;

/**
 * Exchanged object for the product category LevelReports.
 * 
 * @author Viveris Technologies
 */
public class LevelReportDto extends AbstractDto {

    /**
     * ObjectkeyStorage of the reports
     */
    private String content;

    /**
     * Default constructor
     */
    public LevelReportDto() {
        super();
    }

    /**
     * @param productName
     * @param content
     */
    public LevelReportDto(final String productName, final String content,
            final ProductFamily family) {
        super(productName, family);
        this.content = content;
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
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("{productName: %s, content: %s, family: %s, hostname: %s, creationDate: %s}",
                getProductName(), content, getFamily(), getHostname(), getCreationDate());
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(getProductName(), content, getFamily(), getHostname(), getCreationDate());
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
            LevelReportDto other = (LevelReportDto) obj;
            // field comparison
            ret = Objects.equals(getProductName(), other.getProductName())
                    && Objects.equals(content, other.content)
                    && Objects.equals(getFamily(), other.getFamily())
                    && Objects.equals(getHostname(), other.getHostname())
            		&& Objects.equals(getCreationDate(), other.getCreationDate());
        }
        return ret;
    }

}

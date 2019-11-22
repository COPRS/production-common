package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Objects;

import esa.s1pdgs.cpoc.common.ProductFamily;

/**
 * Exchanged object for the product category LevelReports.
 * 
 * @author Viveris Technologies
 */
@Deprecated
public class LevelReportDto extends AbstractMessage {

	private ProductFamily productFamily;
	private String keyObjectStorage;
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
        this.keyObjectStorage = productName;
        this.productFamily = family;
        this.content = content;
    }


    /**
     * @return the content
     */
    public String getContent() {
        return content;
    }
    
    

    public ProductFamily getProductFamily() {
		return productFamily;
	}

	public void setProductFamily(ProductFamily productFamily) {
		this.productFamily = productFamily;
	}

	/**
     * @param content
     *            the content to set
     */
    public void setContent(final String content) {
        this.content = content;
    }

    public String getKeyObjectStorage() {
		return keyObjectStorage;
	}

	public void setKeyObjectStorage(String keyObjectStorage) {
		this.keyObjectStorage = keyObjectStorage;
	}

	/**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("{productName: %s, content: %s, family: %s, hostname: %s, creationDate: %s}",
                keyObjectStorage, content, getProductFamily(), getHostname(), getCreationDate());
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(keyObjectStorage, content, getProductFamily(), getHostname(), getCreationDate());
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
            ret = Objects.equals(keyObjectStorage, other.getKeyObjectStorage())
                    && Objects.equals(content, other.content)
                    && Objects.equals(getProductFamily(), other.getProductFamily())
                    && Objects.equals(getHostname(), other.getHostname())
            		&& Objects.equals(getCreationDate(), other.getCreationDate());
        }
        return ret;
    }

}

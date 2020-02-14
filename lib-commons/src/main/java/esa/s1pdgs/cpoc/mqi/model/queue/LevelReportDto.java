package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Objects;

import esa.s1pdgs.cpoc.common.ProductFamily;

/**
 * Exchanged object for the product category LevelReports.
 * 
 * @author Viveris Technologies
 */
public class LevelReportDto extends AbstractMessage {
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
    	super(family, productName);
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

	@Override
	public int hashCode() {
		return Objects.hash(content, creationDate, hostname, keyObjectStorage, productFamily, uid);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final LevelReportDto other = (LevelReportDto) obj;
		return Objects.equals(content, other.content) 
				&& Objects.equals(creationDate, other.creationDate)
				&& Objects.equals(hostname, other.hostname) 
				&& Objects.equals(keyObjectStorage, other.keyObjectStorage)
				&& Objects.equals(uid, other.uid)
				&& productFamily == other.productFamily;
	}

	@Override
	public String toString() {
		return "LevelReportDto [productFamily=" + productFamily + ", keyObjectStorage=" + keyObjectStorage
				+ ", creationDate=" + creationDate + ", hostname=" + hostname + ", content=" + content + ", uid=" + uid +"]";
	}
}

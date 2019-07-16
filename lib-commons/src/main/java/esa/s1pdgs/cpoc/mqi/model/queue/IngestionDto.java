package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Objects;

import esa.s1pdgs.cpoc.common.ProductFamily;

public class IngestionDto extends AbstractDto {

	private String productUrl;

	public IngestionDto() {
		super();
	}

	public IngestionDto(String productName, String productUrl) {
		super(productName, ProductFamily.BLANK);
		this.productUrl = productUrl;
	}

	public String getProductUrl() {
		return productUrl;
	}

	public void setProductUrl(String productUrl) {
		this.productUrl = productUrl;
	}

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(getProductName(), productUrl, getFamily());
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
        	final IngestionDto other = (IngestionDto) obj;
            // field comparison
            ret = Objects.equals(getProductName(), other.getProductName())
                    && Objects.equals(productUrl, other.productUrl)
                    && Objects.equals(getFamily(), other.getFamily());
        }
        return ret;
    }

	@Override
	public String toString() {
		return "IngestionDto [productUrl=" + productUrl + "]";
	}
}

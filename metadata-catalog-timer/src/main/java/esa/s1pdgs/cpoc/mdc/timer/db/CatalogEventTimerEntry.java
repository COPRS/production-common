package esa.s1pdgs.cpoc.mdc.timer.db;

import java.util.Date;
import java.util.Objects;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import esa.s1pdgs.cpoc.common.ProductFamily;

@Document(collection = "catalogEventTimerEntry")
@CompoundIndexes({
	@CompoundIndex(name = "productType_productFamily", def = "{'productType' : 1, 'productFamily' : 1}")
})
public class CatalogEventTimerEntry {
	
	private String productType;
	
	private ProductFamily productFamily;
	
	private Date lastCheckDate;

	public String getProductType() {
		return productType;
	}

	public void setProductType(String productType) {
		this.productType = productType;
	}

	public ProductFamily getProductFamily() {
		return productFamily;
	}

	public void setProductFamily(ProductFamily productFamily) {
		this.productFamily = productFamily;
	}

	public Date getLastCheckDate() {
		return lastCheckDate;
	}

	public void setLastCheckDate(Date lastCheckDate) {
		this.lastCheckDate = lastCheckDate;
	}

	@Override
	public int hashCode() {
		return Objects.hash(lastCheckDate, productFamily, productType);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof CatalogEventTimerEntry)) {
			return false;
		}
		CatalogEventTimerEntry other = (CatalogEventTimerEntry) obj;
		return Objects.equals(lastCheckDate, other.lastCheckDate) && productFamily == other.productFamily
				&& Objects.equals(productType, other.productType);
	}

	@Override
	public String toString() {
		return "CatalogEventTimerEntry [productType=" + productType + ", productFamily=" + productFamily
				+ ", lastCheckDate=" + lastCheckDate + "]";
	}
}

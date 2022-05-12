package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Arrays;

import esa.s1pdgs.cpoc.mqi.model.control.AllowedAction;

public class CatalogEvent extends AbstractMessage {
	private static final String PRODUCT_NAME_KEY = "productName";
	private static final String PRODUCT_TYPE_KEY = "productType";

	public CatalogEvent() {
		super();
		setAllowedActions(Arrays.asList(AllowedAction.RESUBMIT));
	}

	public String getProductName() {
		return metadata.getOrDefault(PRODUCT_NAME_KEY, "").toString();
	}

	public void setProductName(final String productName) {
		this.metadata.put(PRODUCT_NAME_KEY, productName);
	}

	public String getProductType() {
		return metadata.getOrDefault(PRODUCT_TYPE_KEY, "").toString();
	}

	public void setProductType(final String productType) {
		this.metadata.put(PRODUCT_TYPE_KEY, productType);
	}

	@Override
	public String toString() {
		return "CatalogEvent [productName=" + metadata.get(PRODUCT_NAME_KEY) + ", productType="
				+ metadata.get(PRODUCT_TYPE_KEY) + ", metadata=" + metadata + ", productFamily=" + productFamily
				+ ", keyObjectStorage=" + keyObjectStorage + ", storagePath=" + storagePath + ", creationDate="
				+ creationDate + ", podName=" + podName + ", uid=" + uid + "]";
	}
}
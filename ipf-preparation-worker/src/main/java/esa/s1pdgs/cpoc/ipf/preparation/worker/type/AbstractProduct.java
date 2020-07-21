package esa.s1pdgs.cpoc.ipf.preparation.worker.type;

import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.util.AppDataJobProductAdapter;

public abstract class AbstractProduct implements Product {
	protected final AppDataJobProductAdapter product;

	public AbstractProduct(final AppDataJobProductAdapter product) {
		this.product = product;
	}	
	
	public final void setProductName(final String productName) {
		product.setProductName(productName);		
	}
	
	public final String getProductName() {
		return product.getProductName();
	}
	
	public final String getMissionId() {
		return product.getMissionId();
	}

	public final String getSatelliteId() {
		return product.getSatelliteId();
	}

	public final String getStartTime() {
		return product.getStartTime();
	}

	public String getStopTime() {
		return product.getStopTime();
	}	
	
	public final String getProcessMode() {
		return product.getProcessMode();
	}
	
	@Override
	public final AppDataJobProduct toProduct() {
		return product.toProduct();
	}	
}

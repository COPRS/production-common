package esa.s1pdgs.cpoc.appcatalog.util;

import java.util.Collections;
import java.util.List;

import esa.s1pdgs.cpoc.appcatalog.AppDataJobFile;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;

public final class AppDataJobProductAdapter {
	private final AppDataJobProduct product;

	public AppDataJobProductAdapter(final AppDataJobProduct product) {
		this.product = product;
	}
	
	public final void setProductsFor(final String key, final List<AppDataJobFile> products) {
		product.getInputs().put(key, products);
	}
	
	public final List<AppDataJobFile> getProductsFor(final String key) {
		return product.getInputs().getOrDefault(key, Collections.emptyList());
	}
	
	public final String getStartTime() {
		return getStringValue("startTime");		
	}
	
	public final String getStopTime() {
		return getStringValue("stopTime");		
	}
	
	public final void setProductName(final String name) {
		setStringValue("productName", name);
	}
	
	public final String getProductName() {
		return getStringValue("productName");
	}
	
	public final String getSatelliteId() {
		return getStringValue("satelliteId");
	}
		
	public final String getMissionId() {
		return getStringValue("missionId");
	}
	
	public final void setInsConfId(final int id) {
		setIntegerValue("insConfId", id);
	}
	
	public final int getInsConfId() {
		return Integer.parseInt(getStringValue("insConfId", "-1"));
	}
	
	public final String getProductType() {
		return getStringValue("productType");
	}

	public final String getProcessMode() {
		return getStringValue("processMode", "NOMINAL");
	}

	public final void setStringValue(final String key, final String value) {
		product.getMetadata().put(key, value);
	}
	
	public final void setIntegerValue(final String key, final Integer value) {
		product.getMetadata().put(key, value);
	}
	
    
    public final Integer getIntegerValue(final String key) {
    	final Integer value = (int) product.getMetadata().get(key);
    	return value;
    }
	
    public final String getStringValue(final String key)
    {
		final String value = getStringValue(key, null);
    	if (value == null) {
    		throw new IllegalArgumentException(
    				String.format("Missing metadata element '%s'", key)
    		);
    	}
    	return value;
    }

    public final String getStringValue(final String key, final String defaultValue)
    {
    	final String text = (String) product.getMetadata().get(key);    	
    	if (text == null) {
    		return defaultValue;
    	}
    	return text;
    }
    
    public final AppDataJobProduct toProduct() {
    	return product;
    }
}

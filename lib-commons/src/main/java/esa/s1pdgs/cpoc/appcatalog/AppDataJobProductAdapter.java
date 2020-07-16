package esa.s1pdgs.cpoc.appcatalog;

public final class AppDataJobProductAdapter {
	private final AppDataJobProduct product;

	public AppDataJobProductAdapter(final AppDataJobProduct product) {
		this.product = product;
	}
	
	public final String getStartTime() {
		return getStringValue("startTime");		
	}
	
	public final String getStopTime() {
		return getStringValue("stopTime");		
	}
	
	public final String getProductName() {
		return getStringValue("productName");
	}
	
	public final String getMissionId() {
		return getStringValue("missionId");
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
	
}

package esa.s1pdgs.cpoc.common;

public enum MaskType {
	LAND("MSK_LAND_"),
	OCEAN("MSK_OCEAN_"),
	OVERPASS("MSK_OVRPAS");
	
	private String productType;
	
	private MaskType(String productType) {
		this.productType = productType;
	}
	
	public String getProductType() {
		return productType;
	}
	
	@Override
	public String toString() {
		return name().toLowerCase() + " mask"; // friendly name ("land mask", "ocean mask", "overpass mask")
	}
	
	public static MaskType of(String productType) {	
		for (MaskType maskType : MaskType.values()) {
			if (maskType.getProductType().equals(productType)) {
				return maskType;
			}
		}
		throw new IllegalArgumentException(String.format("Cannot determine mask type for product type %s", productType));
	}

}

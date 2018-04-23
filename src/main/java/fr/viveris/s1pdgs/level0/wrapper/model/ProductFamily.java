package fr.viveris.s1pdgs.level0.wrapper.model;

public enum ProductFamily {
	RAW, JOB, CONFIG, L0_ACN, L0_PRODUCT, L0_REPORT, L1_ACN, L1_PRODUCT, L1_REPORT, BLANK;
	
	public static ProductFamily fromValue(String value) {
		for (ProductFamily c : ProductFamily.values()) {
			if (c.name().equals(value)) {
				return c;
			}
		}
		return ProductFamily.BLANK;
	}
}

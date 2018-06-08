package fr.viveris.s1pdgs.jobgenerator.model;

import org.springframework.util.StringUtils;

/**
 * Product family. Determinate the concerned topics, buckets in OBS, ...
 * 
 * @author Cyrielle Gailliard
 *
 */
public enum ProductFamily {
	RAW, JOB, CONFIG, L0_ACN, L0_PRODUCT, L0_REPORT, L1_ACN, L1_PRODUCT, L1_REPORT, BLANK;

	/**
	 * Get product family from value in string format
	 * 
	 * @param value
	 * @return
	 */
	public static ProductFamily fromValue(final String value) {
		ProductFamily ret;
		if (StringUtils.isEmpty(value)) {
			ret = ProductFamily.BLANK;
		} else {
			try {
				ret = ProductFamily.valueOf(value);
			} catch (IllegalArgumentException ex) {
				ret = ProductFamily.BLANK;
			}
		}
		return ret;
	}
}

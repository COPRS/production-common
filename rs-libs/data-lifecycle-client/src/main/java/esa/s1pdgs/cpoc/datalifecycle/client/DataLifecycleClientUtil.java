package esa.s1pdgs.cpoc.datalifecycle.client;

import java.time.Period;
import java.util.Collection;
import java.util.Date;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.model.RetentionPolicy;

public class DataLifecycleClientUtil {
	
	private static final Logger LOG = LogManager.getLogger(DataLifecycleClientUtil.class);
	
	public static String getFileName(final String obsKey) {
		return FilenameUtils.getName(obsKey);
	}
	
	public static String getProductName(final String obsKey) {
		if (FilenameUtils.getExtension(obsKey).equalsIgnoreCase("ZIP")) {
			return FilenameUtils.getBaseName(obsKey);
		}else {
			return FilenameUtils.getName(obsKey);
		}
	}
	
	public static  Date calculateEvictionDate(
			final Collection<RetentionPolicy> retentionPolicies, 
			final Date creationDate, 
			final ProductFamily productFamily,
			final String fileName
	) {
		for (final RetentionPolicy r : retentionPolicies) {

			if (r.getProductFamily() == productFamily && Pattern.matches(r.getFilePattern(), fileName)) {
				if (r.getRetentionTimeDays() > 0) {
					LOG.info("retention time is {} days for file: {}", r.getRetentionTimeDays(), fileName);
					return Date.from(creationDate.toInstant().plus(Period.ofDays(r.getRetentionTimeDays())));
				} else {
					LOG.info("retention time is unlimited for file: {}", fileName);
					return null;
				}
			}
		}
		LOG.warn("no retention time found for file: {}", fileName);
		return null;
	}
}

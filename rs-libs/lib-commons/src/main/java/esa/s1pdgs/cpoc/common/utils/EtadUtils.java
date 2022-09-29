package esa.s1pdgs.cpoc.common.utils;

import java.time.Duration;
import java.time.Instant;

import esa.s1pdgs.cpoc.metadata.model.SearchMetadata;

public final class EtadUtils {

	public static final boolean isOld(final SearchMetadata metadata, final Duration maxSensingTimeAge) {
		final Instant stopTime = DateUtils.toInstant(metadata.getValidityStop());
		final Instant insertionTime = DateUtils.toInstant(metadata.getInsertionTime());
		
		return insertionTime.minus(maxSensingTimeAge)
				.isAfter(stopTime);
	}
	
	
}

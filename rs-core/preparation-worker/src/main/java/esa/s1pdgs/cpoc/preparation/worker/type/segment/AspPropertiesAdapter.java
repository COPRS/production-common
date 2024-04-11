/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package esa.s1pdgs.cpoc.preparation.worker.type.segment;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.preparation.worker.config.type.AspProperties;

/**
 * Properties adapter for L0ASP segments.
 */
public final class AspPropertiesAdapter {
	
	private static final Logger LOGGER = LogManager.getLogger(AspPropertiesAdapter.class); 
	
	private final int waitingTimeHoursMinimalFast;
	private final int waitingTimeHoursNominalFast;
	private final int waitingTimeHoursMinimalNrtPt;
	private final int waitingTimeHoursNominalNrtPt;
	
	private final boolean disableTimeout;
	
	// --------------------------------------------------------------------------
	
	public AspPropertiesAdapter(
			final boolean disableTimeout,
			final int waitingTimeHoursMinimalFast,
			final int waitingTimeHoursNominalFast,
			final int waitingTimeHoursMinimalNrtPt,
			final int waitingTimeHoursNominalNrtPt
		) {
		this.disableTimeout = disableTimeout;
		this.waitingTimeHoursMinimalFast = waitingTimeHoursMinimalFast;
		this.waitingTimeHoursNominalFast = waitingTimeHoursNominalFast;
		this.waitingTimeHoursMinimalNrtPt = waitingTimeHoursMinimalNrtPt;
		this.waitingTimeHoursNominalNrtPt = waitingTimeHoursNominalNrtPt;
	}

	public static AspPropertiesAdapter of(final AspProperties props) {
        return new AspPropertiesAdapter(
        		props.isDisableTimeout(),
        		props.getWaitingTimeHoursMinimalFast(),
        		props.getWaitingTimeHoursNominalFast(),
        		props.getWaitingTimeHoursMinimalNrtPt(),
        		props.getWaitingTimeHoursNominalNrtPt()
        );
	}
	
	public final boolean isTimeoutReached(final AppDataJob job, final String sensingEndTime, final LocalDateTime now) {
		return !this.disableTimeout && this.checkTimeoutReached(job, sensingEndTime, now);
	}
	
	/**
	 * Calculates when a given job will timeout
	 */
	public Date calculateTimeout(final AppDataJob job) {
		final L0SegmentProduct product = L0SegmentProduct.of(job);
		return calculateTimeout(job, product.getStopTime());
	}
	
	private Date calculateTimeout(final AppDataJob job, String sensingEndTimeStr) {
		final AppDataJobProduct jobProduct = job.getProduct();
    	final String timeliness = (String) jobProduct.getMetadata().get("timeliness");

    	Long minimalTimeout = null;
    	Long nominalTimeout = null;
		if ("PT".equals(timeliness) || "NRT".equals(timeliness)) {
			minimalTimeout = (long) this.waitingTimeHoursMinimalNrtPt;
			nominalTimeout = (long) this.waitingTimeHoursNominalNrtPt;
		} else if ("FAST24".equals(timeliness)) {
			minimalTimeout = (long) this.waitingTimeHoursMinimalFast;
			nominalTimeout = (long) this.waitingTimeHoursNominalFast;
		}
		
		if (null != minimalTimeout) {
			final LocalDateTime sensingStopTime = DateUtils.parse(sensingEndTimeStr);
			final Date jobCreationDate = job.getGeneration().getCreationDate();
			final LocalDateTime jobCreationDateTime = LocalDateTime.ofInstant(jobCreationDate.toInstant(), ZoneId.of("UTC"));

			// wait at least jobCreation + minimal but no longer than sensing stop + nominal
			final LocalDateTime timeoutThreshold = max(sensingStopTime.plusHours(nominalTimeout), jobCreationDateTime.plusHours(minimalTimeout));
			
			return Date.from(timeoutThreshold.atZone(ZoneId.systemDefault()).toInstant());
		}

		return null;
	}
	
	private boolean checkTimeoutReached(final AppDataJob job, final String sensingEndTimeStr,final LocalDateTime now) {
		// S1PRO-1797 / S1PRO-1905: timeout for L0ASP in PT/NRT/FAST mode
		final L0SegmentProduct product = L0SegmentProduct.of(job);
    	final Date timeoutDate = calculateTimeout(job, sensingEndTimeStr);
		
		if (null != timeoutDate) {
			final LocalDateTime timeoutLocalDateTime = LocalDateTime.ofInstant(timeoutDate.toInstant(), ZoneId.of("UTC"));
			
			if(now.isAfter(timeoutLocalDateTime)) {
				LOGGER.warn("Timeout reached for product {}", product.getProductName());
				return true;
			} else {
				LOGGER.debug("product {} has not yet reached timout at {}", product.getProductName(),
						DateUtils.formatToMetadataDateTimeFormat(timeoutLocalDateTime));
			}
		}

		return false;
	}

	private static LocalDateTime max(LocalDateTime a, LocalDateTime b) {
		if (a.isAfter(b)) {
			return a;
		}

		return b;
	}

}

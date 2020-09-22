package esa.s1pdgs.cpoc.ipf.preparation.worker.type.segment;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.AspProperties;

/**
 * Properties adapter for L0ASP segments.
 */
public final class AspPropertiesAdapter {
	
	private static final Logger LOGGER = LogManager.getLogger(AspPropertiesAdapter.class); 
	
	private final int waitingTimeHoursMinimalFast;
	private final int waitingTimeHoursNominalFast;
	private final int waitingTimeHoursMinimalNrtPt;
	private final int waitingTimeHoursNominalNrtPt;
	
	private boolean disableTimeout;
	
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

	public static final AspPropertiesAdapter of(final AspProperties props) {        
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
	
	private boolean checkTimeoutReached(final AppDataJob job, final String sensingEndTimeStr,final LocalDateTime now) {
		// S1PRO-1797 / S1PRO-1905: timeout for L0ASP in PT/NRT/FAST mode
		final L0SegmentProduct product = L0SegmentProduct.of(job);
    	final AppDataJobProduct jobProduct = job.getProduct();
    	final String timeliness = (String) jobProduct.getMetadata().get("timeliness");

    	Long minimalTimeout = null;
    	Long nominalTimeout = null;
		if ("PT".equals(timeliness) || "NRT".equals(timeliness)) {
			minimalTimeout = Long.valueOf( this.waitingTimeHoursMinimalNrtPt);
			nominalTimeout = Long.valueOf( this.waitingTimeHoursNominalNrtPt);
		} else if ("FAST24".equals(timeliness)) {
			minimalTimeout = Long.valueOf( this.waitingTimeHoursMinimalFast);
			nominalTimeout = Long.valueOf( this.waitingTimeHoursNominalFast);
		}
		
		if (null != minimalTimeout && null != nominalTimeout) {
			final LocalDateTime sensingStopTime = DateUtils.parse(sensingEndTimeStr);
			//final Date jobCreationDate = job.getGeneration().getCreationDate();
			final Date jobCreationDate = job.getCreationDate();
			final LocalDateTime jobCreationDateTime = LocalDateTime.ofInstant(jobCreationDate.toInstant(), ZoneId.of("UTC"));
			
			final LocalDateTime timeoutThreshold;
			if(sensingStopTime.plusHours(nominalTimeout).minusHours(minimalTimeout).isBefore(now)) {
				timeoutThreshold = jobCreationDateTime.plusHours(minimalTimeout);
			}else {
				timeoutThreshold = sensingStopTime.plusHours(nominalTimeout);
			}
			
			if(!now.isBefore(timeoutThreshold)) {
				LOGGER.warn("Timeout reached for product {}", product.getProductName());
				return true;
			}else {
				LOGGER.debug("product {} has not yet reached timout at {}", product.getProductName(),
						DateUtils.formatToMetadataDateTimeFormat(timeoutThreshold));
			}
		}

		return false;
	}

}

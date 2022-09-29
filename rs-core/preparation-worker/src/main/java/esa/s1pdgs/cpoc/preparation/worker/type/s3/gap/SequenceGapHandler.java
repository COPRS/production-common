package esa.s1pdgs.cpoc.preparation.worker.type.s3.gap;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.metadata.model.S3Metadata;

public class SequenceGapHandler extends AbstractGapHandler {
	
	private static final Logger LOGGER = LogManager.getLogger(SequenceGapHandler.class);
	
	private final boolean disableFirstLastWaiting;
	
	public SequenceGapHandler(final boolean disableFirstLastWaiting) {
		this.disableFirstLastWaiting = disableFirstLastWaiting;
	}
	
	
	@Override
	public boolean isCovered(LocalDateTime startTime, LocalDateTime stopTime, List<S3Metadata> products) {
		final S3Metadata first = products.get(0);
		final S3Metadata last = products.get(products.size() - 1);
		
		final LocalDateTime firstStart = LocalDateTime.parse(first.getValidityStart(),
				DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'"));
		final LocalDateTime lastStop = LocalDateTime.parse(last.getValidityStop(),
				DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'"));
		
		// Interval is not covered if the start time is not covered
		// Exception: If the earliest granule has position FIRST and those products
		// should not wait for left neighbors (ex. OLCI)
		if (firstStart.isAfter(startTime)
				&& !(disableFirstLastWaiting && first.getGranulePosition().equals("FIRST"))) {
			LOGGER.info("CheckCoverage: First start time is after interval beginning. Interval is not covered");
			return false;
		}

		// Interval is not covered if the stop time is not covered
		// Exception: If the last granule has position LAST and those products
		// should not wait for right neighbors (ex. OLCI)
		if (lastStop.isBefore(stopTime) && !(disableFirstLastWaiting && last.getGranulePosition().equals("LAST"))) {
			LOGGER.info("CheckCoverage: Last stop time is before interval ending. Interval is not covered");
			return false;
		}

		return isGranuleContinuous(products);
	}
	
	/**
	 * Check if the granule numbers are continuous.
	 * 
	 * Edge case: if the granule position is LAST the position of the successor has
	 * to be FIRST
	 * 
	 * @param products List of products which should be checked for continuity
	 * @return true if list is continuous, false if not
	 */
	private boolean isGranuleContinuous(final List<S3Metadata> products) {
		for (int i = 0; i < products.size() - 1; i++) {
			final S3Metadata product = products.get(i);
			final S3Metadata successor = products.get(i + 1);

			if (product.getGranulePosition().equals("LAST")) {
				if (!successor.getGranulePosition().equals("FIRST")) {
					LOGGER.info("Successor to LAST was not FIRST (actual: {}). List of products is not continuous.",
							successor.getGranulePosition());
					return false;
				}
			} else {
				if (product.getGranuleNumber() + 1 != successor.getGranuleNumber()) {
					LOGGER.info("Granule number not continuous. Expected {}, actual {}", product.getGranuleNumber() + 1,
							successor.getGranuleNumber());
					return false;
				}
			}
		}

		return true;
	}
}

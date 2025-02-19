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

package esa.s1pdgs.cpoc.preparation.worker.type.s3.gap;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.common.time.TimeInterval;
import esa.s1pdgs.cpoc.metadata.model.S3Metadata;

public class ThresholdGapHandler extends AbstractGapHandler {

	private static final Logger LOGGER = LogManager.getLogger(ThresholdGapHandler.class);
	private static final long SECONDS_TO_NANOS = 1000000000L;
	
	private final double threshold;
	
	public ThresholdGapHandler(final double threshold) {
		this.threshold = threshold;
	}
	
	@Override
	public boolean isCovered(LocalDateTime startTime, LocalDateTime stopTime, List<S3Metadata> products) {		
		LocalDateTime currentStart = startTime;
		for (S3Metadata product : products) {
			final LocalDateTime productStart = LocalDateTime.parse(product.getValidityStart(),
					DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'"));
			final LocalDateTime productStop = LocalDateTime.parse(product.getValidityStop(),
					DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'"));
			
			if (currentStart.plusNanos((long) (threshold * SECONDS_TO_NANOS)).isBefore(productStart)) {
				LOGGER.debug("Big gap detected - interval not covered");
				return false;
			}
			
			currentStart = productStop;
		}
		
		if (stopTime.minusNanos((long) (threshold * SECONDS_TO_NANOS)).isAfter(currentStart)) {
			LOGGER.debug("Last stop is too early (Big gap at stop) - interval not covered");
			return false;
		}
		
		return true;
	}
	
	/**
	 * Merge TimeIntervals from products together if they are close to each other
	 * (@see threshold parameter). Return list of merged TimeIntervals (at least
	 * one).
	 * 
	 * @param jobInterval  TimeInterval of the job. Provides a lower and upper bound
	 *                     for the intervals
	 * @param intervals    list of intervals of the products to merge
	 * @return list fo merged time intervals
	 */
	public final List<TimeInterval> mergeTimeIntervals(TimeInterval jobInterval, List<TimeInterval> intervals) {
		Iterator<TimeInterval> it = intervals.iterator();

		List<TimeInterval> newIntervals = new ArrayList<>();
		TimeInterval interval = it.next();
		TimeInterval newInterval; 

		if (!jobInterval.getStart().isBefore(interval.getStart())) {
			// If the first interval has an earlier start time than the lower bound, set
			// start time of first interval to lower bound
			newInterval = new TimeInterval(jobInterval.getStart(),interval.getStop());
		} else {
			newInterval = new TimeInterval(interval.getStart(),interval.getStop());
		}

		while (it.hasNext()) {
			interval = it.next();

			if (!newInterval.getStop().equals(interval.getStart())
					&& isBigGap(newInterval.getStop(), interval.getStart())) {
				// If there is a big gap between the two intervals, add the current TimeInterval
				// and start creating a new one
				newIntervals.add(newInterval);
				newInterval = interval;
			} else {
				// Otherwise extend current TimeInterval
				newInterval = new TimeInterval(newInterval.getStart(),interval.getStop());
			}
		}

		// At the end, make sure the last TimeInterval does not exceed the upper bound
		if (newInterval.getStop().isAfter(jobInterval.getStop())) {
			newInterval = new TimeInterval(newInterval.getStart(),jobInterval.getStop());
		}
		newIntervals.add(newInterval);

		return newIntervals;
	}
	
	/**
	 * Checks if a gap is considered a "big gap" in hindsight of the configured
	 * threshold.
	 */
	private final boolean isBigGap(LocalDateTime gapStart, LocalDateTime gapStop) {
		return gapStart.plusNanos((long) (threshold * SECONDS_TO_NANOS)).isBefore(gapStop);
	}
}

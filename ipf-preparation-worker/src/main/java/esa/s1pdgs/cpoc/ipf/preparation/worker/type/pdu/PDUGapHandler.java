package esa.s1pdgs.cpoc.ipf.preparation.worker.type.pdu;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import esa.s1pdgs.cpoc.ipf.preparation.worker.model.TimeInterval;

public class PDUGapHandler {

	private PDUGapHandler() {
		throw new IllegalArgumentException("Instance of this class not allowed");
	}

	/**
	 * Merge TimeIntervals from products together if they are close to each other
	 * (@see threshold parameter). Return list of merged TimeIntervals (at least
	 * one).
	 * 
	 * @param jobInterval  TimeInterval of the job. Provides a lower and upper bound
	 *                     for the intervals
	 * @param intervals    list of intervals of the products to merge
	 * @param thresholdInS threshold in seconds. If a gap between to products is
	 *                     bigger than this threshold it will produce a new time
	 *                     interval
	 * @return list fo merged time intervals
	 */
	public static final List<TimeInterval> mergeTimeIntervals(TimeInterval jobInterval, List<TimeInterval> intervals,
			double thresholdInS) {
		Iterator<TimeInterval> it = intervals.iterator();

		List<TimeInterval> newIntervals = new ArrayList<>();
		TimeInterval interval = it.next();
		TimeInterval newInterval = new TimeInterval();

		if (!jobInterval.getStart().isBefore(interval.getStart())) {
			// If the first interval has an earlier start time than the lower bound, set
			// start time of first interval to lower bound
			newInterval.setStart(jobInterval.getStart());
			newInterval.setStop(interval.getStop());
		} else {
			newInterval.setStart(interval.getStart());
			newInterval.setStop(interval.getStop());
		}

		while (it.hasNext()) {
			interval = it.next();

			if (!newInterval.getStop().equals(interval.getStart())
					&& isBigGap(newInterval.getStop(), interval.getStart(), thresholdInS)) {
				// If there is a big gap between the two intervals, add the current TimeInterval
				// and start creating a new one
				newIntervals.add(newInterval);
				newInterval = interval;
			} else {
				// Otherwise extend current TimeInterval
				newInterval.setStop(interval.getStop());
			}
		}

		// At the end, make sure the last TimeInterval does not exceed the upper bound
		if (newInterval.getStop().isAfter(jobInterval.getStop())) {
			newInterval.setStop(jobInterval.getStop());
		}
		newIntervals.add(newInterval);

		return newIntervals;
	}

	/**
	 * Checks if a gap is considered a "big gap" in hindsight of the configured
	 * threshold.
	 */
	private static final boolean isBigGap(LocalDateTime gapStart, LocalDateTime gapStop, double threshold) {
		return gapStart.plusNanos((long) (threshold * 1000000000L)).isBefore(gapStop);
	}
}

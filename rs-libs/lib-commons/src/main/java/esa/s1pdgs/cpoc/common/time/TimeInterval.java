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

package esa.s1pdgs.cpoc.common.time;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Objects;

public final class TimeInterval implements Comparable<TimeInterval> {
	private final LocalDateTime start;
	private final LocalDateTime stop;
		
	public TimeInterval(final LocalDateTime start, final LocalDateTime stop) {
		this.start = start;
		this.stop = stop;
	}

	public final LocalDateTime getStart() {
		return start;
	}

	public final LocalDateTime getStop() {
		return stop;
	}

	@Override
	public final int hashCode() {
		return Objects.hash(start, stop);
	}

	@Override
	public final boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final TimeInterval other = (TimeInterval) obj;
		return Objects.equals(start, other.start) && 
				Objects.equals(stop, other.stop);
	}

	@Override
	public final String toString() {
		return "TimeInterval [start=" + start + ", stop=" + stop + "]";
	}

	@Override
	public int compareTo(final TimeInterval other) {
		final int retval = start.compareTo(other.start);
		if (retval == 0) {
			return stop.compareTo(other.stop);
		}
		return retval;
	};
	
	public final boolean intersects(final TimeInterval interval) {
		return TimeIntervals.intersects(this, interval);
	}
	
	public final long lengthInNanos() {
		return this.start.until(this.stop, ChronoUnit.NANOS);
	}
}

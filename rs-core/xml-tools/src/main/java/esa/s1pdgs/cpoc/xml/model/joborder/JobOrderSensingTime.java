package esa.s1pdgs.cpoc.xml.model.joborder;

import java.time.format.DateTimeFormatter;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class of sensing time of a session in job order
 * 
 * @author Cyrielle Gailliard
 *
 */
@XmlRootElement(name = "Sensing_Time")
@XmlAccessorType(XmlAccessType.NONE)
public class JobOrderSensingTime {

    public final static DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(
            "yyyyMMdd_HHmmssSSSSSS");

	/**
	 * Sensing start time in format YYYYMMDD_HHmmssSSSSSS
	 */
	@XmlElement(name = "Start")
	private String start;

	/**
	 * Sensing stop time in format YYYYMMDD_HHmmssSSSSSS
	 */
	@XmlElement(name = "Stop")
	private String stop;

	/**
	 * 
	 */
	public JobOrderSensingTime() {
		super();
	}

	/**
	 * @param start
	 * @param stop
	 */
	public JobOrderSensingTime(final String start, final String stop) {
		this();
		this.start = start;
		this.stop = stop;
	}

	/**
	 * 
	 * @param obj
	 */
	public JobOrderSensingTime(final JobOrderSensingTime obj) {
		this(obj.getStart(), obj.getStop());
	}

	/**
	 * @return the start
	 */
	public String getStart() {
		return start;
	}

	/**
	 * @param start
	 *            the start to set
	 */
	public void setStart(final String start) {
		this.start = start;
	}

	/**
	 * @return the stop
	 */
	public String getStop() {
		return stop;
	}

	/**
	 * @param stop
	 *            the stop to set
	 */
	public void setStop(final String stop) {
		this.stop = stop;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("{start: %s, stop: %s}", start, stop);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(start, stop);
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		boolean ret;
		if (this == obj) {
			ret = true;
		} else if (obj == null || getClass() != obj.getClass()) {
			ret = false;
		} else {
			JobOrderSensingTime other = (JobOrderSensingTime) obj;
			ret = Objects.equals(start, other.start) && Objects.equals(stop, other.stop);
		}
		return ret;
	}

}

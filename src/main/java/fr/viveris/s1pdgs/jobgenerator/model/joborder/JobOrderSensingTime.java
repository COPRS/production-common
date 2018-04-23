package fr.viveris.s1pdgs.jobgenerator.model.joborder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class of sensing time of a session in job order
 * @author Cyrielle Gailliard
 *
 */
@XmlRootElement(name = "Sensing_Time")
@XmlAccessorType(XmlAccessType.NONE)
public class JobOrderSensingTime {
	
	public final static String DATE_FORMAT = "yyyyMMdd_HHmmssSSSSSS";
	
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
	public JobOrderSensingTime(String start, String stop) {
		this();
		this.start = start;
		this.stop = stop;
	}
	
	public JobOrderSensingTime(JobOrderSensingTime obj) {
		this(obj.getStart(), obj.getStop());
	}

	/**
	 * @return the start
	 */
	public String getStart() {
		return start;
	}

	/**
	 * @param start the start to set
	 */
	public void setStart(String start) {
		this.start = start;
	}

	/**
	 * @return the stop
	 */
	public String getStop() {
		return stop;
	}

	/**
	 * @param stop the stop to set
	 */
	public void setStop(String stop) {
		this.stop = stop;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "JobOrderSensingTime [start=" + start + ", stop=" + stop + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((start == null) ? 0 : start.hashCode());
		result = prime * result + ((stop == null) ? 0 : stop.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JobOrderSensingTime other = (JobOrderSensingTime) obj;
		if (start == null) {
			if (other.start != null)
				return false;
		} else if (!start.equals(other.start))
			return false;
		if (stop == null) {
			if (other.stop != null)
				return false;
		} else if (!stop.equals(other.stop))
			return false;
		return true;
	}

}

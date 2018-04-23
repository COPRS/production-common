package fr.viveris.s1pdgs.jobgenerator.model.joborder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class of a job order time interval
 * @author Cyrielle Gailliard
 *
 */
@XmlRootElement(name = "Time_Interval")
@XmlAccessorType(XmlAccessType.NONE)
public class JobOrderTimeInterval {
	
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSSSSS");
	
	/**
	 * Validity start time (format YYYYMMDD_HHmmssSSSSSS)
	 */
	@XmlElement(name = "Start")
	private String start;
	
	/**
	 * Validity start time (format YYYYMMDD_HHmmssSSSSSS)
	 */
	@XmlElement(name = "Stop")
	private String stop;
	
	/**
	 * File name
	 */
	@XmlElement(name = "File_Name")
	private String fileName;

	/**
	 * Default constructor
	 */
	public JobOrderTimeInterval() {
		super();
	}

	/**
	 * Constructor using fields
	 * @param start
	 * @param stop
	 * @param fileName
	 */
	public JobOrderTimeInterval(String start, String stop, String fileName) {
		this();
		this.start = start;
		this.stop = stop;
		this.fileName = fileName;
	}

	/**
	 * Constructor using fields
	 * @param start
	 * @param stop
	 * @param fileName
	 */
	public JobOrderTimeInterval(String start, String stop, String fileName, DateTimeFormatter formatInputs) {
		this();
		LocalDateTime startDate = LocalDateTime.parse(start, formatInputs);
		this.start = startDate.format(DATE_FORMATTER);
		LocalDateTime stopDate = LocalDateTime.parse(stop, formatInputs);
		this.stop = stopDate.format(DATE_FORMATTER);
		this.fileName = fileName;
	}

	/**
	 * Clone
	 * @param obj
	 */
	public JobOrderTimeInterval(JobOrderTimeInterval obj) {
		this(obj.getStart(), obj.getStop(), obj.getFileName());
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

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "JobOrderTimeInterval [start=" + start + ", stop=" + stop + ", fileName=" + fileName + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
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
		JobOrderTimeInterval other = (JobOrderTimeInterval) obj;
		if (fileName == null) {
			if (other.fileName != null)
				return false;
		} else if (!fileName.equals(other.fileName))
			return false;
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

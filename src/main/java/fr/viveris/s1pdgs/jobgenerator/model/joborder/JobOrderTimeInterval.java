package fr.viveris.s1pdgs.jobgenerator.model.joborder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class of a job order time interval
 * 
 * @author Cyrielle Gailliard
 *
 */
@XmlRootElement(name = "Time_Interval")
@XmlAccessorType(XmlAccessType.NONE)
public class JobOrderTimeInterval {

	/**
	 * 
	 */
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
	 * 
	 * @param start
	 * @param stop
	 * @param fileName
	 */
	public JobOrderTimeInterval(final String start, final String stop, final String fileName) {
		this();
		this.start = start;
		this.stop = stop;
		this.fileName = fileName;
	}

	/**
	 * Constructor using fields
	 * 
	 * @param start
	 * @param stop
	 * @param fileName
	 */
	public JobOrderTimeInterval(final String start, final String stop, final String fileName,
			final DateTimeFormatter formatInputs) {
		this();
		LocalDateTime startDate = LocalDateTime.parse(start, formatInputs);
		this.start = startDate.format(DATE_FORMATTER);
		LocalDateTime stopDate = LocalDateTime.parse(stop, formatInputs);
		this.stop = stopDate.format(DATE_FORMATTER);
		this.fileName = fileName;
	}

	/**
	 * Clone
	 * 
	 * @param obj
	 */
	public JobOrderTimeInterval(final JobOrderTimeInterval obj) {
		this(obj.getStart(), obj.getStop(), obj.getFileName());
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
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName
	 *            the fileName to set
	 */
	public void setFileName(final String fileName) {
		this.fileName = fileName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("{start: %s, stop: %s, fileName: %s}", start, stop, fileName);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(start, stop, fileName);
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
			JobOrderTimeInterval other = (JobOrderTimeInterval) obj;
			ret = Objects.equals(start, other.start) && Objects.equals(stop, other.stop)
					&& Objects.equals(fileName, other.fileName);
		}
		return ret;
	}

}

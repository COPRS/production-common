package fr.viveris.s1pdgs.jobgenerator.model.joborder;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import fr.viveris.s1pdgs.jobgenerator.model.ProductFamily;
import fr.viveris.s1pdgs.jobgenerator.model.joborder.enums.JobOrderFileNameType;

/**
 * Class description a job order input
 * 
 * @author Cyrielle Gailliard
 *
 */
@XmlRootElement(name = "Input")
@XmlAccessorType(XmlAccessType.NONE)
public class JobOrderInput {

	/**
	 * File type (correspond to the product type)
	 */
	@XmlElement(name = "File_Type")
	private String fileType;

	/**
	 * File name type
	 */
	@XmlElement(name = "File_Name_Type")
	private JobOrderFileNameType fileNameType;

	/**
	 * List of filenames
	 */
	@XmlElementWrapper(name = "List_of_File_Names")
	@XmlElement(name = "File_Name")
	private List<JobOrderInputFile> filenames;

	/**
	 * Number of file names. Automatically built.
	 */
	@XmlPath("List_of_File_Names/@count")
	private int nbFilenames;

	/**
	 * List of time intervals. One per files
	 */
	@XmlElementWrapper(name = "List_of_Time_Intervals")
	@XmlElement(name = "Time_Interval")
	private List<JobOrderTimeInterval> timeIntervals;

	/**
	 * Number of time intervals. Automatically built.
	 */
	@XmlPath("List_of_Time_Intervals/@count")
	private int nbTimeIntervals;

	// TODO update when will be in tasktable
	private ProductFamily family;

	/**
	 * Default constructor
	 */
	public JobOrderInput() {
		super();
		this.timeIntervals = new ArrayList<>();
		this.filenames = new ArrayList<>();
		this.nbTimeIntervals = 0;
		this.nbFilenames = 0;
		this.family = ProductFamily.CONFIG;
	}

	/**
	 * @param fileType
	 * @param fileNameType
	 * @param filenames
	 * @param timeIntervals
	 * @param family
	 */
	public JobOrderInput(String fileType, JobOrderFileNameType fileNameType, List<JobOrderInputFile> filenames,
			List<JobOrderTimeInterval> timeIntervals, ProductFamily family) {
		this();
		this.fileType = fileType;
		this.fileNameType = fileNameType;
		this.family = family;
		if (filenames != null) {
			filenames.forEach(filename -> {
				this.filenames.add(new JobOrderInputFile(filename));
			});
			this.nbFilenames = this.filenames.size();
		}
		if (timeIntervals != null) {
			timeIntervals.forEach(timeInterval -> {
				this.timeIntervals.add(new JobOrderTimeInterval(timeInterval));
			});
			this.nbTimeIntervals = this.timeIntervals.size();
		}
	}
	

	/**
	 * Clone
	 * 
	 * @param obj
	 */
	public JobOrderInput(JobOrderInput obj) {
		this(obj.getFileType(), obj.getFileNameType(), obj.getFilenames(), obj.getTimeIntervals(), obj.getFamily());
	}


	/**
	 * @return the fileType
	 */
	public String getFileType() {
		return fileType;
	}

	/**
	 * @param fileType
	 *            the fileType to set
	 */
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	/**
	 * @return the fileNameType
	 */
	public JobOrderFileNameType getFileNameType() {
		return fileNameType;
	}

	/**
	 * @param fileNameType
	 *            the fileNameType to set
	 */
	public void setFileNameType(JobOrderFileNameType fileNameType) {
		this.fileNameType = fileNameType;
	}

	/**
	 * @return the filenames
	 */
	public List<JobOrderInputFile> getFilenames() {
		return filenames;
	}

	/**
	 * @param filenames
	 *            the filenames to set
	 */
	public void addFilename(String filename, String objectStorageKey) {
		this.filenames.add(new JobOrderInputFile(filename, objectStorageKey));
		this.nbFilenames++;
	}

	/**
	 * @return the timeIntervals
	 */
	public List<JobOrderTimeInterval> getTimeIntervals() {
		return timeIntervals;
	}

	/**
	 * @param timeIntervals
	 *            the timeIntervals to set
	 */
	public void addTimeInterval(JobOrderTimeInterval timeInterval) {
		this.timeIntervals.add(timeInterval);
		this.nbTimeIntervals++;
	}

	/**
	 * @return the nbFilenames
	 */
	public int getNbFilenames() {
		return nbFilenames;
	}

	/**
	 * @return the nbTimeIntervals
	 */
	public int getNbTimeIntervals() {
		return nbTimeIntervals;
	}

	/**
	 * @return the family
	 */
	public ProductFamily getFamily() {
		return family;
	}

	/**
	 * @param family
	 *            the family to set
	 */
	public void setFamily(ProductFamily family) {
		this.family = family;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "JobOrderInput [fileType=" + fileType + ", fileNameType=" + fileNameType + ", filenames=" + filenames
				+ ", nbFilenames=" + nbFilenames + ", timeIntervals=" + timeIntervals + ", nbTimeIntervals="
				+ nbTimeIntervals + ", family=" + family + "]";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((family == null) ? 0 : family.hashCode());
		result = prime * result + ((fileNameType == null) ? 0 : fileNameType.hashCode());
		result = prime * result + ((fileType == null) ? 0 : fileType.hashCode());
		result = prime * result + ((filenames == null) ? 0 : filenames.hashCode());
		result = prime * result + nbFilenames;
		result = prime * result + nbTimeIntervals;
		result = prime * result + ((timeIntervals == null) ? 0 : timeIntervals.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
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
		JobOrderInput other = (JobOrderInput) obj;
		if (family != other.family)
			return false;
		if (fileNameType != other.fileNameType)
			return false;
		if (fileType == null) {
			if (other.fileType != null)
				return false;
		} else if (!fileType.equals(other.fileType))
			return false;
		if (filenames == null) {
			if (other.filenames != null)
				return false;
		} else if (!filenames.equals(other.filenames))
			return false;
		if (nbFilenames != other.nbFilenames)
			return false;
		if (nbTimeIntervals != other.nbTimeIntervals)
			return false;
		if (timeIntervals == null) {
			if (other.timeIntervals != null)
				return false;
		} else if (!timeIntervals.equals(other.timeIntervals))
			return false;
		return true;
	}

}

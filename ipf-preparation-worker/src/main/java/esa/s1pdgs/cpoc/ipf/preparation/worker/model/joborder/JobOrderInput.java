package esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.persistence.oxm.annotations.XmlPath;
import org.springframework.util.CollectionUtils;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.enums.JobOrderFileNameType;

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

	/**
	 * Product family
	 */
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
		this.family = ProductFamily.AUXILIARY_FILE;
	}

	/**
	 */
	public JobOrderInput(final String fileType, final JobOrderFileNameType fileNameType,
			final List<JobOrderInputFile> filenames, final List<JobOrderTimeInterval> timeIntervals,
			final ProductFamily family) {
		this();
		this.fileType = fileType;
		this.fileNameType = fileNameType;
		this.family = family;
		if (filenames != null) {
			filenames.forEach(filename -> this.filenames.add(new JobOrderInputFile(filename)));
			this.nbFilenames = this.filenames.size();
		}
		if (timeIntervals != null) {
			timeIntervals.forEach(timeInterval -> this.timeIntervals.add(new JobOrderTimeInterval(timeInterval)));
			this.nbTimeIntervals = this.timeIntervals.size();
		}
	}

	/**
	 * Clone
	 * 
	 */
	public JobOrderInput(final JobOrderInput obj) {
		this();
		this.family = obj.getFamily();
		this.fileType = obj.getFileType();
		this.fileNameType = obj.getFileNameType();
		if (!CollectionUtils.isEmpty(obj.getFilenames())) {
			this.filenames.addAll(obj.getFilenames().stream().filter(Objects::nonNull)
					.map(JobOrderInputFile::new).collect(Collectors.toList()));
			this.nbFilenames = this.filenames.size();
		}
		if (!CollectionUtils.isEmpty(obj.getTimeIntervals())) {
			this.timeIntervals.addAll(obj.getTimeIntervals().stream().filter(Objects::nonNull)
					.map(JobOrderTimeInterval::new).collect(Collectors.toList()));
			this.nbTimeIntervals = this.timeIntervals.size();
		}
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
	public void setFileType(final String fileType) {
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
	public void setFileNameType(final JobOrderFileNameType fileNameType) {
		this.fileNameType = fileNameType;
	}

	/**
	 * @return the filenames
	 */
	public List<JobOrderInputFile> getFilenames() {
		return filenames;
	}

	/**
	 * @param filename
	 *            the filenames to set
	 */
	public void addFilename(final String filename, final String objectStorageKey) {
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
	 * @param timeInterval
	 *            the timeInterval to set
	 */
	public void addTimeInterval(final JobOrderTimeInterval timeInterval) {
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
	public void setFamily(final ProductFamily family) {
		this.family = family;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format(
				"{fileType: %s, fileNameType: %s, filenames: %s, nbFilenames: %s, timeIntervals: %s, nbTimeIntervals: %s, family: %s}",
				fileType, fileNameType, filenames, nbFilenames, timeIntervals, nbTimeIntervals, family);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(fileType, fileNameType, filenames, nbFilenames, timeIntervals, nbTimeIntervals, family);
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
			JobOrderInput other = (JobOrderInput) obj;
			ret = Objects.equals(fileType, other.fileType) && Objects.equals(fileNameType, other.fileNameType)
					&& Objects.equals(filenames, other.filenames) && nbFilenames == other.nbFilenames
					&& Objects.equals(timeIntervals, other.timeIntervals) && nbTimeIntervals == other.nbTimeIntervals
					&& Objects.equals(family, other.family);
		}
		return ret;
	}

}

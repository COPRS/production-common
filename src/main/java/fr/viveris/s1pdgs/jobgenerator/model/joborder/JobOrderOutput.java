package fr.viveris.s1pdgs.jobgenerator.model.joborder;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import esa.s1pdgs.cpoc.common.ProductFamily;
import fr.viveris.s1pdgs.jobgenerator.model.joborder.enums.JobOrderDestination;
import fr.viveris.s1pdgs.jobgenerator.model.joborder.enums.JobOrderFileNameType;

/**
 * Class describing a job order output
 * 
 * @author Cyrielle Gailliard
 *
 */
@XmlRootElement(name = "Output")
@XmlAccessorType(XmlAccessType.NONE)
public class JobOrderOutput {

	/**
	 * Indicate if the output is mandatory or not
	 */
	@XmlAttribute(name = "mandatory")
	private boolean mandatory;

	/**
	 * Type of the output (correspond to the product type)
	 */
	@XmlElement(name = "File_Type")
	private String fileType;

	/**
	 * Type of the filename (REGEXP | DIRECTORY)
	 */
	@XmlElement(name = "File_Name_Type")
	private JobOrderFileNameType fileNameType;

	/**
	 * Filename
	 */
	@XmlElement(name = "File_Name")
	private String fileName;

	/**
	 * Product family
	 */
	private ProductFamily family;

	/**
	 * Destination
	 */
	private JobOrderDestination destination;

	/**
	 * Default constructor
	 */
	public JobOrderOutput() {
		super();
		this.mandatory = false;
		this.fileNameType = JobOrderFileNameType.BLANK;
		this.family = ProductFamily.AUXILIARY_FILE;
		this.fileName = "";
	}

	/**
	 * Constructor using fields
	 * 
	 * @param fileType
	 * @param fileNameType
	 * @param fileName
	 */
	public JobOrderOutput(final String fileType, final JobOrderFileNameType fileNameType, final String fileName) {
		this();
		this.fileType = fileType;
		this.fileNameType = fileNameType;
		this.fileName = fileName;
	}

	/**
	 * Constructor using fields
	 * 
	 * @param mandatory
	 * @param fileType
	 * @param fileNameType
	 * @param fileName
	 */
	public JobOrderOutput(final String fileType, final JobOrderFileNameType fileNameType, final String fileName,
			final JobOrderDestination destination, final boolean mandatory, final ProductFamily family) {
		this(fileType, fileNameType, fileName);
		this.destination = destination;
		this.mandatory = mandatory;
		this.family = family;
	}

	/**
	 * Clone
	 * 
	 * @param obj
	 */
	public JobOrderOutput(final JobOrderOutput obj) {
		this(obj.getFileType(), obj.getFileNameType(), obj.getFileName(), obj.getDestination(), obj.isMandatory(),
				obj.getFamily());
	}

	/**
	 * @return the mandatory
	 */
	public boolean isMandatory() {
		return mandatory;
	}

	/**
	 * @param mandatory
	 *            the mandatory to set
	 */
	public void setMandatory(final boolean mandatory) {
		this.mandatory = mandatory;
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
	 * @return the destination
	 */
	public JobOrderDestination getDestination() {
		return destination;
	}

	/**
	 * @param destination
	 *            the destination to set
	 */
	public void setDestination(final JobOrderDestination destination) {
		this.destination = destination;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format(
				"{mandatory: %s, fileType: %s, fileNameType: %s, fileName: %s, family: %s, destination: %s}", mandatory,
				fileType, fileNameType, fileName, family, destination);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(mandatory, fileType, fileNameType, fileName, family, destination);
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
			JobOrderOutput other = (JobOrderOutput) obj;
			ret = mandatory == other.mandatory && Objects.equals(fileType, other.fileType)
					&& Objects.equals(fileNameType, other.fileNameType) && Objects.equals(fileName, other.fileName)
					&& Objects.equals(family, other.family) && Objects.equals(destination, other.destination);
		}
		return ret;
	}

}

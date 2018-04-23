package fr.viveris.s1pdgs.jobgenerator.model.joborder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import fr.viveris.s1pdgs.jobgenerator.model.ProductFamily;
import fr.viveris.s1pdgs.jobgenerator.model.joborder.enums.JobOrderDestination;
import fr.viveris.s1pdgs.jobgenerator.model.joborder.enums.JobOrderFileNameType;

/**
 * Class describing a job order output
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
	
	private ProductFamily family;
	
	private JobOrderDestination destination;

	/**
	 * Default constructor
	 */
	public JobOrderOutput() {
		super();
		this.mandatory = false;
		this.fileNameType = JobOrderFileNameType.BLANK;
		this.family = ProductFamily.CONFIG;
		this.fileName = "";
	}

	/**
	 * Constructor using fields
	 * @param fileType
	 * @param fileNameType
	 * @param fileName
	 */
	public JobOrderOutput(String fileType, JobOrderFileNameType fileNameType, String fileName) {
		this();
		this.fileType = fileType;
		this.fileNameType = fileNameType;
		this.fileName = fileName;
	}

	/**
	 * Constructor using fields
	 * @param mandatory
	 * @param fileType
	 * @param fileNameType
	 * @param fileName
	 */
	public JobOrderOutput(String fileType, JobOrderFileNameType fileNameType, String fileName, JobOrderDestination destination, boolean mandatory, ProductFamily family) {
		this(fileType, fileNameType, fileName);
		this.destination = destination;
		this.mandatory = mandatory;
		this.family = family;
	}
	
	/**
	 * Clone
	 * @param obj
	 */
	public JobOrderOutput(JobOrderOutput obj) {
		this(obj.getFileType(), obj.getFileNameType(), obj.getFileName(), obj.getDestination(), obj.isMandatory(), obj.getFamily());
	}

	/**
	 * @return the mandatory
	 */
	public boolean isMandatory() {
		return mandatory;
	}


	/**
	 * @param mandatory the mandatory to set
	 */
	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}


	/**
	 * @return the fileType
	 */
	public String getFileType() {
		return fileType;
	}

	/**
	 * @param fileType the fileType to set
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
	 * @param fileNameType the fileNameType to set
	 */
	public void setFileNameType(JobOrderFileNameType fileNameType) {
		this.fileNameType = fileNameType;
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

	/**
	 * @return the family
	 */
	public ProductFamily getFamily() {
		return family;
	}

	/**
	 * @param family the family to set
	 */
	public void setFamily(ProductFamily family) {
		this.family = family;
	}

	/**
	 * @return the destination
	 */
	public JobOrderDestination getDestination() {
		return destination;
	}

	/**
	 * @param destination the destination to set
	 */
	public void setDestination(JobOrderDestination destination) {
		this.destination = destination;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "JobOrderOutput [mandatory=" + mandatory + ", fileType=" + fileType + ", fileNameType=" + fileNameType
				+ ", fileName=" + fileName + ", family=" + family + ", destination=" + destination + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((destination == null) ? 0 : destination.hashCode());
		result = prime * result + ((family == null) ? 0 : family.hashCode());
		result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
		result = prime * result + ((fileNameType == null) ? 0 : fileNameType.hashCode());
		result = prime * result + ((fileType == null) ? 0 : fileType.hashCode());
		result = prime * result + (mandatory ? 1231 : 1237);
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
		JobOrderOutput other = (JobOrderOutput) obj;
		if (destination != other.destination)
			return false;
		if (family != other.family)
			return false;
		if (fileName == null) {
			if (other.fileName != null)
				return false;
		} else if (!fileName.equals(other.fileName))
			return false;
		if (fileNameType != other.fileNameType)
			return false;
		if (fileType == null) {
			if (other.fileType != null)
				return false;
		} else if (!fileType.equals(other.fileType))
			return false;
		if (mandatory != other.mandatory)
			return false;
		return true;
	}
	
}

package fr.viveris.s1pdgs.jobgenerator.controller.dto;

/**
 * DTO object representing a task to be executed for the job
 * @author Cyrielle Gailliard
 * @see JobDto
 *
 */
public class JobTaskDto {
	
	/**
	 * Absolute path of the binary
	 */
	private String binaryPath;

	/**
	 * Default constructor
	 */
	public JobTaskDto() {
		
	}

	/**
	 * Constructor using fields
	 * @param binaryPath
	 */
	public JobTaskDto(String binaryPath) {
		this();
		this.binaryPath = binaryPath;
	}

	/**
	 * @return the binaryPath
	 */
	public String getBinaryPath() {
		return binaryPath;
	}

	/**
	 * @param binaryPath the binaryPath to set
	 */
	public void setBinaryPath(String binaryPath) {
		this.binaryPath = binaryPath;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "JobTaskDto [binaryPath=" + binaryPath + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((binaryPath == null) ? 0 : binaryPath.hashCode());
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
		JobTaskDto other = (JobTaskDto) obj;
		if (binaryPath == null) {
			if (other.binaryPath != null)
				return false;
		} else if (!binaryPath.equals(other.binaryPath))
			return false;
		return true;
	}

}

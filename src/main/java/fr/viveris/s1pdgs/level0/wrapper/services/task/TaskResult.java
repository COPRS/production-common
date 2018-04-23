package fr.viveris.s1pdgs.level0.wrapper.services.task;

/**
 * 
 * @author Cyrielle Gailliard
 *
 */
public class TaskResult {
	
	private String binary;
	
	private int exitCode;

	/**
	 * @param binary
	 * @param exitCode
	 */
	public TaskResult(String binary, int exitCode) {
		super();
		this.binary = binary;
		this.exitCode = exitCode;
	}

	/**
	 * @return the binary
	 */
	public String getBinary() {
		return binary;
	}

	/**
	 * @param binary the binary to set
	 */
	public void setBinary(String binary) {
		this.binary = binary;
	}

	/**
	 * @return the exitCode
	 */
	public int getExitCode() {
		return exitCode;
	}

	/**
	 * @param exitCode the exitCode to set
	 */
	public void setExitCode(int exitCode) {
		this.exitCode = exitCode;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "TaskResult [binary=" + binary + ", exitCode=" + exitCode + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((binary == null) ? 0 : binary.hashCode());
		result = prime * result + exitCode;
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
		TaskResult other = (TaskResult) obj;
		if (binary == null) {
			if (other.binary != null)
				return false;
		} else if (!binary.equals(other.binary))
			return false;
		if (exitCode != other.exitCode)
			return false;
		return true;
	}

}

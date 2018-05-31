package fr.viveris.s1pdgs.jobgenerator.model;

import java.util.Objects;

/**
 * Object describing the progress of the job generation
 * @author Cyrielle Gailliard
 *
 */
public class JobGenerationStatus {
	
	/**
	 * Last generation status
	 */
	private GenerationStatusEnum status;
	
	/**
	 * Last update date of the status
	 */
	private long lastModifiedTime;
	
	/**
	 * Number of time the status has been updated with the same value
	 */
	private int nbRetries;

	/**
	 * Default constructor
	 */
	public JobGenerationStatus() {
		status = GenerationStatusEnum.NOT_READY;
		lastModifiedTime = 0;
		nbRetries = 0;
	}

	/**
	 * @return the status
	 */
	public GenerationStatusEnum getStatus() {
		return status;
	}
	
	/**
	 * If same status, consider it is an error
	 * @param status
	 */
	public void updateStatus(final GenerationStatusEnum status) {
		if (this.status == status) {
			lastModifiedTime = System.currentTimeMillis();
			nbRetries ++;
		} else {
			this.status = status;
			nbRetries = 0;
		}
	}

	/**
	 * @return the lastModifiedTime
	 */
	public long getLastModifiedTime() {
		return lastModifiedTime;
	}

	/**
	 * @return the nbRetries
	 */
	public int getNbRetries() {
		return nbRetries;
	}

	/**
	 * to string
	 */
	@Override
	public String toString() {
		return String.format("{status: %s, lastModifiedTime: %s, nbRetries: %s}", status, lastModifiedTime, nbRetries);
	}

	/**
	 * hashcode
	 */
	@Override
	public int hashCode() {
		return Objects.hash(status, lastModifiedTime, nbRetries);
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
			JobGenerationStatus other = (JobGenerationStatus) obj;
			ret = Objects.equals(status, other.status)
					&& lastModifiedTime == other.lastModifiedTime
					&& nbRetries == other.nbRetries;
		}
		return ret;
	}
}

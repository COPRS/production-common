package fr.viveris.s1pdgs.jobgenerator.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import fr.viveris.s1pdgs.jobgenerator.model.joborder.JobOrder;
import fr.viveris.s1pdgs.jobgenerator.model.metadata.SearchMetadataResult;
import fr.viveris.s1pdgs.jobgenerator.model.product.AbstractProduct;

/**
 * Describe a job. Used during generation to keep a status of each in progress
 * job.<br/>
 * This class is generic and depends on the processing level.
 * <li>L0: T is EdrsSession object</li>
 * <li>L1: T is L0Slice object</li>
 * 
 * @author Cyrielle Gailliard
 *
 * @param <T>
 */
public class Job<T> {

	/**
	 * Name of the task table used for this generation
	 */
	private String taskTableName;

	/**
	 * Input product
	 */
	private AbstractProduct<T> product;

	/**
	 * Job order to send to the wrapper (object used to map a job order in the XML
	 * file)
	 */
	private JobOrder jobOrder;

	/**
	 * Distinct metadata queries needed for searching inputs of the task table
	 */
	private Map<Integer, SearchMetadataResult> metadataQueries;

	/**
	 * Job generation status
	 */
	private final JobGenerationStatus status;

	/**
	 * Working directory used by the wrapper
	 */
	private String workDirectory;

	/**
	 * Used increment value (in the working directory path name)
	 */
	private int workDirectoryInc;

	/**
	 * Input topic name
	 */
	private final ResumeDetails resumeDetails;

	/**
	 * Constructor from product
	 * 
	 * @param identifier
	 * @param startTime
	 * @param stopTime
	 * @param product
	 */
	public Job(final AbstractProduct<T> product, final ResumeDetails resumeDetails) {
		this.metadataQueries = new HashMap<>();
		this.status = new JobGenerationStatus();
		this.product = product;
		this.resumeDetails = resumeDetails;
	}

	/**
	 * @return the taskTableName
	 */
	public String getTaskTableName() {
		return taskTableName;
	}

	/**
	 * @param taskTableName
	 *            the taskTableName to set
	 */
	public void setTaskTableName(final String taskTableName) {
		this.taskTableName = taskTableName;
	}

	/**
	 * @return the session
	 */
	public AbstractProduct<T> getProduct() {
		return product;
	}

	/**
	 * @param session
	 *            the session to set
	 */
	public void setProduct(final AbstractProduct<T> product) {
		this.product = product;
	}

	/**
	 * @return the jobOrder
	 */
	public JobOrder getJobOrder() {
		return jobOrder;
	}

	/**
	 * @param jobOrder
	 *            the jobOrder to set
	 */
	public void setJobOrder(final JobOrder jobOrder) {
		this.jobOrder = jobOrder;
	}

	/**
	 * @return the metadataQueries
	 */
	public Map<Integer, SearchMetadataResult> getMetadataQueries() {
		return metadataQueries;
	}

	/**
	 * @param metadataQueries
	 *            the metadataQueries to set
	 */
	public void setMetadataQueries(final Map<Integer, SearchMetadataResult> metadataQueries) {
		this.metadataQueries = metadataQueries;
	}

	/**
	 * @return the status
	 */
	public JobGenerationStatus getStatus() {
		return status;
	}

	/**
	 * @return the workDirectory
	 */
	public String getWorkDirectory() {
		return workDirectory;
	}

	/**
	 * @param workDirectory
	 *            the workDirectory to set
	 */
	public void setWorkDirectory(final String workDirectory) {
		this.workDirectory = workDirectory;
	}

	/**
	 * @return the workDirectoryInc
	 */
	public int getWorkDirectoryInc() {
		return workDirectoryInc;
	}

	/**
	 * @param workDirectoryInc
	 *            the workDirectoryInc to set
	 */
	public void setWorkDirectoryInc(final int workDirectoryInc) {
		this.workDirectoryInc = workDirectoryInc;
	}

	/**
	 * @return the resumeDetails
	 */
	public ResumeDetails getResumeDetails() {
		return resumeDetails;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format(
				"{taskTableName: %s, product: %s, jobOrder: %s, metadataQueries: %s, status: %s, workDirectory: %s, workDirectoryInc: %s, resumeDetails: %s}",
				taskTableName, product, jobOrder, metadataQueries, status, workDirectory, workDirectoryInc,
				resumeDetails);
	}

	/**
	 * hashcode
	 */
	@Override
	public int hashCode() {
		return Objects.hash(taskTableName, product, jobOrder, metadataQueries, status, workDirectory, workDirectoryInc,
				resumeDetails);
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
			Job<?> other = (Job<?>) obj;
			ret = Objects.equals(taskTableName, other.taskTableName) && Objects.equals(product, other.product)
					&& Objects.equals(jobOrder, other.jobOrder)
					&& Objects.equals(metadataQueries, other.metadataQueries) && Objects.equals(status, other.status)
					&& Objects.equals(workDirectory, other.workDirectory) && workDirectoryInc == other.workDirectoryInc
					&& Objects.equals(resumeDetails, other.resumeDetails);
		}
		return ret;
	}
}

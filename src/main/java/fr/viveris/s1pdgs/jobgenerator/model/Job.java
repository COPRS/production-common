package fr.viveris.s1pdgs.jobgenerator.model;

import java.util.HashMap;
import java.util.Map;

import fr.viveris.s1pdgs.jobgenerator.model.joborder.JobOrder;
import fr.viveris.s1pdgs.jobgenerator.model.metadata.SearchMetadataResult;
import fr.viveris.s1pdgs.jobgenerator.model.product.AbstractProduct;

public class Job<T> {
	
	private String taskTableName;
	
	private AbstractProduct<T> product;
	
	private JobOrder jobOrder;
	
	private Map<Integer, SearchMetadataResult> metadataQueries;
	
	private JobGenerationStatus status;
	
	private String workDirectory;
	
	private int workDirectoryInc;

	public Job() {
		this.metadataQueries = new HashMap<>();
		this.status = new JobGenerationStatus();
	}

	/**
	 * @param identifier
	 * @param startTime
	 * @param stopTime
	 * @param product
	 */
	public Job(AbstractProduct<T> product) {
		this();
		this.product = product; 
	}

	/**
	 * @return the taskTableName
	 */
	public String getTaskTableName() {
		return taskTableName;
	}

	/**
	 * @param taskTableName the taskTableName to set
	 */
	public void setTaskTableName(String taskTableName) {
		this.taskTableName = taskTableName;
	}

	/**
	 * @return the session
	 */
	public AbstractProduct<T> getProduct() {
		return product;
	}

	/**
	 * @param session the session to set
	 */
	public void setProduct(AbstractProduct<T> product) {
		this.product = product;
	}

	/**
	 * @return the jobOrder
	 */
	public JobOrder getJobOrder() {
		return jobOrder;
	}

	/**
	 * @param jobOrder the jobOrder to set
	 */
	public void setJobOrder(JobOrder jobOrder) {
		this.jobOrder = jobOrder;
	}

	/**
	 * @return the metadataQueries
	 */
	public Map<Integer, SearchMetadataResult> getMetadataQueries() {
		return metadataQueries;
	}

	/**
	 * @param metadataQueries the metadataQueries to set
	 */
	public void setMetadataQueries(Map<Integer, SearchMetadataResult> metadataQueries) {
		this.metadataQueries = metadataQueries;
	}

	/**
	 * @param metadataQueries the metadataQueries to set
	 */
	public void addMetadataQuery(SearchMetadataResult metadataQuery) {
		this.metadataQueries.put(metadataQuery.getQuery().getIdentifier(), metadataQuery);
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
	 * @param workDirectory the workDirectory to set
	 */
	public void setWorkDirectory(String workDirectory) {
		this.workDirectory = workDirectory;
	}

	/**
	 * @return the workDirectoryInc
	 */
	public int getWorkDirectoryInc() {
		return workDirectoryInc;
	}

	/**
	 * @param workDirectoryInc the workDirectoryInc to set
	 */
	public void setWorkDirectoryInc(int workDirectoryInc) {
		this.workDirectoryInc = workDirectoryInc;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Job [taskTableName=" + taskTableName + ", product=" + product + ", jobOrder=" + jobOrder
				+ ", metadataQueries=" + metadataQueries + ", status=" + status + ", workDirectory=" + workDirectory
				+ "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((jobOrder == null) ? 0 : jobOrder.hashCode());
		result = prime * result + ((metadataQueries == null) ? 0 : metadataQueries.hashCode());
		result = prime * result + ((product == null) ? 0 : product.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result + ((taskTableName == null) ? 0 : taskTableName.hashCode());
		result = prime * result + ((workDirectory == null) ? 0 : workDirectory.hashCode());
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
		Job<?> other = (Job<?>) obj;
		if (jobOrder == null) {
			if (other.jobOrder != null)
				return false;
		} else if (!jobOrder.equals(other.jobOrder))
			return false;
		if (metadataQueries == null) {
			if (other.metadataQueries != null)
				return false;
		} else if (!metadataQueries.equals(other.metadataQueries))
			return false;
		if (product == null) {
			if (other.product != null)
				return false;
		} else if (!product.equals(other.product))
			return false;
		if (status == null) {
			if (other.status != null)
				return false;
		} else if (!status.equals(other.status))
			return false;
		if (taskTableName == null) {
			if (other.taskTableName != null)
				return false;
		} else if (!taskTableName.equals(other.taskTableName))
			return false;
		if (workDirectory == null) {
			if (other.workDirectory != null)
				return false;
		} else if (!workDirectory.equals(other.workDirectory))
			return false;
		return true;
	}
	
}

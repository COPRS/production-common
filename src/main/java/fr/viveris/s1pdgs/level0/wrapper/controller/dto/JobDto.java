package fr.viveris.s1pdgs.level0.wrapper.controller.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Exchanged object in the topic t-pdgs-l0-jobs.</br>
 * A job will contains everything the wrapper needs to launch a job.
 * 
 * @author Cyrielle Gailliard
 *
 */
public class JobDto {

	/**
	 * Session identifier
	 */
	private String productIdentifier;

	/**
	 * Local work directory for launching the job
	 */
	private String workDirectory;

	/**
	 * Local work directory for launching the job
	 */
	private String jobOrder;

	/**
	 * List of inputs needed to execute the job.<br/>
	 * They contain the absolute name on the target host and where we can find the
	 * file according the input family
	 */
	private List<JobInputDto> inputs;

	/**
	 * List information needed to validate the outputs of the job and share them
	 */
	private List<JobOutputDto> outputs;

	/**
	 * List the tasks to be executed for processing the job grouped by pools.<br/>
	 * The pools shall be executed one after one if the previous execution is ok
	 */
	private List<JobPoolDto> pools;

	/**
	 * Default constructor
	 */
	public JobDto() {
		this.inputs = new ArrayList<>();
		this.outputs = new ArrayList<>();
		this.pools = new ArrayList<>();
	}

	/**
	 * Constructor from session identifier
	 */
	public JobDto(String productIdentifier, String workDirectory, String jobOrder) {
		this();
		this.productIdentifier = productIdentifier;
		this.workDirectory = workDirectory;
		this.jobOrder = jobOrder;
	}

	/**
	 * @return the productIdentifier
	 */
	public String getProductIdentifier() {
		return productIdentifier;
	}

	/**
	 * @param productIdentifier the productIdentifier to set
	 */
	public void setProductIdentifier(String productIdentifier) {
		this.productIdentifier = productIdentifier;
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
	public void setWorkDirectory(String workDirectory) {
		this.workDirectory = workDirectory;
	}

	/**
	 * @return the jobOrder
	 */
	public String getJobOrder() {
		return jobOrder;
	}

	/**
	 * @param jobOrder the jobOrder to set
	 */
	public void setJobOrder(String jobOrder) {
		this.jobOrder = jobOrder;
	}

	/**
	 * @return the inputs
	 */
	public List<JobInputDto> getInputs() {
		return inputs;
	}

	/**
	 * @param inputs
	 *            the inputs to set
	 */
	public void setInputs(List<JobInputDto> inputs) {
		this.inputs = inputs;
	}

	/**
	 * @param inputs
	 *            the inputs to set
	 */
	public void addInputs(List<JobInputDto> inputs) {
		this.inputs.addAll(inputs);
	}

	/**
	 * @param inputs
	 *            the inputs to set
	 */
	public void addInput(JobInputDto input) {
		this.inputs.add(input);
	}

	/**
	 * @return the outputs
	 */
	public List<JobOutputDto> getOutputs() {
		return outputs;
	}

	/**
	 * @param outputs
	 *            the outputs to set
	 */
	public void setOutputs(List<JobOutputDto> outputs) {
		this.outputs = outputs;
	}

	/**
	 * @param outputs
	 *            the outputs to set
	 */
	public void addOutputs(List<JobOutputDto> outputs) {
		this.outputs.addAll(outputs);
	}

	/**
	 * @param outputs
	 *            the outputs to set
	 */
	public void addOutput(JobOutputDto output) {
		this.outputs.add(output);
	}

	/**
	 * @return the pools
	 */
	public List<JobPoolDto> getPools() {
		return pools;
	}

	/**
	 * @param pools
	 *            the pools to set
	 */
	public void setPools(List<JobPoolDto> pools) {
		this.pools = pools;
	}

	/**
	 * @param pools
	 *            the pools to set
	 */
	public void addPools(List<JobPoolDto> pools) {
		this.pools.addAll(pools);
	}

	/**
	 * @param pools
	 *            the pools to set
	 */
	public void addPool(JobPoolDto pool) {
		this.pools.add(pool);
	}
	

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "{productIdentifier: " + productIdentifier + ", workDirectory: " + workDirectory + ", jobOrder: "
				+ jobOrder + ", inputs: " + inputs + ", outputs: " + outputs + ", pools: " + pools + "}";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((inputs == null) ? 0 : inputs.hashCode());
		result = prime * result + ((outputs == null) ? 0 : outputs.hashCode());
		result = prime * result + ((pools == null) ? 0 : pools.hashCode());
		result = prime * result + ((productIdentifier == null) ? 0 : productIdentifier.hashCode());
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
		JobDto other = (JobDto) obj;
		if (inputs == null) {
			if (other.inputs != null)
				return false;
		} else if (!inputs.equals(other.inputs))
			return false;
		if (outputs == null) {
			if (other.outputs != null)
				return false;
		} else if (!outputs.equals(other.outputs))
			return false;
		if (pools == null) {
			if (other.pools != null)
				return false;
		} else if (!pools.equals(other.pools))
			return false;
		if (productIdentifier == null) {
			if (other.productIdentifier != null)
				return false;
		} else if (!productIdentifier.equals(other.productIdentifier))
			return false;
		if (workDirectory == null) {
			if (other.workDirectory != null)
				return false;
		} else if (!workDirectory.equals(other.workDirectory))
			return false;
		return true;
	}
	
}

package fr.viveris.s1pdgs.jobgenerator.controller.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import esa.s1pdgs.cpoc.common.ProductFamily;

/**
 * Exchanged object in the topic t-pdgs-l0-jobs.</br>
 * A job will contains everything the wrapper needs to launch a job.
 * 
 * @author Cyrielle Gailliard
 *
 */
public class JobDto {
	
	/**
	 * Family
	 */
	private ProductFamily family;

	/**
	 * Session identifier
	 */
	private String productIdentifier;

	/**
	 * Local work directory for launching the job
	 */
	private String workDirectory;

	/**
	 * Local path to the job order
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
		this.family = ProductFamily.L0_JOB;
	}

	/**
	 * Constructor from session identifier
	 */
	public JobDto(final String productIdentifier, final String workDirectory, final String jobOrder) {
		this();
		this.productIdentifier = productIdentifier;
		this.workDirectory = workDirectory;
		this.jobOrder = jobOrder;
		this.family = ProductFamily.L0_JOB;
	}

	/**
	 * @return the productIdentifier
	 */
	public String getProductIdentifier() {
		return productIdentifier;
	}

	/**
	 * @return the productIdentifier
	 */
	public ProductFamily getProductFamily() {
		return family;
	}

	
	/**
	 * @param productIdentifier
	 *            the productIdentifier to set
	 */
	public void setProductIdentifier(final String productIdentifier) {
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
	public void setWorkDirectory(final String workDirectory) {
		this.workDirectory = workDirectory;
	}

	/**
	 * @return the jobOrder
	 */
	public String getJobOrder() {
		return jobOrder;
	}

	/**
	 * @param jobOrder
	 *            the jobOrder to set
	 */
	public void setJobOrder(final String jobOrder) {
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
	public void setInputs(final List<JobInputDto> inputs) {
		this.inputs = inputs;
	}

	/**
	 * @param inputs
	 *            the inputs to set
	 */
	public void addInputs(final List<JobInputDto> inputs) {
		this.inputs.addAll(inputs);
	}

	/**
	 * @param inputs
	 *            the inputs to set
	 */
	public void addInput(final JobInputDto input) {
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
	public void setOutputs(final List<JobOutputDto> outputs) {
		this.outputs = outputs;
	}

	/**
	 * @param outputs
	 *            the outputs to set
	 */
	public void addOutputs(final List<JobOutputDto> outputs) {
		this.outputs.addAll(outputs);
	}

	/**
	 * @param outputs
	 *            the outputs to set
	 */
	public void addOutput(final JobOutputDto output) {
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
	public void setPools(final List<JobPoolDto> pools) {
		this.pools = pools;
	}

	/**
	 * @param pools
	 *            the pools to set
	 */
	public void addPool(final JobPoolDto pool) {
		this.pools.add(pool);
	}

	/**
	 * to string
	 */
	@Override
	public String toString() {
		return String.format(
				"{productIdentifier: %s, workDirectory: %s, jobOrder: %s, inputs: %s, outputs: %s, pools: %s}",
				productIdentifier, workDirectory, jobOrder, inputs, outputs, pools);
	}

	/**
	 * hash code
	 */
	@Override
	public int hashCode() {
		return Objects.hash(family, productIdentifier, workDirectory, jobOrder, inputs, outputs, pools);
	}

	/**
	 * equals
	 */
	@Override
	public boolean equals(final Object obj) {
		boolean ret;
		if (this == obj) {
			ret = true;
		} else if (obj == null || getClass() != obj.getClass()) {
			ret = false;
		} else {
			JobDto other = (JobDto) obj;
			ret = Objects.equals(family, other.family) 
					&& Objects.equals(productIdentifier, other.productIdentifier)
					&& Objects.equals(workDirectory, other.workDirectory) && Objects.equals(jobOrder, other.jobOrder)
					&& Objects.equals(inputs, other.inputs) && Objects.equals(outputs, other.outputs)
					&& Objects.equals(pools, other.pools);
		}
		return ret;

	}

}

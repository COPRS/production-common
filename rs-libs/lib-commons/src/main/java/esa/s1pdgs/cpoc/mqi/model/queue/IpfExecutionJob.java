package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mqi.model.control.AllowedAction;

/**
 * Exchanged object for the product category LevelJobs.
 * 
 * @author Viveris Technologies
 */
public class IpfExecutionJob extends AbstractMessage {
	/**
	 * Process mode
	 */
	private String productProcessMode;

	/**
	 * Local work directory for launching the job
	 */
	private String workDirectory;

	/**
	 * Local path to the job order
	 */
	private String jobOrder;


	private String etadMode = "";

	/**
	 * List of inputs needed to execute the job.<br/>
	 * They contain the absolute name on the target host and where we can find the
	 * file according the input family
	 */
	private List<LevelJobInputDto> inputs = new ArrayList<>();

	/**
	 * List information needed to validate the outputs of the job and share them
	 */
	private List<LevelJobOutputDto> outputs = new ArrayList<>();

	/**
	 * List the tasks to be executed for processing the job grouped by pools.<br/>
	 * The pools shall be executed one after one if the previous execution is ok
	 */
	private List<LevelJobPoolDto> pools = new ArrayList<>();

	private IpfPreparationJob preparationJob;

	private boolean timedOut = false;

	/**
	 * Default constructor
	 */
	public IpfExecutionJob() {
		super();
		setAllowedActions(Arrays.asList(AllowedAction.RESTART, AllowedAction.RESUBMIT));
	}

	/**
	 * Constructor from session identifier
	 */
	public IpfExecutionJob(final ProductFamily productFamily, final String keyObjectStorage,
			final String productProcessMode, final String workDirectory, final String jobOrder, final String timeliness,
			final UUID reportingTaskUID) {
		super(productFamily, keyObjectStorage);
		this.productProcessMode = productProcessMode;
		this.workDirectory = workDirectory;
		this.jobOrder = jobOrder;
		this.uid = reportingTaskUID;
		this.timeliness = timeliness;
		setAllowedActions(Arrays.asList(AllowedAction.RESTART, AllowedAction.RESUBMIT));
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
	 * @param jobOrder the jobOrder to set
	 */
	public void setJobOrder(final String jobOrder) {
		this.jobOrder = jobOrder;
	}

	/**
	 * @return the productProcessMode
	 */
	public String getProductProcessMode() {
		return productProcessMode;
	}

	/**
	 * @param productProcessMode the productProcessMode to set
	 */
	public void setProductProcessMode(final String productProcessMode) {
		this.productProcessMode = productProcessMode;
	}

	/**
	 * @return the inputs
	 */
	public List<LevelJobInputDto> getInputs() {
		return inputs;
	}

	/**
	 * @param inputs the inputs to set
	 */
	public void setInputs(final List<LevelJobInputDto> inputs) {
		this.inputs = inputs;
	}

	/**
	 * @param input the input to set
	 */
	public void addInput(final LevelJobInputDto input) {
		this.inputs.add(input);
	}

	/**
	 * @param inputs the inputs to set
	 */
	public void addInputs(final List<LevelJobInputDto> inputs) {
		this.inputs.addAll(inputs);
	}

	/**
	 * @return the outputs
	 */
	public List<LevelJobOutputDto> getOutputs() {
		return outputs;
	}

	/**
	 * @param outputs the outputs to set
	 */
	public void setOutputs(final List<LevelJobOutputDto> outputs) {
		this.outputs = outputs;
	}

	/**
	 * @param output the output to set
	 */
	public void addOutput(final LevelJobOutputDto output) {
		this.outputs.add(output);
	}

	/**
	 * @param outputs the outputs to set
	 */
	public void addOutputs(final List<LevelJobOutputDto> outputs) {
		this.outputs.addAll(outputs);
	}

	/**
	 * @return the pools
	 */
	public List<LevelJobPoolDto> getPools() {
		return pools;
	}

	/**
	 * @param pools the pools to set
	 */
	public void setPools(final List<LevelJobPoolDto> pools) {
		this.pools = pools;
	}

	/**
	 * @param pools the pools to set
	 */
	public void addPool(final LevelJobPoolDto pool) {
		this.pools.add(pool);
	}

	public IpfPreparationJob getPreparationJob() {
		return preparationJob;
	}

	public void setPreparationJob(final IpfPreparationJob preparationJob) {
		this.preparationJob = preparationJob;
	}

	public boolean isTimedOut() {
		return timedOut;
	}

	public void setTimedOut(final boolean timedOut) {
		this.timedOut = timedOut;
	}

	public String getEtadMode() {
		return etadMode;
	}

	public void setEtadMode(final String etadMode) {
		this.etadMode = etadMode;
	}

	@Override
	public String toString() {
		return "IpfExecutionJob [productProcessMode=" + productProcessMode + ", workDirectory=" + workDirectory
				+ ", jobOrder=" + jobOrder + ", timeliness=" + timeliness + ", etadMode=" + etadMode + ", inputs="
				+ inputs + ", outputs=" + outputs + ", pools=" + pools + ", preparationJob="
				+ preparationJob + ", timedOut=" + timedOut + "productFamily=" + productFamily
				+ ", keyObjectStorage=" + keyObjectStorage + ", storagePath=" + storagePath + ", uid=" + uid
				+ ", creationDate=" + creationDate + ", podName=" + podName + ", allowedActions=" + allowedActions
				+ ", demandType=" + demandType + ", retryCounter=" + retryCounter + ", debug=" + debug + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(etadMode, inputs, preparationJob, jobOrder, outputs, pools,
				productProcessMode, timedOut, timeliness, workDirectory);
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		final IpfExecutionJob other = (IpfExecutionJob) obj;
		return Objects.equals(etadMode, other.etadMode) && Objects.equals(inputs, other.inputs)
				&& Objects.equals(preparationJob, other.preparationJob)
				&& Objects.equals(jobOrder, other.jobOrder) && Objects.equals(outputs, other.outputs)
				&& Objects.equals(pools, other.pools) && Objects.equals(productProcessMode, other.productProcessMode)
				&& timedOut == other.timedOut && Objects.equals(timeliness, other.timeliness)
				&& Objects.equals(workDirectory, other.workDirectory);
	}

}

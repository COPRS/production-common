package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mqi.model.control.AllowedAction;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

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

    /**	
     * Timeliness
     */
    private String timeliness;

    /**
     * List of inputs needed to execute the job.<br/>
     * They contain the absolute name on the target host and where we can find
     * the file according the input family
     */
    private List<LevelJobInputDto> inputs= new ArrayList<>();

    /**
     * List information needed to validate the outputs of the job and share them
     */
    private List<LevelJobOutputDto> outputs= new ArrayList<>();

    /**
     * List the tasks to be executed for processing the job grouped by
     * pools.<br/>
     * The pools shall be executed one after one if the previous execution is ok
     */
    private List<LevelJobPoolDto> pools= new ArrayList<>();
    
    
    private GenericMessageDto<IpfPreparationJob> ipfPreparationJobMessage;
    
    private boolean timedOut = false;

    /**
     * Default constructor
     */
    public IpfExecutionJob() {
    	super();
    	setAllowedActions(Arrays.asList(AllowedAction.RESTART, AllowedAction.REEVALUATE));
    }

    /**
     * Constructor from session identifier
     */
    public IpfExecutionJob(final ProductFamily productFamily,
            final String keyObjectStorage, final String productProcessMode, final String workDirectory,
            final String jobOrder, final String timeliness, final UUID reportingTaskUID) {
        super(productFamily, keyObjectStorage);
        this.productProcessMode = productProcessMode;
        this.workDirectory = workDirectory;
        this.jobOrder = jobOrder;
        this.uid = reportingTaskUID;
        this.timeliness = timeliness;
        setAllowedActions(Arrays.asList(AllowedAction.RESTART, AllowedAction.REEVALUATE));
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
	 * @return the timeliness
	 */
	public String getTimeliness() {
		return timeliness;
	}

	/**
	 * @param timeliness the timeliness to set
	 */
	public void setTimeliness(final String timeliness) {
		this.timeliness = timeliness;
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
     * @param inputs
     *            the inputs to set
     */
    public void setInputs(final List<LevelJobInputDto> inputs) {
        this.inputs = inputs;
    }

    /**
     * @param input
     *            the input to set
     */
    public void addInput(final LevelJobInputDto input) {
        this.inputs.add(input);
    }

    /**
     * @param inputs
     *            the inputs to set
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
     * @param outputs
     *            the outputs to set
     */
    public void setOutputs(final List<LevelJobOutputDto> outputs) {
        this.outputs = outputs;
    }

    /**
     * @param output
     *            the output to set
     */
    public void addOutput(final LevelJobOutputDto output) {
        this.outputs.add(output);
    }

    /**
     * @param outputs
     *            the outputs to set
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
     * @param pools
     *            the pools to set
     */
    public void setPools(final List<LevelJobPoolDto> pools) {
        this.pools = pools;
    }

    /**
     * @param pools
     *            the pools to set
     */
    public void addPool(final LevelJobPoolDto pool) {
        this.pools.add(pool);
    }
    
	public GenericMessageDto<IpfPreparationJob> getIpfPreparationJobMessage() {
		return ipfPreparationJobMessage;
	}

	public void setIpfPreparationJobMessage(final GenericMessageDto<IpfPreparationJob> ipfPreparationJobMessage) {
		this.ipfPreparationJobMessage = ipfPreparationJobMessage;
	}

	public boolean isTimedOut() {
		return timedOut;
	}

	public void setTimedOut(final boolean timedOut) {
		this.timedOut = timedOut;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((inputs == null) ? 0 : inputs.hashCode());
		result = prime * result + ((ipfPreparationJobMessage == null) ? 0 : ipfPreparationJobMessage.hashCode());
		result = prime * result + ((jobOrder == null) ? 0 : jobOrder.hashCode());
		result = prime * result + ((outputs == null) ? 0 : outputs.hashCode());
		result = prime * result + ((pools == null) ? 0 : pools.hashCode());
		result = prime * result + ((productProcessMode == null) ? 0 : productProcessMode.hashCode());
		result = prime * result + (timedOut ? 1231 : 1237);
		result = prime * result + ((timeliness == null) ? 0 : timeliness.hashCode());
		result = prime * result + ((workDirectory == null) ? 0 : workDirectory.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		IpfExecutionJob other = (IpfExecutionJob) obj;
		if (inputs == null) {
			if (other.inputs != null)
				return false;
		} else if (!inputs.equals(other.inputs))
			return false;
		if (ipfPreparationJobMessage == null) {
			if (other.ipfPreparationJobMessage != null)
				return false;
		} else if (!ipfPreparationJobMessage.equals(other.ipfPreparationJobMessage))
			return false;
		if (jobOrder == null) {
			if (other.jobOrder != null)
				return false;
		} else if (!jobOrder.equals(other.jobOrder))
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
		if (productProcessMode == null) {
			if (other.productProcessMode != null)
				return false;
		} else if (!productProcessMode.equals(other.productProcessMode))
			return false;
		if (timedOut != other.timedOut)
			return false;
		if (timeliness == null) {
			if (other.timeliness != null)
				return false;
		} else if (!timeliness.equals(other.timeliness))
			return false;
		if (workDirectory == null) {
			if (other.workDirectory != null)
				return false;
		} else if (!workDirectory.equals(other.workDirectory))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "IpfExecutionJob [productFamily=" + productFamily + ", keyObjectStorage=" + keyObjectStorage
				+ ", creationDate=" + creationDate + ", hostname=" + hostname + ", productProcessMode="
				+ productProcessMode + ", workDirectory=" + workDirectory + ", jobOrder=" + jobOrder +
				", timeliness=" + timeliness + ", inputs=" + inputs + ", outputs=" + outputs +
				", pools=" + pools + ", timedOut=" + timedOut + ", uid=" + uid +
				", ipfPreparationJobMessage=" + ipfPreparationJobMessage + "]";
	}

}

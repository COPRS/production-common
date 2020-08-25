package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mqi.model.control.ControlAction;
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

    /**
     * Default constructor
     */
    public IpfExecutionJob() {
    	super();
    	setAllowedControlActions(Arrays.asList(ControlAction.RESTART));
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
        setAllowedControlActions(Arrays.asList(ControlAction.RESTART));
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

	public void setIpfPreparationJobMessage(GenericMessageDto<IpfPreparationJob> ipfPreparationJobMessage) {
		this.ipfPreparationJobMessage = ipfPreparationJobMessage;
	}

	@Override
	public int hashCode() {
		return Objects.hash(creationDate, hostname, inputs, jobOrder, timeliness, keyObjectStorage, outputs, pools,
				productFamily, productProcessMode, workDirectory,  uid, ipfPreparationJobMessage,
				allowedControlActions, controlDemandType, controlDebug, controlRetryCounter);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final IpfExecutionJob other = (IpfExecutionJob) obj;
		return Objects.equals(creationDate, other.creationDate) 
				&& Objects.equals(hostname, other.hostname)
				&& Objects.equals(inputs, other.inputs) 
				&& Objects.equals(jobOrder, other.jobOrder)
				&& Objects.equals(timeliness, other.timeliness)
				&& Objects.equals(keyObjectStorage, other.keyObjectStorage) 
				&& Objects.equals(outputs, other.outputs)
				&& Objects.equals(pools, other.pools) 
				&& productFamily == other.productFamily
				&& Objects.equals(productProcessMode, other.productProcessMode)
				&& Objects.equals(workDirectory, other.workDirectory)
				&& Objects.equals(uid, other.uid)
				&& Objects.equals(ipfPreparationJobMessage, other.ipfPreparationJobMessage)
				&& Objects.equals(allowedControlActions, other.getAllowedControlActions())
		        && controlDemandType == other.controlDemandType
		        && controlDebug == other.controlDebug
		        && controlRetryCounter == other.controlRetryCounter;
	}

	@Override
	public String toString() {
		return "IpfExecutionJob [productFamily=" + productFamily + ", keyObjectStorage=" + keyObjectStorage
				+ ", creationDate=" + creationDate + ", hostname=" + hostname + ", productProcessMode="
				+ productProcessMode + ", workDirectory=" + workDirectory + ", jobOrder=" + jobOrder +
				", timeliness=" + timeliness + ", inputs=" + inputs + ", outputs=" + outputs +
				", pools=" + pools + ", uid=" + uid + ", ipfPreparationJobMessage=" + ipfPreparationJobMessage + "]";
	}

}

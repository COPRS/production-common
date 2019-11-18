package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import esa.s1pdgs.cpoc.common.ProductFamily;

/**
 * Exchanged object for the product category LevelJobs.
 * 
 * @author Viveris Technologies
 */
public class IpfExecutionJob extends AbstractDto {    
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

    /**
     * Default constructor
     */
    public IpfExecutionJob() {
    	super();
    }

    /**
     * Constructor from session identifier
     */
    public IpfExecutionJob(final ProductFamily family,
            final String productIdentifier, final String productProcessMode, final String workDirectory,
            final String jobOrder) {
        super(productIdentifier, family);
        this.productProcessMode = productProcessMode;
        this.workDirectory = workDirectory;
        this.jobOrder = jobOrder;
    }

    /**
     * @return the productIdentifier
     */
    public String getProductIdentifier() {
        return getProductName();
    }

    /**
     * @param productIdentifier
     *            the productIdentifier to set
     */
    public void setProductIdentifier(final String productIdentifier) {
        this.setProductName(productIdentifier);;
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
     * @return the productProcessMode
     */
    public String getProductProcessMode() {
        return productProcessMode;
    }

    /**
     * @param productProcessMode the productProcessMode to set
     */
    public void setProductProcessMode(String productProcessMode) {
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

    /**
     * to string
     */
    @Override
    public String toString() {
        return String.format(
                "{family: %s, productIdentifier: %s, productProcessMode: %s, workDirectory: %s, jobOrder: %s, inputs: %s, outputs: %s, pools: %s, hostname: %s, creationDate: %s}",
                getFamily(), getProductIdentifier(), productProcessMode, workDirectory, jobOrder, inputs,
                outputs, pools, getHostname(), getCreationDate());
    }

    /**
     * hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(getFamily(), getProductIdentifier(), productProcessMode, workDirectory, jobOrder, 
                inputs, outputs, pools, getHostname(), getCreationDate());
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
            IpfExecutionJob other = (IpfExecutionJob) obj;
            ret = Objects.equals(getFamily(), other.getFamily())
                    && Objects.equals(getProductIdentifier(), other.getProductIdentifier())
                    && Objects.equals(productProcessMode,
                            other.productProcessMode)
                    && Objects.equals(workDirectory, other.workDirectory)
                    && Objects.equals(jobOrder, other.jobOrder)
                    && Objects.equals(inputs, other.inputs)
                    && Objects.equals(outputs, other.outputs)
                    && Objects.equals(pools, other.pools)
                    && Objects.equals(getHostname(), other.getHostname())
            		&& Objects.equals(getCreationDate(), other.getCreationDate())
            		&& Objects.equals(getHostname(), other.getHostname())
                    && Objects.equals(getCreationDate(), other.getCreationDate());
        }
        return ret;

    }

}

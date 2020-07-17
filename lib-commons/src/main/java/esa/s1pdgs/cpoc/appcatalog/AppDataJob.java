package esa.s1pdgs.cpoc.appcatalog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

/**
 * Class used for exchanging applicative data of a job
 * 
 * @author Viveris Technologies
 */
public class AppDataJob {

	// dirty workaround to have the mongodb id mapped
	private long id;

    /**
     * Application Level: L0 or L1
     */
    private ApplicationLevel level;

    /**
     * Name of the pod generating the jobs
     */
    private String pod;

    /**
     * Global state of the job (aggregation of the states of all its job
     * generations)
     */
    private AppDataJobState state;
    
    private String taskTableName;
    
    private String startTime;
    
    private String stopTime;
    
    private String productName;

    /**
     * Date when the job is created
     */
    private Date creationDate;

    /**
     * Date of the last modification done on the job
     */
    private Date lastUpdateDate;

    /**
     * MQI messages linked to this job
     */
    private List<GenericMessageDto<CatalogEvent>> messages;

    /**
     * Product of this job
     */
    private AppDataJobProduct product;

    /**
     * Additional inputs for the job, e.g. aux files
     */
    private List<AppDataJobTaskInputs> additionalInputs;

    /**
     * Generations of the job
     */
    private AppDataJobGeneration generation;
    
    private UUID reportingId;
    
    private long prepJobMessageId;
    
    private String prepJobInputQueue;
    
    public AppDataJob(final long id) {
    	this();
    	this.id = id;
    }
    
    /**
     * 
     */
    public AppDataJob() {
        this.state = AppDataJobState.WAITING;
        this.messages = new ArrayList<>();
        this.generation = new AppDataJobGeneration();
    }
    
    

	public long getId() {
		return id;
	}

	public void setId(final long id) {
		this.id = id;
	}

    /**
     * @return the level
     */
    public ApplicationLevel getLevel() {
        return level;
    }

    /**
     * @param level
     *            the level to set
     */
    public void setLevel(final ApplicationLevel level) {
        this.level = level;
    }

    /**
     * @return the pod
     */
    public String getPod() {
        return pod;
    }

    /**
     * @param pod
     *            the pod to set
     */
    public void setPod(final String pod) {
        this.pod = pod;
    }

    /**
     * @return the state
     */
    public AppDataJobState getState() {
        return state;
    }

    /**
     * @param state
     *            the state to set
     */
    public void setState(final AppDataJobState state) {
        this.state = state;
    }

    /**
     * @return the creationDate
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * @param creationDate
     *            the creationDate to set
     */
    public void setCreationDate(final Date creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * @return the lastUpdateDate
     */
    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    /**
     * @param lastUpdateDate
     *            the lastUpdateDate to set
     */
    public void setLastUpdateDate(final Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    /**
     * @return the messages
     */
    public List<GenericMessageDto<CatalogEvent>> getMessages() {
        return messages;
    }

    /**
     * @param messages
     *            the messages to set
     */
    public void setMessages(final List<GenericMessageDto<CatalogEvent>> messages) {
        this.messages = messages;
    }

    /**
     * @return the product
     */
    public AppDataJobProduct getProduct() {
        return product;
    }

    /**
     * @param product
     *            the product to set
     */
    public void setProduct(final AppDataJobProduct product) {
        this.product = product;
    }

    /**
     *
     * @return additional job inputs
     */
    public List<AppDataJobTaskInputs> getAdditionalInputs() {
        return additionalInputs;
    }

    /**
     *
     * @param additionalInputs additional job inputs
     */
    public void setAdditionalInputs(final List<AppDataJobTaskInputs> additionalInputs) {
        this.additionalInputs = additionalInputs;
    }

    /**
     * @return the generations
     */
    public AppDataJobGeneration getGeneration() {
        return generation;
    }

    /**
     * @param generation
     *            the generation to set
     */
    public void setGeneration(final AppDataJobGeneration generation) {
        this.generation = generation;
    }
    
    public UUID getReportingId() {
		return reportingId;
	}

	public void setReportingId(final UUID reportingId) {
		this.reportingId = reportingId;
	}
	
    public long getPrepJobMessageId() {
		return prepJobMessageId;
	}

	public void setPrepJobMessageId(final long prepJobMessageId) {
		this.prepJobMessageId = prepJobMessageId;
	}

	public String getPrepJobInputQueue() {
		return prepJobInputQueue;
	}

	public void setPrepJobInputQueue(final String prepJobInputQueue) {
		this.prepJobInputQueue = prepJobInputQueue;
	}
	
	public String getTaskTableName() {
		return taskTableName;
	}

	public void setTaskTableName(final String taskTableName) {
		this.taskTableName = taskTableName;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(final String startTime) {
		this.startTime = startTime;
	}

	public String getStopTime() {
		return stopTime;
	}

	public void setStopTime(final String stoptime) {
		this.stopTime = stoptime;
	}
	
	public String getProductName() {
		return productName;
	}

	public void setProductName(final String productName) {
		this.productName = productName;
	}

	/**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, level, pod, state, taskTableName, startTime, stopTime, productName,
                creationDate, lastUpdateDate, messages, product, additionalInputs, generation, reportingId, prepJobInputQueue, prepJobMessageId);
    }
    
    @Override
	public String toString() {
		return "AppDataJob [id=" + id + ", level=" + level + ", pod=" + pod + ", state=" + state
			    + ", taskTableName=" + taskTableName + ", startTime=" + startTime + ", stopTime=" + stopTime
                + ", creationDate=" + creationDate + ", lastUpdateDate=" + lastUpdateDate + ", messages=" + messages
                + ", product=" + product + ", additionalInputs=" + additionalInputs + ", generation=" + generation
                + ", reportingId=" + reportingId + ", prepJobMessageId=" + prepJobMessageId + ", productName=" + productName
                + ", prepJobInputQueue=" + prepJobInputQueue + "]";
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
            final AppDataJob other = (AppDataJob) obj;
            ret =  id == other.id
                    && Objects.equals(level, other.level)
                    && Objects.equals(pod, other.pod)
                    && Objects.equals(state, other.state)                    
                    && Objects.equals(taskTableName, other.taskTableName)
                    && Objects.equals(startTime, other.startTime)
                    && Objects.equals(stopTime, other.stopTime)
                    && Objects.equals(creationDate, other.creationDate)
                    && Objects.equals(lastUpdateDate, other.lastUpdateDate)
                    && Objects.equals(messages, other.messages)
                    && Objects.equals(product, other.product)
                    && Objects.equals(additionalInputs, other.additionalInputs)
                    && Objects.equals(generation, other.generation)
                    && Objects.equals(productName, other.productName)
                    && Objects.equals(prepJobInputQueue, other.prepJobInputQueue)
                    && Objects.equals(prepJobMessageId, other.prepJobMessageId)
                    && Objects.equals(reportingId, other.reportingId);
        }
        return ret;
    }

}

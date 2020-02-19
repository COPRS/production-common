package esa.s1pdgs.cpoc.appcatalog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

/**
 * Class used for exchanging applicative data of a job
 * 
 * @author Viveris Technologies
 */
public class AppDataJob<E extends AbstractMessage> {

	// dirty workaround to have the mongodb id mapped
	private long id;

    /**
     * Product category
     */
    private ProductCategory category;

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
    private List<GenericMessageDto<E>> messages;

    /**
     * Product of this job
     */
    private AppDataJobProduct product;

    /**
     * Generations of the job
     */
    private List<AppDataJobGeneration> generations;
    
    private UUID reportingId;
    
    private long prepJobMessageId;
    
    private String prepJobInputQueue;
    

    /**
     * 
     */
    public AppDataJob() {
        super();
        this.state = AppDataJobState.WAITING;
        this.messages = new ArrayList<>();
        this.generations = new ArrayList<>();
    }

	public long getId() {
		return id;
	}

	public void setId(final long id) {
		this.id = id;
	}

    /**
     * @return the category
     */
    public ProductCategory getCategory() {
        return category;
    }

    /**
     * @param category
     *            the category to set
     */
    public void setCategory(final ProductCategory category) {
        this.category = category;
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
    public List<GenericMessageDto<E>> getMessages() {
        return messages;
    }

    /**
     * @param messages
     *            the messages to set
     */
    public void setMessages(final List<GenericMessageDto<E>> messages) {
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
     * @return the generations
     */
    public List<AppDataJobGeneration> getGenerations() {
        return generations;
    }

    /**
     * @param generations
     *            the generations to set
     */
    public void setGenerations(final List<AppDataJobGeneration> generations) {
        this.generations = generations;
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


	/**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, category, level, pod, state,
                creationDate, lastUpdateDate, messages, product, generations, reportingId, prepJobInputQueue, prepJobMessageId);
    }
    
    @Override
	public String toString() {
		return "AppDataJob [id=" + id + ", category=" + category + ", level=" + level + ", pod=" + pod + ", state="
				+ state + ", creationDate=" + creationDate + ", lastUpdateDate=" + lastUpdateDate + ", messages="
				+ messages + ", product=" + product + ", generations=" + generations + ", reportingId=" + reportingId
				+ ", prepJobMessageId=" + prepJobMessageId + ", prepJobInputQueue=" + prepJobInputQueue + "]";
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
                    && Objects.equals(category, other.category)
                    && Objects.equals(level, other.level)
                    && Objects.equals(pod, other.pod)
                    && Objects.equals(state, other.state)
                    && Objects.equals(creationDate, other.creationDate)
                    && Objects.equals(lastUpdateDate, other.lastUpdateDate)
                    && Objects.equals(messages, other.messages)
                    && Objects.equals(product, other.product)
                    && Objects.equals(generations, other.generations)
                    && Objects.equals(prepJobInputQueue, other.prepJobInputQueue)
                    && Objects.equals(prepJobMessageId, other.prepJobMessageId)
                    && Objects.equals(reportingId, other.reportingId);
        }
        return ret;
    }

}

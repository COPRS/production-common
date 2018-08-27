package esa.s1pdgs.cpoc.appcatalog.server.mqi.db;

import java.util.Date;

import org.springframework.data.annotation.Id;

import esa.s1pdgs.cpoc.appcatalog.rest.MqiStateMessageEnum;
import esa.s1pdgs.cpoc.common.ProductCategory;

/**
 * Object Stored in the database
 * 
 * @author Viveris Technologies
 */
public class MqiMessage {
    
    /**
     * Category of the product
     */
    private ProductCategory category;
    /**
     * Identifier of the message
     */
    @Id
    private long identifier;
    /**
     * Topic name
     */
    private String topic;
    /**
     * Partition number
     */
    private int partition;
    /**
     * Offset of the message
     */
    private long offset;
    /**
     * Group of the message
     */
    private String group;
    /**
     * State of the message
     */
    private MqiStateMessageEnum state;
    /**
     * Pod who read the message
     */
    private String readingPod;
    /**
     * Date where the pod read the message
     */
    private Date lastReadDate;
    /**
     * Pod who send the message
     */
    private String sendingPod;
    /**
     * Date whe the pod last send the message
     */
    private Date lastSendDate;
    /**
     * Date where the message was last acknowledge
     */
    private Date lastAckDate;
    /**
     * Number of retries
     */
    private int nbRetries;
    /**
     * Dto of the message
     */
    private Object dto;
    
    /**
     * Date of the insertion in MongoDB
     */
    private Date creationDate;
    
    /**
     * Default Constructor
     */
    public MqiMessage() {
        
    }
    
    /**
     * @param category
     * @param identifier
     * @param topic
     * @param partition
     * @param offset
     * @param group
     * @param state
     * @param readingPod
     * @param lastReadDate
     * @param sendingPod
     * @param lastSendDate
     * @param lastAckDate
     * @param nbRetries
     * @param dto
     */
    public MqiMessage(ProductCategory category, String topic,
            int partition, long offset, String group, MqiStateMessageEnum state,
            String readingPod, Date lastReadDate, String sendingPod,
            Date lastSendDate, Date lastAckDate, int nbRetries, Object dto,
            Date creationDate) {
        super();
        this.category = category;
        this.topic = topic;
        this.partition = partition;
        this.offset = offset;
        this.group = group;
        this.state = state;
        this.readingPod = readingPod;
        this.lastReadDate = lastReadDate;
        this.sendingPod = sendingPod;
        this.lastSendDate = lastSendDate;
        this.lastAckDate = lastAckDate;
        this.nbRetries = nbRetries;
        this.dto = dto;
        this.setCreationDate(creationDate);
    }
    
    
    /**
     * @return the category
     */
    public ProductCategory getCategory() {
        return category;
    }
    /**
     * @param category the category to set
     */
    public void setCategory(ProductCategory category) {
        this.category = category;
    }
    /**
     * @return the identifier
     */
    public long getIdentifier() {
        return identifier;
    }
    /**
	 * @param identifier the identifier to set
	 */
	public void setIdentifier(long identifier) {
		this.identifier = identifier;
	}

	/**
     * @return the topic
     */
    public String getTopic() {
        return topic;
    }
    /**
     * @param topic the topic to set
     */
    public void setTopic(String topic) {
        this.topic = topic;
    }
    /**
     * @return the partition
     */
    public int getPartition() {
        return partition;
    }
    /**
     * @param partition the partition to set
     */
    public void setPartition(int partition) {
        this.partition = partition;
    }
    /**
     * @return the offset
     */
    public long getOffset() {
        return offset;
    }
    /**
     * @param offset the offset to set
     */
    public void setOffset(long offset) {
        this.offset = offset;
    }
    /**
     * @return the group
     */
    public String getGroup() {
        return group;
    }
    /**
     * @param group the group to set
     */
    public void setGroup(String group) {
        this.group = group;
    }
    /**
     * @return the state
     */
    public MqiStateMessageEnum getState() {
        return state;
    }
    /**
     * @param state the state to set
     */
    public void setState(MqiStateMessageEnum state) {
        this.state = state;
    }
    /**
     * @return the readingPod
     */
    public String getReadingPod() {
        return readingPod;
    }
    /**
     * @param readingPod the readingPod to set
     */
    public void setReadingPod(String readingPod) {
        this.readingPod = readingPod;
    }
    /**
     * @return the lastReadDate
     */
    public Date getLastReadDate() {
        return lastReadDate;
    }
    /**
     * @param lastReadDate the lastReadDate to set
     */
    public void setLastReadDate(Date lastReadDate) {
        this.lastReadDate = lastReadDate;
    }
    /**
     * @return the sendingPod
     */
    public String getSendingPod() {
        return sendingPod;
    }
    /**
     * @param sendingPod the sendingPod to set
     */
    public void setSendingPod(String sendingPod) {
        this.sendingPod = sendingPod;
    }
    /**
     * @return the lastSendDate
     */
    public Date getLastSendDate() {
        return lastSendDate;
    }
    /**
     * @param lastSendDate the lastSendDate to set
     */
    public void setLastSendDate(Date lastSendDate) {
        this.lastSendDate = lastSendDate;
    }
    /**
     * @return the lastAckDate
     */
    public Date getLastAckDate() {
        return lastAckDate;
    }
    /**
     * @param lastAckDate the lastAckDate to set
     */
    public void setLastAckDate(Date lastAckDate) {
        this.lastAckDate = lastAckDate;
    }
    /**
     * @return the nbRetries
     */
    public int getNbRetries() {
        return nbRetries;
    }
    /**
     * @param nbRetries the nbRetries to set
     */
    public void setNbRetries(int nbRetries) {
        this.nbRetries = nbRetries;
    }
    /**
     * @return the dto
     */
    public Object getDto() {
        return dto;
    }
    /**
     * @param dto the dto to set
     */
    public void setDto(Object dto) {
        this.dto = dto;
    }


    /**
     * @return the creationDate
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * @param creationDate the creationDate to set
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "{\"category\":\"" + category + "\", \"identifier\":\"" + identifier
                + "\", \"topic\":\"" + topic + "\", \"partition\":\"" + partition + "\", \"offset\":\""
                + offset + "\", \"group\":\"" + group + "\", \"state\":\"" + state
                + "\", \"readingPod\":\"" + readingPod + "\", \"lastReadDate\":\""
                + lastReadDate + "\", \"sendingPod\":\"" + sendingPod + "\", \"lastSendDate\":\""
                + lastSendDate + "\", \"lastAckDate\":\"" + lastAckDate + "\", \"nbRetries\":\""
                + nbRetries + "\", \"dto\":\"" + dto + "\" , \"creationDate\":\"" + creationDate + "\"}";
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((category == null) ? 0 : category.hashCode());
        result = prime * result
                + ((creationDate == null) ? 0 : creationDate.hashCode());
        result = prime * result + ((dto == null) ? 0 : dto.hashCode());
        result = prime * result + ((group == null) ? 0 : group.hashCode());
        result = prime * result + (int) (identifier ^ (identifier >>> 32));
        result = prime * result
                + ((lastAckDate == null) ? 0 : lastAckDate.hashCode());
        result = prime * result
                + ((lastReadDate == null) ? 0 : lastReadDate.hashCode());
        result = prime * result
                + ((lastSendDate == null) ? 0 : lastSendDate.hashCode());
        result = prime * result + nbRetries;
        result = prime * result + (int) (offset ^ (offset >>> 32));
        result = prime * result + partition;
        result = prime * result
                + ((readingPod == null) ? 0 : readingPod.hashCode());
        result = prime * result
                + ((sendingPod == null) ? 0 : sendingPod.hashCode());
        result = prime * result + ((state == null) ? 0 : state.hashCode());
        result = prime * result + ((topic == null) ? 0 : topic.hashCode());
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
        MqiMessage other = (MqiMessage) obj;
        if (category != other.category)
            return false;
        if (creationDate == null) {
            if (other.creationDate != null)
                return false;
        } else if (!creationDate.equals(other.creationDate))
            return false;
        if (dto == null) {
            if (other.dto != null)
                return false;
        } else if (!dto.equals(other.dto))
            return false;
        if (group == null) {
            if (other.group != null)
                return false;
        } else if (!group.equals(other.group))
            return false;
        if (identifier != other.identifier)
            return false;
        if (lastAckDate == null) {
            if (other.lastAckDate != null)
                return false;
        } else if (!lastAckDate.equals(other.lastAckDate))
            return false;
        if (lastReadDate == null) {
            if (other.lastReadDate != null)
                return false;
        } else if (!lastReadDate.equals(other.lastReadDate))
            return false;
        if (lastSendDate == null) {
            if (other.lastSendDate != null)
                return false;
        } else if (!lastSendDate.equals(other.lastSendDate))
            return false;
        if (nbRetries != other.nbRetries)
            return false;
        if (offset != other.offset)
            return false;
        if (partition != other.partition)
            return false;
        if (readingPod == null) {
            if (other.readingPod != null)
                return false;
        } else if (!readingPod.equals(other.readingPod))
            return false;
        if (sendingPod == null) {
            if (other.sendingPod != null)
                return false;
        } else if (!sendingPod.equals(other.sendingPod))
            return false;
        if (state != other.state)
            return false;
        if (topic == null) {
            if (other.topic != null)
                return false;
        } else if (!topic.equals(other.topic))
            return false;
        return true;
    }


    

}
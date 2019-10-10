package esa.s1pdgs.cpoc.appcatalog.rest;

import java.util.Date;
import java.util.Objects;

import esa.s1pdgs.cpoc.common.MessageState;
import esa.s1pdgs.cpoc.common.ProductCategory;

/**
 * Message object (without the kafka dto) used by the REST applicative catalog
 * 
 * @author Viveris Technologies
 */
public class AppCatMessageDto<T> {

    /**
     * Category
     */
    private ProductCategory category;

    /**
     * Identifier
     */
    private long identifier;

    /**
     * Topic name
     */
    private String topic;

    /**
     * Partition identifier
     */
    private int partition;

    /**
     * Offset in Kafka
     */
    private long offset;

    /**
     * Group name
     */
    private String group;

    /**
     * State of the message
     */
    private MessageState state;

    /**
     * Pod name who is reading the message
     */
    private String readingPod;

    /**
     * Date of the last read
     */
    private Date lastReadDate;

    /**
     * Pod name who is sending the message
     */
    private String sendingPod;

    /**
     * Date of the last send
     */
    private Date lastSendDate;

    /**
     * Date of the last ack
     */
    private Date lastAckDate;

    /**
     * Number of retries
     */
    private int nbRetries;
    
    /**
     * Date of creation
     */
    private Date creationDate;
    
    
    private T dto;
        

    /**
     * Default constructor
     */
    public AppCatMessageDto() {
        super();
        this.nbRetries = 0;
        this.state = MessageState.READ;
    }

    /**
     * @param category
     */
    public AppCatMessageDto(final ProductCategory category) {
        this();
        this.category = category;
    }

    /**
     * @param category
     * @param identifier
     * @param topic
     * @param partition
     * @param offset
     */
    public AppCatMessageDto(final ProductCategory category,
            final long identifier, final String topic, final int partition,
            final long offset) {
        this(category);
        this.identifier = identifier;
        this.topic = topic;
        this.partition = partition;
        this.offset = offset;
    }

    public AppCatMessageDto(ProductCategory category, int identifier, String topic, int partition, int offset, T dto) {
        this(category);
        this.identifier = identifier;
        this.topic = topic;
        this.partition = partition;
        this.offset = offset;
        this.dto = dto;
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
     * @return the identifier
     */
    public long getIdentifier() {
        return identifier;
    }

    /**
     * @param identifier
     *            the identifier to set
     */
    public void setIdentifier(final long identifier) {
        this.identifier = identifier;
    }

    /**
     * @return the topic
     */
    public String getTopic() {
        return topic;
    }

    /**
     * @param topic
     *            the topic to set
     */
    public void setTopic(final String topic) {
        this.topic = topic;
    }

    /**
     * @return the partition
     */
    public int getPartition() {
        return partition;
    }

    /**
     * @param partition
     *            the partition to set
     */
    public void setPartition(final int partition) {
        this.partition = partition;
    }

    /**
     * @return the offset
     */
    public long getOffset() {
        return offset;
    }

    /**
     * @param offset
     *            the offset to set
     */
    public void setOffset(final long offset) {
        this.offset = offset;
    }

    /**
     * @return the group
     */
    public String getGroup() {
        return group;
    }

    /**
     * @param group
     *            the group to set
     */
    public void setGroup(final String group) {
        this.group = group;
    }

    /**
     * @return the state
     */
    public MessageState getState() {
        return state;
    }

    /**
     * @param state
     *            the state to set
     */
    public void setState(final MessageState state) {
        this.state = state;
    }

    /**
     * @return the readingPod
     */
    public String getReadingPod() {
        return readingPod;
    }

    /**
     * @param readingPod
     *            the readingPod to set
     */
    public void setReadingPod(final String readingPod) {
        this.readingPod = readingPod;
    }

    /**
     * @return the lastReadDate
     */
    public Date getLastReadDate() {
        return lastReadDate;
    }

    /**
     * @param lastReadDate
     *            the lastReadDate to set
     */
    public void setLastReadDate(final Date lastReadDate) {
        this.lastReadDate = lastReadDate;
    }

    /**
     * @return the sendingPod
     */
    public String getSendingPod() {
        return sendingPod;
    }

    /**
     * @param sendingPod
     *            the sendingPod to set
     */
    public void setSendingPod(final String sendingPod) {
        this.sendingPod = sendingPod;
    }

    /**
     * @return the lastSendDate
     */
    public Date getLastSendDate() {
        return lastSendDate;
    }

    /**
     * @param lastSendDate
     *            the lastSendDate to set
     */
    public void setLastSendDate(final Date lastSendDate) {
        this.lastSendDate = lastSendDate;
    }

    /**
     * @return the lastAckDate
     */
    public Date getLastAckDate() {
        return lastAckDate;
    }

    /**
     * @param lastAckDate
     *            the lastAckDate to set
     */
    public void setLastAckDate(final Date lastAckDate) {
        this.lastAckDate = lastAckDate;
    }

    /**
     * @return the nbRetries
     */
    public int getNbRetries() {
        return nbRetries;
    }

    /**
     * @param nbRetries
     *            the nbRetries to set
     */
    public void setNbRetries(final int nbRetries) {
        this.nbRetries = nbRetries;
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
    public void setCreationDate(final Date creationDate) {
        this.creationDate = creationDate;
    }
    

    /**
     * @return the dto
     */
    public T getDto() {
        return dto;
    }

    /**
     * @param dto
     *            the dto to set
     */
    public void setDto(final T dto) {
        this.dto = dto;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("{%s}", toStringForExtend());
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toStringForExtend() {
        return String.format(
                "category: %s, identifier: %s, topic: %s, partition: %s, offset: %s, "
                + "group: %s, state: %s, readingPod: %s, lastReadDate: %s, sendingPod: %s, "
                + "lastSendDate: %s, lastAckDate: %s, nbRetries: %s, creationDate: %s, dto: %s",
                category, identifier, topic, partition, offset, group, state,
                readingPod, lastReadDate, sendingPod, lastSendDate, lastAckDate,
                nbRetries, creationDate, dto);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(category, identifier, topic, partition, offset,
                group, state, readingPod, lastReadDate, sendingPod,
                lastSendDate, lastAckDate, nbRetries, creationDate, dto);
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
            AppCatMessageDto<?> other = (AppCatMessageDto<?>) obj;
            ret = Objects.equals(category, other.category)
                    && identifier == other.identifier
                    && Objects.equals(topic, other.topic)
                    && partition == other.partition && offset == other.offset
                    && Objects.equals(group, other.group)
                    && Objects.equals(state, other.state)
                    && Objects.equals(readingPod, other.readingPod)
                    && Objects.equals(lastReadDate, other.lastReadDate)
                    && Objects.equals(sendingPod, other.sendingPod)
                    && Objects.equals(lastSendDate, other.lastSendDate)
                    && Objects.equals(lastAckDate, other.lastAckDate)
                    && Objects.equals(creationDate, other.creationDate)
                    && Objects.equals(dto, other.dto)
                    && nbRetries == other.nbRetries;
        }
        return ret;
    }

}

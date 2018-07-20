package esa.s1pdgs.cpoc.appcatalog.rest;

import java.util.Date;
import java.util.Objects;

import esa.s1pdgs.cpoc.common.ProductCategory;

/**
 * Message object (without the kafka dto) used by the REST applicative catalog
 * 
 * @author Viveris Technologies
 */
public class MqiLightMessageDto {

    /**
     * Category
     */
    protected ProductCategory category;

    /**
     * Identifier
     */
    protected long identifier;

    /**
     * Topic name
     */
    protected String topic;

    /**
     * Partition identifier
     */
    protected int partition;

    /**
     * Offset in Kafka
     */
    protected long offset;

    /**
     * Group name
     */
    protected String group;

    /**
     * State of the message
     */
    protected MqiStateMessageEnum state;

    /**
     * Pod name who is reading the message
     */
    protected String readingPod;

    /**
     * Date of the last read
     */
    protected Date lastReadDate;

    /**
     * Pod name who is sending the message
     */
    protected String sendingPod;

    /**
     * Date of the last send
     */
    protected Date lastSendDate;

    /**
     * Date of the last ack
     */
    protected Date lastAckDate;

    /**
     * Number of retries
     */
    protected int nbRetries;

    /**
     * Default constructor
     */
    public MqiLightMessageDto() {
        super();
        this.nbRetries = 0;
        this.state = MqiStateMessageEnum.READ;
    }

    /**
     * @param category
     */
    public MqiLightMessageDto(final ProductCategory category) {
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
    public MqiLightMessageDto(final ProductCategory category,
            final long identifier, final String topic, final int partition,
            final long offset) {
        this(category);
        this.identifier = identifier;
        this.topic = topic;
        this.partition = partition;
        this.offset = offset;
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
    public MqiStateMessageEnum getState() {
        return state;
    }

    /**
     * @param state
     *            the state to set
     */
    public void setState(final MqiStateMessageEnum state) {
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
                "category: %s, identifier: %s, topic: %s, partition: %s, offset: %s, group: %s, state: %s, readingPod: %s, lastReadDate: %s, sendingPod: %s, lastSendDate: %s, lastAckDate: %s, nbRetries: %s",
                category, identifier, topic, partition, offset, group, state,
                readingPod, lastReadDate, sendingPod, lastSendDate, lastAckDate,
                nbRetries);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(category, identifier, topic, partition, offset,
                group, state, readingPod, lastReadDate, sendingPod,
                lastSendDate, lastAckDate, nbRetries);
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
            MqiLightMessageDto other = (MqiLightMessageDto) obj;
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
                    && nbRetries == other.nbRetries;
        }
        return ret;
    }

}

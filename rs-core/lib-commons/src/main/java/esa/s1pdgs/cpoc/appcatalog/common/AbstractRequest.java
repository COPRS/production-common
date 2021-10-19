package esa.s1pdgs.cpoc.appcatalog.common;

import java.util.Date;

import esa.s1pdgs.cpoc.common.MessageState;
import esa.s1pdgs.cpoc.common.ProductCategory;

abstract class AbstractRequest {
	/**
	 * Category of the product
	 */
	protected ProductCategory category;
	
	/**
	 * Topic name
	 */
	protected String topic;
	
	/**
	 * Partition number
	 */
	protected int partition;
	/**
	 * Offset of the message
	 */
	protected long offset;
	/**
	 * Group of the message
	 */
	protected String group;
	/**
	 * State of the message
	 */
	protected MessageState state;
	/**
	 * Pod who send the message
	 */
	protected String sendingPod;
	/**
	 * Date whe the pod last send the message
	 */
	protected Date lastSendDate;
	/**
	 * Date where the message was last acknowledge
	 */
	protected Date lastAckDate;
	/**
	 * Number of retries
	 */
	protected int nbRetries;
	/**
	 * Dto of the message
	 */
	protected Object dto;
	/**
	 * Date of the insertion in MongoDB
	 */
	protected Date creationDate;

	protected AbstractRequest() {
	}
	
	protected AbstractRequest(ProductCategory category, String topic, int partition, long offset,
			String group, MessageState state, String sendingPod, Date lastSendDate, Date lastAckDate, int nbRetries,
			Object dto, Date creationDate) {
		this.category = category;
		this.topic = topic;
		this.partition = partition;
		this.offset = offset;
		this.group = group;
		this.state = state;
		this.sendingPod = sendingPod;
		this.lastSendDate = lastSendDate;
		this.lastAckDate = lastAckDate;
		this.nbRetries = nbRetries;
		this.dto = dto;
		this.creationDate = creationDate;
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
	public MessageState getState() {
	    return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(MessageState state) {
	    this.state = state;
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

}
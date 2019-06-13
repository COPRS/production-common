package esa.s1pdgs.cpoc.errorrepo.model.rest;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

import esa.s1pdgs.cpoc.appcatalog.rest.MqiStateMessageEnum;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

/**
 * @author birol_colak@net.werum
 *
 */
public class FailedProcessing<T> {

	private UUID id = null;

	private String processingType = null;

	private MqiStateMessageEnum processingStatus = null;

	private ProductCategory productCategory = null;

	private String partition = null;

	private Long offset = null;

	private String group = null;

	private String failedPod = null;

	private OffsetDateTime lastAssignmentDate = null;

	private String sendingPod = null;

	private OffsetDateTime lastSendDate = null;

	private OffsetDateTime lastAckDate = null;

	private String nbRetries = null;

	private OffsetDateTime creationDate = null;

	private OffsetDateTime failureDate = null;

	private String failureMessage = null;

	private GenericMessageDto<T> processingDetails = null;

	public FailedProcessing<T> id(UUID id) {
		this.id = id;
		return this;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public FailedProcessing<T> processingType(String processingType) {
		this.processingType = processingType;
		return this;
	}

	public String getProcessingType() {
		return processingType;
	}

	public void setProcessingType(String processingType) {
		this.processingType = processingType;
	}

	public FailedProcessing<T> processingStatus(MqiStateMessageEnum processingStatus) {
		this.processingStatus = processingStatus;
		return this;
	}

	public MqiStateMessageEnum getProcessingStatus() {
		return processingStatus;
	}

	public void setProcessingStatus(MqiStateMessageEnum processingStatus) {
		this.processingStatus = processingStatus;
	}

	public FailedProcessing<T> productCategory(ProductCategory productCategory) {
		this.productCategory = productCategory;
		return this;
	}

	public ProductCategory getProductCategory() {
		return productCategory;
	}

	public void setProductCategory(ProductCategory productCategory) {
		this.productCategory = productCategory;
	}

	public FailedProcessing<T> partition(String partition) {
		this.partition = partition;
		return this;
	}

	public String getPartition() {
		return partition;
	}

	public void setPartition(String partition) {
		this.partition = partition;
	}

	public FailedProcessing<T> offset(Long offset) {
		this.offset = offset;
		return this;
	}

	public Long getOffset() {
		return offset;
	}

	public void setOffset(Long offset) {
		this.offset = offset;
	}

	public FailedProcessing<T> group(String group) {
		this.group = group;
		return this;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public FailedProcessing<T> failedPod(String failedPod) {
		this.failedPod = failedPod;
		return this;
	}

	public String getFailedPod() {
		return failedPod;
	}

	public void setFailedPod(String failedPod) {
		this.failedPod = failedPod;
	}

	public FailedProcessing<T> lastAssignmentDate(OffsetDateTime lastAssignmentDate) {
		this.lastAssignmentDate = lastAssignmentDate;
		return this;
	}

	public OffsetDateTime getLastAssignmentDate() {
		return lastAssignmentDate;
	}

	public void setLastAssignmentDate(OffsetDateTime lastAssignmentDate) {
		this.lastAssignmentDate = lastAssignmentDate;
	}

	public FailedProcessing<T> sendingPod(String sendingPod) {
		this.sendingPod = sendingPod;
		return this;
	}

	public String getSendingPod() {
		return sendingPod;
	}

	public void setSendingPod(String sendingPod) {
		this.sendingPod = sendingPod;
	}

	public FailedProcessing<T> lastSendDate(OffsetDateTime lastSendDate) {
		this.lastSendDate = lastSendDate;
		return this;
	}

	public OffsetDateTime getLastSendDate() {
		return lastSendDate;
	}

	public void setLastSendDate(OffsetDateTime lastSendDate) {
		this.lastSendDate = lastSendDate;
	}

	public FailedProcessing<T> lastAckDate(OffsetDateTime lastAckDate) {
		this.lastAckDate = lastAckDate;
		return this;
	}

	public OffsetDateTime getLastAckDate() {
		return lastAckDate;
	}

	public void setLastAckDate(OffsetDateTime lastAckDate) {
		this.lastAckDate = lastAckDate;
	}

	public FailedProcessing<T> nbRetries(String nbRetries) {
		this.nbRetries = nbRetries;
		return this;
	}

	public String getNbRetries() {
		return nbRetries;
	}

	public void setNbRetries(String nbRetries) {
		this.nbRetries = nbRetries;
	}

	public FailedProcessing<T> creationDate(OffsetDateTime creationDate) {
		this.creationDate = creationDate;
		return this;
	}

	public OffsetDateTime getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(OffsetDateTime creationDate) {
		this.creationDate = creationDate;
	}

	public FailedProcessing<T> failureDate(OffsetDateTime failureDate) {
		this.failureDate = failureDate;
		return this;
	}

	public OffsetDateTime getFailureDate() {
		return failureDate;
	}

	public void setFailureDate(OffsetDateTime failureDate) {
		this.failureDate = failureDate;
	}

	public FailedProcessing<T> failureMessage(String failureMessage) {
		this.failureMessage = failureMessage;
		return this;
	}

	public String getFailureMessage() {
		return failureMessage;
	}

	public void setFailureMessage(String failureMessage) {
		this.failureMessage = failureMessage;
	}

	public FailedProcessing<T> processingDetails(GenericMessageDto<T> processingDetails) {
		this.processingDetails = processingDetails;
		return this;
	}

	public GenericMessageDto<T> getProcessingDetails() {
		return processingDetails;
	}

	public void setProcessingDetails(GenericMessageDto<T> processingDetails) {
		this.processingDetails = processingDetails;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		FailedProcessing<T> failedProcessing = (FailedProcessing) o;
		return Objects.equals(this.id, failedProcessing.id)
				&& Objects.equals(this.processingType, failedProcessing.processingType)
				&& Objects.equals(this.processingStatus, failedProcessing.processingStatus)
				&& Objects.equals(this.productCategory, failedProcessing.productCategory)
				&& Objects.equals(this.partition, failedProcessing.partition)
				&& Objects.equals(this.offset, failedProcessing.offset)
				&& Objects.equals(this.group, failedProcessing.group)
				&& Objects.equals(this.failedPod, failedProcessing.failedPod)
				&& Objects.equals(this.lastAssignmentDate, failedProcessing.lastAssignmentDate)
				&& Objects.equals(this.sendingPod, failedProcessing.sendingPod)
				&& Objects.equals(this.lastSendDate, failedProcessing.lastSendDate)
				&& Objects.equals(this.lastAckDate, failedProcessing.lastAckDate)
				&& Objects.equals(this.nbRetries, failedProcessing.nbRetries)
				&& Objects.equals(this.creationDate, failedProcessing.creationDate)
				&& Objects.equals(this.failureDate, failedProcessing.failureDate)
				&& Objects.equals(this.failureMessage, failedProcessing.failureMessage)
				&& Objects.equals(this.processingDetails, failedProcessing.processingDetails);
	}

	@Override
	public int hashCode() {
		return java.util.Objects.hash(id, processingType, processingStatus, productCategory, partition, offset, group,
				failedPod, lastAssignmentDate, sendingPod, lastSendDate, lastAckDate, nbRetries, creationDate,
				failureDate, failureMessage, processingDetails);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class FailedProcessing {\n");

		sb.append("    id: ").append(toIndentedString(id)).append("\n");
		sb.append("    processingType: ").append(toIndentedString(processingType)).append("\n");
		sb.append("    processingStatus: ").append(toIndentedString(processingStatus)).append("\n");
		sb.append("    productCategory: ").append(toIndentedString(productCategory)).append("\n");
		sb.append("    partition: ").append(toIndentedString(partition)).append("\n");
		sb.append("    offset: ").append(toIndentedString(offset)).append("\n");
		sb.append("    group: ").append(toIndentedString(group)).append("\n");
		sb.append("    failedPod: ").append(toIndentedString(failedPod)).append("\n");
		sb.append("    lastAssignmentDate: ").append(toIndentedString(lastAssignmentDate)).append("\n");
		sb.append("    sendingPod: ").append(toIndentedString(sendingPod)).append("\n");
		sb.append("    lastSendDate: ").append(toIndentedString(lastSendDate)).append("\n");
		sb.append("    lastAckDate: ").append(toIndentedString(lastAckDate)).append("\n");
		sb.append("    nbRetries: ").append(toIndentedString(nbRetries)).append("\n");
		sb.append("    creationDate: ").append(toIndentedString(creationDate)).append("\n");
		sb.append("    failureDate: ").append(toIndentedString(failureDate)).append("\n");
		sb.append("    failureMessage: ").append(toIndentedString(failureMessage)).append("\n");
		sb.append("    processingDetails: ").append(toIndentedString(processingDetails)).append("\n");
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Convert the given object to string with each line indented by 4 spaces
	 * (except the first line).
	 */
	private String toIndentedString(java.lang.Object o) {
		if (o == null) {
			return "null";
		}
		return o.toString().replace("\n", "\n    ");
	}

}

package esa.s1pdgs.cpoc.errorrepo.model.rest;

import java.util.Date;
import java.util.Objects;

import esa.s1pdgs.cpoc.appcatalog.rest.MqiGenericMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.MqiStateMessageEnum;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

/**
 * @author birol_colak@net.werum
 *
 */
public class FailedProcessingDto<T extends GenericMessageDto<?>> extends MqiGenericMessageDto<T> {
	
	private static final String TOPIC = "t-pdgs-errors";
	
	public FailedProcessingDto() {
		super();
	}

	public FailedProcessingDto(
			ProductCategory category, 
			long identifier, 
			String topic, 
			int partition, 
			long offset,
			T dto
	) {
		super(category, identifier, TOPIC, partition, offset, dto);
	}

	public FailedProcessingDto(
			ProductCategory category, 
			long identifier, 
			String topic, 
			int partition, 
			long offset
	) {
		super(category, identifier, TOPIC, partition, offset);
	}

	public FailedProcessingDto(ProductCategory category) {
		super(category);
	}
		
	private String processingType = null;
	private String failureMessage = null;

	// maybe map to 'readingPod'
	private String failedPod = null;

	// maybe map to 'lastReadDate'
	private Date lastAssignmentDate = null;
	
	// current time (at creation of this object)
	private Date failureDate = null;

	// original error
	private GenericMessageDto<T> processingDetails = null;

	public FailedProcessingDto<T> processingType(String processingType) {
		this.processingType = processingType;
		return this;
	}
	
	public FailedProcessingDto<T> processingStatus(MqiStateMessageEnum processingStatus) {
		this.setState(processingStatus);
		return this;
	}

	public FailedProcessingDto<T> productCategory(ProductCategory productCategory) {
		this.setCategory(productCategory);
		return this;
	}
	
	public FailedProcessingDto<T> partition(int partition) {
		this.setPartition(partition);
		return this;
	}

	public FailedProcessingDto<T> offset(long offset) {
		this.setOffset(offset);
		return this;
	}
	
	public FailedProcessingDto<T> group(String group) {
		this.setGroup(group);
		return this;
	}
	
	public FailedProcessingDto<T> lastAssignmentDate(Date lastAssignmentDate) {
		this.lastAssignmentDate = lastAssignmentDate;
		return this;
	}

	public FailedProcessingDto<T> failedPod(String failedPod) {
		this.failedPod = failedPod;
		return this;
	}
	
	public FailedProcessingDto<T> sendingPod(String sendingPod) {
		this.setSendingPod(sendingPod);
		return this;
	}
	
	public FailedProcessingDto<T> lastSendDate(Date lastSendDate) {
		this.setLastSendDate(lastSendDate);
		return this;
	}

	public FailedProcessingDto<T> lastAckDate(Date lastAckDate) {
		this.setLastAckDate(lastAckDate);
		return this;
	}
	
	public FailedProcessingDto<T> nbRetries(int nbRetries) {
		this.setNbRetries(nbRetries);
		return this;
	}
	
	public FailedProcessingDto<T> creationDate(Date creationDate) {
		this.setCreationDate(creationDate);
		return this;
	}
	
	public FailedProcessingDto<T> failureDate(Date failureDate) {
		this.failureDate = failureDate;
		return this;
	}
	
	public String getProcessingType() {
		return processingType;
	}

	public void setProcessingType(String processingType) {
		this.processingType = processingType;
	}

	public String getFailedPod() {
		return failedPod;
	}

	public void setFailedPod(String failedPod) {
		this.failedPod = failedPod;
	}

	public Date getLastAssignmentDate() {
		return lastAssignmentDate;
	}

	public void setLastAssignmentDate(Date lastAssignmentDate) {
		this.lastAssignmentDate = lastAssignmentDate;
	}

	public Date getFailureDate() {
		return failureDate;
	}

	public void setFailureDate(Date failureDate) {
		this.failureDate = failureDate;
	}

	public FailedProcessingDto<T> failureMessage(String failureMessage) {
		this.failureMessage = failureMessage;
		return this;
	}

	public String getFailureMessage() {
		return failureMessage;
	}

	public void setFailureMessage(String failureMessage) {
		this.failureMessage = failureMessage;
	}

	public FailedProcessingDto<T> processingDetails(GenericMessageDto<T> processingDetails) {
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
		@SuppressWarnings("unchecked")
		FailedProcessingDto<T> failedProcessing = (FailedProcessingDto<T>) o;
		
		return Objects.equals(this.identifier, failedProcessing.identifier)
				&& Objects.equals(this.processingType, failedProcessing.processingType)
				&& Objects.equals(this.state, failedProcessing.state)
				&& Objects.equals(this.category, failedProcessing.category)
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
		return java.util.Objects.hash(identifier, processingType, state, category, partition, offset, group,
				failedPod, lastAssignmentDate, sendingPod, lastSendDate, lastAckDate, nbRetries, creationDate,
				failureDate, failureMessage, processingDetails);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class FailedProcessing {\n");

		sb.append("    id: ").append(toIndentedString(identifier)).append("\n");
		sb.append("    processingType: ").append(toIndentedString(processingType)).append("\n");
		sb.append("    processingStatus: ").append(toIndentedString(state)).append("\n");
		sb.append("    productCategory: ").append(toIndentedString(category)).append("\n");
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

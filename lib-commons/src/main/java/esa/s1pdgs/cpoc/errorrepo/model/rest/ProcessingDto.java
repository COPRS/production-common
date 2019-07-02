package esa.s1pdgs.cpoc.errorrepo.model.rest;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;

import esa.s1pdgs.cpoc.appcatalog.rest.AppCatMessageDto;
import esa.s1pdgs.cpoc.common.MessageState;
import esa.s1pdgs.cpoc.common.ProductCategory;

public class ProcessingDto extends AppCatMessageDto<Object> {

	private static class AscendingCreationTimeComparator implements Comparator<ProcessingDto>, Serializable {

		private static final long serialVersionUID = 1191382370884793376L;

		/**
		 * order by ascending creation time
		 */
		@Override
		public int compare(ProcessingDto o1, ProcessingDto o2) {

			return o1.getCreationDate().compareTo(o2.getCreationDate());
		}
	}

	public static final Comparator<ProcessingDto> ASCENDING_CREATION_TIME_COMPERATOR = new AscendingCreationTimeComparator();

	public ProcessingDto() {
		super();
	}

	private String processingType = null;

	// maybe map to 'readingPod'
	private String assignedPod = null;

	// maybe map to 'lastReadDate'
	private Date lastAssignmentDate = null;

	public ProcessingDto processingType(String processingType) {
		this.processingType = processingType;
		return this;
	}
	
	public ProcessingDto identifier(long id) {
		this.setIdentifier(id);
		return this;
	}
	
	public ProcessingDto assignedPod(String pod) {
		this.setAssignedPod(pod);
		return this;
	}

	public ProcessingDto topic(String topic) {
		this.setTopic(topic);
		return this;
	}

	public ProcessingDto processingStatus(MessageState processingStatus) {
		this.setState(processingStatus);
		return this;
	}

	public ProcessingDto productCategory(ProductCategory productCategory) {
		this.setCategory(productCategory);
		return this;
	}

	public ProcessingDto partition(int partition) {
		this.setPartition(partition);
		return this;
	}

	public ProcessingDto offset(long offset) {
		this.setOffset(offset);
		return this;
	}

	public ProcessingDto group(String group) {
		this.setGroup(group);
		return this;
	}

	public ProcessingDto lastAssignmentDate(Date lastAssignmentDate) {
		this.lastAssignmentDate = lastAssignmentDate;
		return this;
	}

	public ProcessingDto failedPod(String failedPod) {
		this.assignedPod = failedPod;
		return this;
	}

	public ProcessingDto sendingPod(String sendingPod) {
		this.setSendingPod(sendingPod);
		return this;
	}

	public ProcessingDto lastSendDate(Date lastSendDate) {
		this.setLastSendDate(lastSendDate);
		return this;
	}

	public ProcessingDto lastAckDate(Date lastAckDate) {
		this.setLastAckDate(lastAckDate);
		return this;
	}

	public ProcessingDto nbRetries(int nbRetries) {
		this.setNbRetries(nbRetries);
		return this;
	}

	public ProcessingDto creationDate(Date creationDate) {
		this.setCreationDate(creationDate);
		return this;
	}

	public String getProcessingType() {
		return processingType;
	}

	public void setProcessingType(String processingType) {
		this.processingType = processingType;
	}

	public String getAssignedPod() {
		return assignedPod;
	}

	public void setAssignedPod(String failedPod) {
		this.assignedPod = failedPod;
	}

	public Date getLastAssignmentDate() {
		return lastAssignmentDate;
	}

	public void setLastAssignmentDate(Date lastAssignmentDate) {
		this.lastAssignmentDate = lastAssignmentDate;
	}

	public ProcessingDto processingDetails(Object processingDetails) {
		this.setDto(processingDetails);
		return this;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ProcessingDto failedProcessing = (ProcessingDto) o;

		return Objects.equals(this.getIdentifier(), failedProcessing.getIdentifier())
				&& Objects.equals(this.processingType, failedProcessing.processingType)
				&& Objects.equals(this.getState(), failedProcessing.getState())
				&& Objects.equals(this.getCategory(), failedProcessing.getCategory())
				&& Objects.equals(this.getPartition(), failedProcessing.getPartition())
				&& Objects.equals(this.getOffset(), failedProcessing.getOffset())
				&& Objects.equals(this.getGroup(), failedProcessing.getGroup())
				&& Objects.equals(this.assignedPod, failedProcessing.assignedPod)
				&& Objects.equals(this.lastAssignmentDate, failedProcessing.lastAssignmentDate)
				&& Objects.equals(this.getSendingPod(), failedProcessing.getSendingPod())
				&& Objects.equals(this.getLastSendDate(), failedProcessing.getLastSendDate())
				&& Objects.equals(this.getLastAckDate(), failedProcessing.getLastAckDate())
				&& Objects.equals(this.getNbRetries(), failedProcessing.getNbRetries())
				&& Objects.equals(this.getCreationDate(), failedProcessing.getCreationDate());
	}

	@Override
	public int hashCode() {
		return java.util.Objects.hash(getIdentifier(), processingType, getState(), getCategory(), getPartition(), getOffset(), getGroup(), assignedPod,
				lastAssignmentDate, getSendingPod(), getLastSendDate(), getLastAckDate(), getNbRetries(), getCreationDate());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class FailedProcessing {\n");

		sb.append("    id: ").append(toIndentedString(getIdentifier())).append("\n");
		sb.append("    processingType: ").append(toIndentedString(processingType)).append("\n");
		sb.append("    processingStatus: ").append(toIndentedString(getState())).append("\n");
		sb.append("    productCategory: ").append(toIndentedString(getCategory())).append("\n");
		sb.append("    partition: ").append(toIndentedString(getPartition())).append("\n");
		sb.append("    offset: ").append(toIndentedString(getOffset())).append("\n");
		sb.append("    group: ").append(toIndentedString(getGroup())).append("\n");
		sb.append("    assignedPod: ").append(toIndentedString(assignedPod)).append("\n");
		sb.append("    lastAssignmentDate: ").append(toIndentedString(lastAssignmentDate)).append("\n");
		sb.append("    sendingPod: ").append(toIndentedString(getSendingPod())).append("\n");
		sb.append("    lastSendDate: ").append(toIndentedString(getLastSendDate())).append("\n");
		sb.append("    lastAckDate: ").append(toIndentedString(getLastAckDate())).append("\n");
		sb.append("    nbRetries: ").append(toIndentedString(getNbRetries())).append("\n");
		sb.append("    creationDate: ").append(toIndentedString(getCreationDate())).append("\n");
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

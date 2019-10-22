package esa.s1pdgs.cpoc.appcatalog.common;

import java.util.Comparator;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.common.MessageState;
import esa.s1pdgs.cpoc.common.ProductCategory;

public class Processing {
	private static class AscendingCreationTimeComparator implements Comparator<Processing> {
		/**
		 * order by ascending creation time
		 */
		@Override
		public int compare(Processing o1, Processing o2) {
			return o1.getCreationDate().compareTo(o2.getCreationDate());
		}
	}
	
	@JsonIgnore
	public static final Comparator<Processing> ASCENDING_CREATION_TIME_COMPARATOR = new AscendingCreationTimeComparator();

	private final MqiMessage mqiMessage;

	public Processing(MqiMessage mqiMessage) {
		this.mqiMessage = mqiMessage;
	}

	@JsonProperty("id")
	public long getIdentifier() {
		return mqiMessage.getId();
	}

	@JsonProperty("productCategory")
	public ProductCategory getCategory() {
		return mqiMessage.getCategory();
	}

	@JsonProperty("assignedPod")
	public String getReadingPod() {
		return mqiMessage.getReadingPod();
	}

	@JsonProperty("processingType")
	public String getTopic() {
		return mqiMessage.getTopic();
	}
	
	@JsonProperty("lastAssignmentDate")
	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone="UTC")
	public Date getLastReadDate() {
		return mqiMessage.getLastReadDate();
	}

	public int getPartition() {
		return mqiMessage.getPartition();
	}

	public long getOffset() {
		return mqiMessage.getOffset();
	}

	public String getGroup() {
		return mqiMessage.getGroup();
	}

	@JsonProperty("processingStatus")
	public MessageState getState() {
		return mqiMessage.getState();
	}

	public String getSendingPod() {
		return mqiMessage.getSendingPod();
	}

	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone="UTC")
	public Date getLastSendDate() {
		return mqiMessage.getLastSendDate();
	}

	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone="UTC")
	public Date getLastAckDate() {
		return mqiMessage.getLastAckDate();
	}

	public int getNbRetries() {
		return mqiMessage.getNbRetries();
	}

	@JsonProperty("processingDetails")
	public Object getDto() {
		return mqiMessage.getDto();
	}

	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone="UTC")
	public Date getCreationDate() {
		return mqiMessage.getCreationDate();
	}
	
	
	

}

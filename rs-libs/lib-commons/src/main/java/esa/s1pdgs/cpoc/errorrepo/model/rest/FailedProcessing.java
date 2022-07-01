package esa.s1pdgs.cpoc.errorrepo.model.rest;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import esa.s1pdgs.cpoc.metadata.model.MissionId;

public class FailedProcessing {
	
	private static class AscendingCreationTimeComparator implements Comparator<FailedProcessing>, Serializable {
		private static final long serialVersionUID = 1370191382793768483L;

		/**
		 * order by ascending creation time
		 */
		@Override
		public int compare(final FailedProcessing o1, final FailedProcessing o2) {
			return o1.getFailureDate().compareTo(o2.getFailureDate());
		}
	}
	
	@JsonIgnore
	public static final Comparator<FailedProcessing> ASCENDING_CREATION_TIME_COMPARATOR = new AscendingCreationTimeComparator();

	private String id;
	private Date failureDate;
	private MissionId missionId;
	private String failureMessage;
	private String topic;
	private Object message;
	private String stacktrace;
	private String errorLevel;
	private int retryCounter;
	
	public FailedProcessing() {
	}

	public FailedProcessing(final String topic, final Date failureDate,
			final MissionId missionId, final String errorLevel, final Object message,
			final String failureMessage, final String stacktrace, final int retryCounter) {
		this.topic = topic;
		this.failureDate = failureDate;
		this.missionId = missionId;
		this.errorLevel = errorLevel;
		this.message = message;
		this.failureMessage = failureMessage;
		this.stacktrace = stacktrace;
		this.retryCounter = retryCounter;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone="UTC")
	public Date getFailureDate() {
		return failureDate;
	}

	public void setFailureDate(final Date failureDate) {
		this.failureDate = failureDate;
	}
	
	public MissionId getMissionId() {
		return missionId;
	}

	public void setMissionId(MissionId missionId) {
		this.missionId = missionId;
	}

	public String getFailureMessage() {
		return failureMessage;
	}

	public void setFailureMessage(final String failureMessage) {
		this.failureMessage = failureMessage;
	}
	
	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public Object getMessage() {
		return message;
	}

	public void setMessage(Object message) {
		this.message = message;
	}

	public String getStacktrace() {
		return stacktrace;
	}

	public void setStacktrace(String stacktrace) {
		this.stacktrace = stacktrace;
	}

	public String getErrorLevel() {
		return errorLevel;
	}

	public void setErrorLevel(String errorLevel) {
		this.errorLevel = errorLevel;
	}

	public int getRetryCounter() {
		return retryCounter;
	}

	public void setRetryCounter(int retryCounter) {
		this.retryCounter = retryCounter;
	}

	@Override
	public int hashCode() {
		return Objects.hash(errorLevel, failureDate, failureMessage, id, message, missionId, topic, retryCounter,
				stacktrace);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FailedProcessing other = (FailedProcessing) obj;
		return Objects.equals(errorLevel, other.errorLevel) && Objects.equals(failureDate, other.failureDate)
				&& Objects.equals(failureMessage, other.failureMessage) && Objects.equals(id, other.id)
				&& Objects.equals(message, other.message) && missionId == other.missionId
				&& Objects.equals(topic, other.topic) && retryCounter == other.retryCounter
				&& Objects.equals(stacktrace, other.stacktrace);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("class FailedProcessing {\n");
		sb.append("    id: ").append(toIndentedString(id)).append("\n");
		sb.append("    missionId: ").append(toIndentedString(getMissionId())).append("\n");
		sb.append("    failureDate: ").append(toIndentedString(failureDate)).append("\n");
		sb.append("    failureMessage: ").append(toIndentedString(failureMessage)).append("\n");
		sb.append("    topic: ").append(toIndentedString(getTopic())).append("\n");
		sb.append("    message: ").append(toIndentedString(getMessage())).append("\n");
		sb.append("    stacktrace: ").append(toIndentedString(getStacktrace())).append("\n");
		sb.append("    errorLevel: ").append(toIndentedString(getErrorLevel())).append("\n");
		sb.append("    retryCounter: ").append(toIndentedString(getRetryCounter())).append("\n");
		sb.append("}");
		return sb.toString();
	}
	
	/**
	 * Convert the given object to string with each line indented by 4 spaces
	 * (except the first line).
	 */
	private final String toIndentedString(final java.lang.Object o) {
		if (o == null) {
			return "null";
		}
		return o.toString().replace("\n", "\n    ");
	}

}
package esa.s1pdgs.cpoc.errorrepo.model.rest;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

/**
 * @author birol_colak@net.werum
 *
 */
public class FailedProcessingDto {    
	private String failedPod; // deprecated
	private Date failedDate;
	private String failureMessage;
	private GenericMessageDto<?> processingDetails; // deprecated
	private GenericMessageDto<?> predecessor; // deprecated
	private String topic;
	private Object message;
	private String stacktrace;
	private String errorLevel;
	private int retryCounter;
	
	public FailedProcessingDto() {
	}

	public FailedProcessingDto(final String topic, final Date failedDate, final String errorLevel,
			final Object message, final String failureMessage, final String stacktrace,
			 final int retryCounter) {
		this.topic = topic;
		this.failedDate = failedDate;
		this.errorLevel = errorLevel;
		this.message = message;
		this.failureMessage = failureMessage;
		this.stacktrace = stacktrace;
		this.retryCounter = retryCounter;
	}

	// deprecated
	public FailedProcessingDto(final String failedPod, final Date failedDate, final String failureMessage,
			final GenericMessageDto<?> processingDetails) {
		super();
		this.failedPod = failedPod;
		this.failedDate = failedDate;
		this.failureMessage = failureMessage;
		this.processingDetails = processingDetails;
	}

	public String getFailedPod() {
		return failedPod;
	}

	public void setFailedPod(final String failedPod) {
		this.failedPod = failedPod;
	}

	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone="UTC")
	public Date getFailedDate() {
		return failedDate;
	}

	public void setFailedDate(final Date failedDate) {
		this.failedDate = failedDate;
	}

	public String getFailureMessage() {
		return failureMessage;
	}

	public void setFailureMessage(final String failureMessage) {
		this.failureMessage = failureMessage;
	}

	public GenericMessageDto<?> getProcessingDetails() {
		return processingDetails;
	}

	public void setProcessingDetails(final GenericMessageDto<?> processingDetails) {
		this.processingDetails = processingDetails;
	}

	public GenericMessageDto<?> getPredecessor() {
		return predecessor;
	}

	public void setPredecessor(final GenericMessageDto<?> predecessor) {
		this.predecessor = predecessor;
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
	public String toString() {
		return "FailedProcessingDto [failedPod=" + failedPod + ", failedDate=" + failedDate + ", failureMessage="
				+ failureMessage + ", processingDetails=" + processingDetails + ", predecessor=" + predecessor
				+ ", topic=" + topic + ", message=" + message + ", stacktrace=" + stacktrace + ", errorLevel="
				+ errorLevel + ", retryCounter=" + retryCounter + "]";
	}

}

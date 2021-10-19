package esa.s1pdgs.cpoc.errorrepo.model.rest;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

/**
 * @author birol_colak@net.werum
 *
 */
public class FailedProcessingDto {    
	private String failedPod;
	private Date failedDate;
	private String failureMessage;
	private GenericMessageDto<?> processingDetails;
	private GenericMessageDto<?> predecessor;
	
	public FailedProcessingDto() {
	}

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

	@Override
	public String toString() {
		return "FailedProcessingDto [failedPod=" + failedPod + ", failedDate=" + failedDate + ", failureMessage="
				+ failureMessage + ", processingDetails=" + processingDetails + ", predecessor=" + predecessor + "]";
	}
}

package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Collections;
import java.util.Objects;

import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.mqi.model.control.ControlAction;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

public class IpfPreparationJob extends AbstractMessage {	
    private ApplicationLevel level;
    private GenericMessageDto<CatalogEvent> eventMessage;
    private String taskTableName;
    private String startTime;    
    private String stopTime;    

    public IpfPreparationJob() {
		allowedControlActions = Collections.singletonList(ControlAction.RESTART);
	}
    
	public ApplicationLevel getLevel() {
		return level;
	}

	public void setLevel(final ApplicationLevel level) {
		this.level = level;
	}

	public GenericMessageDto<CatalogEvent> getEventMessage() {
		return eventMessage;
	}

	public void setEventMessage(final GenericMessageDto<CatalogEvent> eventMessage) {
		this.eventMessage = eventMessage;
	}

	public String getTaskTableName() {
		return taskTableName;
	}

	public void setTaskTableName(final String taskTableName) {
		this.taskTableName = taskTableName;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(final String startTime) {
		this.startTime = startTime;
	}

	public String getStopTime() {
		return stopTime;
	}

	public void setStopTime(final String stopTime) {
		this.stopTime = stopTime;
	}


	@Override
	public int hashCode() {
		return Objects.hash(
				creationDate, 
				hostname, 
				keyObjectStorage,
				productFamily, 
				uid,
			    level,
			    eventMessage,
			    taskTableName,
			    startTime,  
			    stopTime,   
			    allowedControlActions,
			    controlDemandType,
			    controlRetryCounter,
			    controlDebug
		);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final IpfPreparationJob other = (IpfPreparationJob) obj;
		return Objects.equals(creationDate, other.creationDate) 			
				&& Objects.equals(hostname, other.hostname)
				&& Objects.equals(keyObjectStorage, other.keyObjectStorage)
				&& Objects.equals(uid, other.uid)
			    && Objects.equals(level,other.level)	    
			    && Objects.equals(allowedControlActions, other.allowedControlActions)
			    && Objects.equals(controlDemandType, other.controlDemandType)
			    && Objects.equals(controlRetryCounter, other.controlRetryCounter)
			    && Objects.equals(controlDebug, other.controlDebug)			    
			    && Objects.equals(eventMessage, other.eventMessage)
			    && Objects.equals(taskTableName,other.taskTableName)
			    && Objects.equals(startTime, other.startTime)
			    && Objects.equals(stopTime, other.stopTime) 							
				&& productFamily == other.productFamily;
	}

	@Override
	public String toString() {
		return "IpfPreparationJob [level=" + level + ", eventMessage=" + eventMessage + ", taskTableName="
				+ taskTableName + ", startTime=" + startTime + ", stopTime=" + stopTime 
				+ ", productFamily=" + productFamily + ", keyObjectStorage=" + keyObjectStorage + ", uid=" + uid
				+ ", creationDate=" + creationDate + ", hostname=" + hostname + ", allowedControlActions="
				+ allowedControlActions + ", controlDemandType=" + controlDemandType + ", controlRetryCounter="
				+ controlRetryCounter + ", controlDebug=" + controlDebug + "]";
	}
}

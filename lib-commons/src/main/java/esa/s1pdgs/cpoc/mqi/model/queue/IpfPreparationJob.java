package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Collections;

import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.mqi.model.control.AllowedAction;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

public class IpfPreparationJob extends AbstractMessage {	
    private ApplicationLevel level;
    private GenericMessageDto<CatalogEvent> eventMessage;
    private String taskTableName;
    private String startTime;    
    private String stopTime;    
    private String outputProductType;

    public IpfPreparationJob() {
		allowedActions = Collections.singletonList(AllowedAction.RESTART);
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
	public String getOutputProductType() {
		return outputProductType;
	}

	public void setOutputProductType(final String outputProductType) {
		this.outputProductType = outputProductType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((eventMessage == null) ? 0 : eventMessage.hashCode());
		result = prime * result + ((level == null) ? 0 : level.hashCode());
		result = prime * result + ((outputProductType == null) ? 0 : outputProductType.hashCode());
		result = prime * result + ((startTime == null) ? 0 : startTime.hashCode());
		result = prime * result + ((stopTime == null) ? 0 : stopTime.hashCode());
		result = prime * result + ((taskTableName == null) ? 0 : taskTableName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		IpfPreparationJob other = (IpfPreparationJob) obj;
		if (eventMessage == null) {
			if (other.eventMessage != null)
				return false;
		} else if (!eventMessage.equals(other.eventMessage))
			return false;
		if (level != other.level)
			return false;
		if (outputProductType == null) {
			if (other.outputProductType != null)
				return false;
		} else if (!outputProductType.equals(other.outputProductType))
			return false;
		if (startTime == null) {
			if (other.startTime != null)
				return false;
		} else if (!startTime.equals(other.startTime))
			return false;
		if (stopTime == null) {
			if (other.stopTime != null)
				return false;
		} else if (!stopTime.equals(other.stopTime))
			return false;
		if (taskTableName == null) {
			if (other.taskTableName != null)
				return false;
		} else if (!taskTableName.equals(other.taskTableName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "IpfPreparationJob [level=" + level + ", eventMessage=" + eventMessage + ", taskTableName="
				+ taskTableName + ", startTime=" + startTime + ", stopTime=" + stopTime 
				+ ", productFamily=" + productFamily + ", keyObjectStorage=" + keyObjectStorage + ", uid=" + uid
				+ ", creationDate=" + creationDate + ", hostname=" + hostname + ", allowedActions="
				+ allowedActions + ", demandType=" + demandType + ", retryCounter="
				+ retryCounter + ", debug=" + debug + ", outputProductType="  + outputProductType + "]";
	}

}

package esa.s1pdgs.cpoc.dlq.manager.model.mapping;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public abstract class JsonMapping {
	
	// only used for persisting as-is via MongoDB Sink
	
	@JsonIgnore
	abstract int getId();
	
	@JsonSerialize(using = CustomDateSerializer.class)
	abstract Date getFailureDate();
}
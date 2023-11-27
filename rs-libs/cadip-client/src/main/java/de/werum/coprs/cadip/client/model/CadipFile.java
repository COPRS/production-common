package de.werum.coprs.cadip.client.model;

import java.time.LocalDateTime;
import java.util.UUID;

public interface CadipFile {
	
	UUID getId();

	String getName();

	String getSessionId();

	Long getChannel();

	Long getBlockNumber();

	Boolean getFinalBlock();

	LocalDateTime getPublicationDate();

	LocalDateTime getEvictionDate();

	Long getSize();

	Boolean getRetransfer();
}

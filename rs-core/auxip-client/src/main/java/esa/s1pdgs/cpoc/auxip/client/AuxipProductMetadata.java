package esa.s1pdgs.cpoc.auxip.client;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AuxipProductMetadata {
	
	UUID getId();
	
	String getProductName();
	
	LocalDateTime getCreationDate();
	
	List<String> getParsingErrors();
	
	URI getRootServiceUrl();

	long getContentLength();

}

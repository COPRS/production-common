package esa.s1pdgs.cpoc.auxip.client;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.lang.NonNull;

public interface AuxipClient {
	
	List<AuxipProductMetadata> list(@NonNull LocalDateTime from, @NonNull LocalDateTime to, Integer top, Integer skip,
			AuxipProductFilter filter);
	
	//InputStream read(AuxipProductMetadata metadata);

}

package esa.s1pdgs.cpoc.auxip.client;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.lang.NonNull;

public interface AuxipClient {
	
	/**
	 * Returns the product metadata from an AUXIP interface.
	 * 
	 * @param from the beginning of the timeframe interval(inclusive)
	 * @param to the end of the timeframe interval (exclusive)
	 * @param pageSize the max size of the search result
	 * @param offset the offset for the returned elements (paging)
	 * @return the product metadata from an AUXIP interface
	 */
	List<AuxipProductMetadata> getMetadata(@NonNull LocalDateTime from, @NonNull LocalDateTime to, Integer pageSize,
			Integer offset);
	
	/**
	 * Returns the product metadata from an AUXIP interface.
	 * 
	 * @param from                the beginning of the timeframe interval(inclusive)
	 * @param to                  the end of the timeframe interval (exclusive)
	 * @param pageSize            the max size of the search result
	 * @param offset              the offset for the returned elements (paging)
	 * @param productNameContains a strings which should be part of the
	 *                            product name of the returned results
	 * @return the product metadata from an AUXIP interface
	 */
	List<AuxipProductMetadata> getMetadata(@NonNull LocalDateTime from, @NonNull LocalDateTime to, Integer pageSize,
			Integer offset, String productNameContains);
	
	//InputStream read(AuxipProductMetadata metadata);

}

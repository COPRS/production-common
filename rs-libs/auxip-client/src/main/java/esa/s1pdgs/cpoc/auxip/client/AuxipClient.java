package esa.s1pdgs.cpoc.auxip.client;

import java.io.Closeable;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.lang.NonNull;

public interface AuxipClient extends Closeable {
	
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
	
	/**
	 * Returns the a stream to the actual product file referenced by the given ID in this particular AUXIP client.<br>
	 * It is not guaranteed however, that the product is still downloadable, even if the product was queried just a second before.
	 * So the calling code must be able to cope with this.<br>
	 * <br>
	 * Note: It is the callers responsibility to close the stream after consumption!
	 * 
	 * @param productMetadataId the ID of the product returned from querying this AUXIP client
	 * @return the a stream to the actual product file referenced by the given ID in this particular AUXIP client
	 */
	InputStream read(@NonNull UUID productMetadataId);

	/**
	 * @return {@code true}, if this client is disabled and won't make queries,<br>
	 *         {@code false}, if this client is active and will make queries to its configured target host
	 */
	boolean isDisabled();

}

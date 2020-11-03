package esa.s1pdgs.cpoc.prip.metadata;

import java.util.List;
import java.util.Optional;

import esa.s1pdgs.cpoc.prip.model.PripMetadata;
import esa.s1pdgs.cpoc.prip.model.filter.PripQueryFilter;

public interface PripMetadataRepository {

	/**
	 * Saves a new PRIP metadata.
	 * 
	 * @param pripMetadata
	 */
	public void save(PripMetadata pripMetadata);
	
	/**
	 * Finds a PRIP metadata by its ID. Returns null if not found.
	 * 
	 * @param id
	 * @return
	 */
	public PripMetadata findById(String id);

	/**
	 * Returns all PRIP metadata.
	 * 
	 * @param top
	 * @param skip
	 * @return
	 */
	public List<PripMetadata> findAll(Optional<Integer> top, Optional<Integer> skip);

	/**
	 * Querying the repository with filters.
	 * When filters are empty this is like calling {@link #findAll(Optional, Optional)}. 
	 *
	 * @param filters to narrow the query 
	 * @param top for paging
	 * @param skip fir paging
	 * @return the search result
	 */
	public List<PripMetadata> findWithFilters(List<PripQueryFilter> filters, Optional<Integer> top, Optional<Integer> skip);

	/**
	 * Counts all PRIP metadata.
	 * 
	 * @return
	 */
	public int countAll();
	
	/**
	 * Counts PRIP metadata by creation date and name using date time and name filters.
	 * Each of the the result matches with all filters provided for the fields 'creationDate' and 'name'.
	 * 
	 * @param filters can be empty
	 * @return
	 */
	public int countWithFilters(List<PripQueryFilter> filters);

}

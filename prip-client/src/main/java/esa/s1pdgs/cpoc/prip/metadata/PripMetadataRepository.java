package esa.s1pdgs.cpoc.prip.metadata;

import java.util.List;
import java.util.Optional;

import esa.s1pdgs.cpoc.prip.model.PripDateTimeFilter;
import esa.s1pdgs.cpoc.prip.model.PripMetadata;
import esa.s1pdgs.cpoc.prip.model.PripTextFilter;

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
	 * Finds PRIP metadata by creation date using date time filters.
	 * Each of the the result matches with all filters provided for the field 'creationDate'.
	 * 
	 * @param creationDateFilters
	 * @param top
	 * @param skip
	 * @return
	 */
	public List<PripMetadata> findByCreationDate(List<PripDateTimeFilter> creationDateFilters, Optional<Integer> top, Optional<Integer> skip);

	/**
	 * Finds PRIP metadata by product name using name filters. 
	 * Each of the the result matches with all filters provided for the field 'name'.
	 * 
	 * @param nameFilters
	 * @param top
	 * @param skip
	 * @return
	 */
	public List<PripMetadata> findByProductName(List<PripTextFilter> nameFilters, Optional<Integer> top, Optional<Integer> skip);

	/**
	 * Finds PRIP metadata by creation date and name using date time and name filters.
	 * Each of the the result matches with all filters provided for the fields 'creationDate' and 'name'.
	 * 
	 * @param creationDateFilters can be empty
	 * @param nameFilters can be empty
	 * @param top
	 * @param skip
	 * @return
	 */
	public List<PripMetadata> findByCreationDateAndProductName(List<PripDateTimeFilter> creationDateFilters,
			List<PripTextFilter> nameFilters, Optional<Integer> top, Optional<Integer> skip);

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
	 * @param creationDateFilters can be empty
	 * @param nameFilters can be empty
	 * @return
	 */
	public int countByCreationDateAndProductName(List<PripDateTimeFilter> creationDateFilters,
			List<PripTextFilter> nameFilters);
}

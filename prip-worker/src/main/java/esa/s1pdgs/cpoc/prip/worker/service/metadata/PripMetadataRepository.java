package esa.s1pdgs.cpoc.prip.worker.service.metadata;

import java.util.List;

import esa.s1pdgs.cpoc.prip.worker.model.PripDateTimeFilter;
import esa.s1pdgs.cpoc.prip.worker.model.PripMetadata;
import esa.s1pdgs.cpoc.prip.worker.model.PripTextFilter;

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
	 * @return
	 */
	public List<PripMetadata> findAll();

	/**
	 * Finds PRIP metadata by creation date using date time filters.
	 * Each of the the result matches with all filters provided for the field 'creationDate'.
	 * 
	 * @param creationDateFilters
	 * @return
	 */
	public List<PripMetadata> findByCreationDate(List<PripDateTimeFilter> creationDateFilters);

	/**
	 * Finds PRIP metadata by product name using name filters. 
	 * Each of the the result matches with all filters provided for the field 'name'.
	 * 
	 * @param nameFilters
	 * @return
	 */
	public List<PripMetadata> findByProductName(List<PripTextFilter> nameFilters);

	/**
	 * Finds PRIP metadata by creation date and name using date time and name filters.
	 * Each of the the result matches with all filters provided for the fields 'creationDate' and 'name'.
	 * 
	 * @param creationDateFilters can be empty
	 * @param nameFilters can be empty
	 * @return
	 */
	public List<PripMetadata> findByCreationDateAndProductName(List<PripDateTimeFilter> creationDateFilters,
			List<PripTextFilter> nameFilters);


}

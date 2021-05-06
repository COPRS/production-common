package esa.s1pdgs.cpoc.prip.metadata;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import esa.s1pdgs.cpoc.prip.model.PripMetadata;
import esa.s1pdgs.cpoc.prip.model.PripSortTerm;
import esa.s1pdgs.cpoc.prip.model.filter.PripQueryFilter;

public interface PripMetadataRepository {

	/**
	 * Saves a new PRIP metadata.
	 *
	 * @param pripMetadata
	 */
	void save(PripMetadata pripMetadata);

	/**
	 * Finds a PRIP metadata by its ID. Returns null if not found.
	 *
	 * @param id
	 * @return
	 */
	PripMetadata findById(String id);
	
	/**
	 * Finds a PRIP metadata by its name. Returns null if not found.
	 *
	 * @param id
	 * @return
	 * @throws Exception
	 */
	PripMetadata findByName(String name) throws Exception;

	/**
	 * Returns all PRIP metadata.
	 *
	 * @param top
	 * @param skip
	 * @return
	 */
	default List<PripMetadata> findAll(Optional<Integer> top, Optional<Integer> skip) {
		return this.findAll(top, skip, Collections.emptyList());
	}

	/**
	 * Returns all PRIP metadata.
	 *
	 * @param top
	 * @param skip
	 * @param sortTerms
	 * @return
	 */
	List<PripMetadata> findAll(Optional<Integer> top, Optional<Integer> skip, List<PripSortTerm> sortTerms);

	/**
	 * Querying the repository with filters.
	 * When filters are empty this is like calling {@link #findAll(Optional, Optional)}.
	 *
	 * @param filters to narrow the query
	 * @param top for paging
	 * @param skip fir paging
	 * @return the search result
	 */
	default List<PripMetadata> findWithFilters(List<PripQueryFilter> filters, Optional<Integer> top, Optional<Integer> skip) {
		return this.findWithFilters(filters, top, skip, Collections.emptyList());
	}

	/**
	 * Querying the repository with filters.
	 * When filters are empty this is like calling {@link #findAll(Optional, Optional)}.
	 *
	 * @param filters to narrow the query
	 * @param top for paging
	 * @param skip fir paging
	 * @param sortTerms
	 * @return the search result
	 */
	List<PripMetadata> findWithFilters(List<PripQueryFilter> filters, Optional<Integer> top, Optional<Integer> skip, List<PripSortTerm> sortTerms);

	/**
	 * Counts all PRIP metadata.
	 *
	 * @return
	 */
	int countAll();

	/**
	 * Counts PRIP metadata by creation date and name using date time and name filters.
	 * Each of the the result matches with all filters provided for the fields 'creationDate' and 'name'.
	 *
	 * @param filters can be empty
	 * @return
	 */
	int countWithFilters(List<PripQueryFilter> filters);

}

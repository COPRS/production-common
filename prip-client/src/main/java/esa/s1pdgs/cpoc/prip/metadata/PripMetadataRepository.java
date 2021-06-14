package esa.s1pdgs.cpoc.prip.metadata;

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
	 * Querying the repository with filter.
	 * When filter is {@code null} or empty this is like calling {@link #findAll(Optional, Optional)}.
	 *
	 * @param filter to narrow the query
	 * @param top for paging
	 * @param skip fir paging
	 * @return the search result
	 */
	default List<PripMetadata> findWithFilter(final PripQueryFilter filter, final Optional<Integer> top, final Optional<Integer> skip) {
		return this.findWithFilter(filter, top, skip, Collections.emptyList());
	}

	/**
	 * Querying the persistence with filter.
	 * When filter is {@code null} or empty this is like calling {@link #findAll(Optional, Optional)}.
	 *
	 * @param filter to narrow the query
	 * @param top for paging
	 * @param skip fir paging
	 * @param sortTerms
	 * @return the search result
	 */
	List<PripMetadata> findWithFilter(final PripQueryFilter filter, final Optional<Integer> top, final Optional<Integer> skip,
			final List<PripSortTerm> sortTerms);

	/**
	 * Counts all PRIP metadata.
	 *
	 * @return
	 */
	int countAll();

	/**
	 * Counts PRIP metadata returned from persistence after applying the given filter.
	 *
	 * @param filter can be {@code null}
	 * @return
	 */
	int countWithFilter(final PripQueryFilter filter);

}

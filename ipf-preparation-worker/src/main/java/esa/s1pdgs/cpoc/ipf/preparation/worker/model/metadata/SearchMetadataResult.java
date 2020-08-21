package esa.s1pdgs.cpoc.ipf.preparation.worker.model.metadata;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import esa.s1pdgs.cpoc.metadata.client.SearchMetadataQuery;
import esa.s1pdgs.cpoc.metadata.model.SearchMetadata;

/**
 * Class representing the result of a search query for metadata
 * 
 * @author Cyrielle Gailliard
 *
 */
public class SearchMetadataResult {

	private SearchMetadataQuery query;
	
	// WARNING: Previously, there were some "unhealthy" semantics coupled to this field. It had been 'null'
	// if the query has not been performed, empty if no results were returned from query and contained 
	// the results, if there were any. To get it at least a little but cleaned up and avoid those nasty NPE
	// we'll make it explicit with an additonal boolean field to avoid these null scenarios.
	private List<SearchMetadata> result = Collections.emptyList();
	private boolean hasResults = false;

	public SearchMetadataResult(final SearchMetadataQuery query) {
		this.query = query;
	}

	public SearchMetadataQuery getQuery() {
		return query;
	}

	public void setQuery(final SearchMetadataQuery query) {
		this.query = query;
	}

	public List<SearchMetadata> getResult() {
		return result;
	}

	public boolean hasResult() {
		return hasResults;
	}

	public void setResult(final List<SearchMetadata> result) {
		this.result = result;
		this.hasResults = !result.isEmpty();
	}

	public String toJsonString() {
		return String.format("{query: %s, result: %s}", query, result);
	}

	@Override
	public int hashCode() {
		return Objects.hash(query, result, hasResults);
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		boolean ret;
		if (this == obj) {
			ret = true;
		} else if (obj == null || getClass() != obj.getClass()) {
			ret = false;
		} else {
			final SearchMetadataResult other = (SearchMetadataResult) obj;
			ret = Objects.equals(query, other.query) && 
					Objects.equals(result, other.result) &&
					hasResults == other.hasResults;
		}
		return ret;
	}

	@Override
	public String toString() {
		return "SearchMetadataResult [query=" + query + ", result=" + result + "]";
	}
}

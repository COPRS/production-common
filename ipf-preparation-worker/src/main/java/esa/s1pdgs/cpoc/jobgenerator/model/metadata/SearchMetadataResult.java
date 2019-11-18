package esa.s1pdgs.cpoc.jobgenerator.model.metadata;

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

	/**
	 * Query
	 */
	private SearchMetadataQuery query;

	/**
	 * Result. Null if not found
	 */
	private List<SearchMetadata> result;

	/**
	 * Constructor using fields
	 * 
	 * @param query
	 * @param res
	 */
	public SearchMetadataResult(final SearchMetadataQuery query) {
		this.query = query;
	}
	
	public SearchMetadataResult() {
		
	}

	/**
	 * @return the query
	 */
	public SearchMetadataQuery getQuery() {
		return query;
	}

	/**
	 * @param query
	 *            the query to set
	 */
	public void setQuery(final SearchMetadataQuery query) {
		this.query = query;
	}

	/**
	 * @return the result
	 */
	public List<SearchMetadata> getResult() {
		return result;
	}

	/**
	 * @param result
	 *            the result to set
	 */
	public void setResult(final List<SearchMetadata> result) {
		this.result = result;
	}

	public String toJsonString() {
		return String.format("{query: %s, result: %s}", query, result);
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(query, result);
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
			SearchMetadataResult other = (SearchMetadataResult) obj;
			ret = Objects.equals(query, other.query) && Objects.equals(result, other.result);
		}
		return ret;
	}

	@Override
	public String toString() {
		return "SearchMetadataResult [query=" + query + ", result=" + result + "]";
	}

	
}

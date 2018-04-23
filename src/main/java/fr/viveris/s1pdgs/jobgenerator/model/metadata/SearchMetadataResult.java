package fr.viveris.s1pdgs.jobgenerator.model.metadata;

/**
 * Class representing the result of a search query for metadata
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
	private SearchMetadata result;

	/**
	 * Default constrcutor
	 */
	public SearchMetadataResult() {
	}

	/**
	 * Constructor using fields
	 * @param query
	 * @param res
	 */
	public SearchMetadataResult(SearchMetadataQuery query) {
		this();
		this.query = query;
	}

	/**
	 * Constructor using fields
	 * @param query
	 * @param res
	 */
	public SearchMetadataResult(SearchMetadataQuery query, SearchMetadata result) {
		super();
		this.query = query;
		this.result = result;
	}

	/**
	 * Constructor using fields
	 * @param query
	 * @param res
	 */
	public SearchMetadataResult(SearchMetadataResult obj) {
		this();
		this.query = new SearchMetadataQuery(obj.getQuery());
		this.result = new SearchMetadata(obj.getResult());
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
	public void setQuery(SearchMetadataQuery query) {
		this.query = query;
	}

	/**
	 * @return the result
	 */
	public SearchMetadata getResult() {
		return result;
	}

	/**
	 * @param result
	 *            the result to set
	 */
	public void setResult(SearchMetadata result) {
		this.result = result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SearchMetadataResult [query=" + query + ", result=" + result + "]";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((query == null) ? 0 : query.hashCode());
		result = prime * result + ((this.result == null) ? 0 : this.result.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SearchMetadataResult other = (SearchMetadataResult) obj;
		if (query == null) {
			if (other.query != null)
				return false;
		} else if (!query.equals(other.query))
			return false;
		if (result == null) {
			if (other.result != null)
				return false;
		} else if (!result.equals(other.result))
			return false;
		return true;
	}

}

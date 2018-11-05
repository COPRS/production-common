package esa.s1pdgs.cpoc.mdcatalog.es.model;

/**
 * Object containing the metadata from ES
 *
 * @author Viveris Technologies
 */
public class SearchMetadata extends AbstractMetadata {
        
	public SearchMetadata() {
		super();
	}

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return super.toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        return super.equals(obj);
    }

}

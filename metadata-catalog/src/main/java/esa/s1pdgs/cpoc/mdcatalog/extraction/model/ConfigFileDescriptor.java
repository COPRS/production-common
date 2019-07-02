package esa.s1pdgs.cpoc.mdcatalog.extraction.model;

/**
 * Class describing a configuration file (AUX and MPL)
 * 
 * @author Cyrielle Gailliard
 *
 */
public class ConfigFileDescriptor extends AbstractFileDescriptor {

	/**
	 * Default constructor
	 */
	public ConfigFileDescriptor() {
		super();
	}

	/**
	 * String formatting
	 */
	@Override
	public String toString() {
		return super.toString();
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
	    if (obj == null || getClass() != obj.getClass())
	    	return false;
		if (!super.equals(obj))
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return super.hashCode();
	}
}

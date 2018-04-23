package fr.viveris.s1pdgs.level0.wrapper.controller.dto;

/**
 * DTO object reprsenting an output of a job
 * @author Cyrielle Gailliard
 * @see JobDto
 *
 */
public class JobOutputDto {
	
	/**
	 * Family of the output
	 */
	private String family;
	
	/**
	 * The regular expression
	 */
	private String regexp;

	/**
	 * Default constructor
	 */
	public JobOutputDto() {
		
	}

	/**
	 * Constructor
	 * @param family
	 * @param regexp
	 */
	public JobOutputDto(String family, String regexp) {
		this();
		this.family = family;
		this.regexp = regexp;
	}

	/**
	 * @return the family
	 */
	public String getFamily() {
		return family;
	}

	/**
	 * @param family the family to set
	 */
	public void setFamily(String family) {
		this.family = family;
	}

	/**
	 * @return the regexp
	 */
	public String getRegexp() {
		return regexp;
	}

	/**
	 * @param regexp the regexp to set
	 */
	public void setRegexp(String regexp) {
		this.regexp = regexp;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "JobOutputDto [family=" + family + ", regexp=" + regexp + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((family == null) ? 0 : family.hashCode());
		result = prime * result + ((regexp == null) ? 0 : regexp.hashCode());
		return result;
	}

	/* (non-Javadoc)
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
		JobOutputDto other = (JobOutputDto) obj;
		if (family == null) {
			if (other.family != null)
				return false;
		} else if (!family.equals(other.family))
			return false;
		if (regexp == null) {
			if (other.regexp != null)
				return false;
		} else if (!regexp.equals(other.regexp))
			return false;
		return true;
	}

}

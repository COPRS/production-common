package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Objects;

/**
 * @author Viveris Technologies
 */
public class LevelJobOutputDto {

	/**
	 * Family of the output
	 */
	private String family;

	/**
	 * The regular expression
	 */
	private String regexp;

	/**
	 * Is a oqcCheck for the wrapper required for this output?
	 */
	private boolean oqcCheck = false;

	/**
	 * Default constructor
	 */
	public LevelJobOutputDto() {
		super();
	}

	/**
	 * Constructor
	 * 
	 * @param family
	 * @param regexp
	 */
	public LevelJobOutputDto(final String family, final String regexp) {
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
	public void setFamily(final String family) {
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
	public void setRegexp(final String regexp) {
		this.regexp = regexp;
	}

	public boolean isOqcCheck() {
		return oqcCheck;
	}

	public void setOqcCheck(boolean oqcCheck) {
		this.oqcCheck = oqcCheck;
	}

	/**
	 * to string
	 */
	@Override
	public String toString() {
		return String.format("{family: %s, regexp: %s, oqcCheck: %s}", family, regexp, oqcCheck);
	}

	/**
	 * hash code
	 */
	@Override
	public int hashCode() {
		return Objects.hash(family, regexp, oqcCheck);
	}

	/**
	 * Equals
	 */
	@Override
	public boolean equals(final Object obj) {
		boolean ret;
		if (this == obj) {
			ret = true;
		} else if (obj == null || getClass() != obj.getClass()) {
			ret = false;
		} else {
			LevelJobOutputDto other = (LevelJobOutputDto) obj;
			ret = Objects.equals(family, other.family) && Objects.equals(regexp, other.regexp)
					&& Objects.equals(oqcCheck, other.oqcCheck);
		}
		return ret;
	}

}

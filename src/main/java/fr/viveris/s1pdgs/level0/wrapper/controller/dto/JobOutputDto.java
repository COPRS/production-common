package fr.viveris.s1pdgs.level0.wrapper.controller.dto;

import java.util.Objects;

/**
 * DTO object reprsenting an output of a job
 * 
 * @author Viveris Technologies
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
        super();
    }

    /**
     * Constructor
     * 
     * @param family
     * @param regexp
     */
    public JobOutputDto(final String family, final String regexp) {
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
     * @param family
     *            the family to set
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
     * @param regexp
     *            the regexp to set
     */
    public void setRegexp(final String regexp) {
        this.regexp = regexp;
    }

    /**
     * to string
     */
    @Override
    public String toString() {
        return String.format("{family: %s, regexp: %s}", family, regexp);
    }

    /**
     * hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(family, regexp);
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
            JobOutputDto other = (JobOutputDto) obj;
            ret = Objects.equals(family, other.family)
                    && Objects.equals(regexp, other.regexp);
        }
        return ret;
    }

}

package esa.s1pdgs.cpoc.appcatalog.common.rest.model.job;

import java.util.Objects;

/**
 * Object for describing a file
 * 
 * @author Viveris Technologies
 */
public class AppDataJobFileDto {

    /**
     * Name of the file
     */
    private String filename;

    /**
     * Key in the OBS
     */
    private String keyObs;

    /**
     * @param filename
     */
    public AppDataJobFileDto() {
        super();
    }

    /**
     * @param filename
     */
    public AppDataJobFileDto(final String filename) {
        super();
        this.filename = filename;
    }

    /**
     * @param filename
     */
    public AppDataJobFileDto(final String filename, final String keyObs) {
        super();
        this.filename = filename;
        this.keyObs = keyObs;
    }

    /**
     * @return the filename
     */
    public String getFilename() {
        return filename;
    }

    /**
     * @param filename the filename to set
     */
    public void setFilename(final String filename) {
        this.filename = filename;
    }

    /**
     * @return the keyObs
     */
    public String getKeyObs() {
        return keyObs;
    }

    /**
     * @param keyObs
     *            the keyObs to set
     */
    public void setKeyObs(final String keyObs) {
        this.keyObs = keyObs;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("{filename: %s, keyObs: %s}", filename, keyObs);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(filename, keyObs);
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
            AppDataJobFileDto other = (AppDataJobFileDto) obj;
            ret = Objects.equals(filename, other.filename)
                    && Objects.equals(keyObs, other.keyObs);
        }
        return ret;
    }

}

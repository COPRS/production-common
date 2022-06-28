package esa.s1pdgs.cpoc.appcatalog;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Object for describing a file
 * 
 * @author Viveris Technologies
 */
public class AppDataJobFile implements Comparable<AppDataJobFile> {

    /**
     * Name of the file
     */
    private String fileName;

    /**
     * Key in the OBS
     */
    private String keyObs;

    private String startDate;

    private String endDate;
    
    private Date t0_pdgs_date;
    
    private Map<String,String> metadata;

    public AppDataJobFile(
    		final String fileName, 
    		final String keyObs, 
    		final String startDate, 
    		final String endDate,
    		final Date t0_pdgs_date,
    		final Map<String,String> metadata
    ) {
        this.fileName = fileName;
        this.keyObs = keyObs;
        this.startDate = startDate;
        this.endDate = endDate;
        this.t0_pdgs_date = t0_pdgs_date;
        this.metadata = metadata;
    }
    
    public AppDataJobFile() {
    	this(null,null,null,null,null,new LinkedHashMap<>());
    }

    public AppDataJobFile(final String fileName) {
        this(fileName, null,null,null,null,new LinkedHashMap<>());
    }
    
    public AppDataJobFile(final String fileName, final String keyObs) {
    	this(fileName, keyObs, null, null,null,new LinkedHashMap<>());
    }
    
    public AppDataJobFile(
    		final String fileName, 
    		final String keyObs, 
    		final String startDate, 
    		final String endDate,
    		final Date t0_pdgs_date
    ) {
    	this(fileName, keyObs, startDate, endDate, t0_pdgs_date, new LinkedHashMap<>());
    }    

    public AppDataJobFile(final AppDataJobFile other) {
    	this(other.fileName, other.keyObs, other.startDate, other.endDate, other.t0_pdgs_date, other.metadata);
    }

    /**
     * @return the filename
     */
    public String getFilename() {
        return fileName;
    }

    /**
     * @param filename the filename to set
     */
    public void setFilename(final String filename) {
        this.fileName = filename;
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

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(final String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(final String endDate) {
        this.endDate = endDate;
    }
    
    public Date getT0_pdgs_date() {
		return t0_pdgs_date;
	}

	public void setT0_pdgs_date(Date t0_pdgs_date) {
		this.t0_pdgs_date = t0_pdgs_date;
	}

	public Map<String, String> getMetadata() {
		return metadata;
	}

	public void setMetadata(final Map<String, String> metadata) {
		this.metadata = metadata;
	}

	/**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("{fileName: %s, keyObs: %s, startDate: %s, endDate: %s, t0_pdgs_date: %s, metadata:%s}", fileName, keyObs, startDate, endDate, t0_pdgs_date, metadata);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(fileName, keyObs, startDate, endDate, t0_pdgs_date, metadata);
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
            final AppDataJobFile other = (AppDataJobFile) obj;
            ret = Objects.equals(fileName, other.fileName)
                    && Objects.equals(keyObs, other.keyObs)
                    && Objects.equals(startDate, other.startDate)
                    && Objects.equals(endDate, other.endDate)
                    && Objects.equals(t0_pdgs_date, other.t0_pdgs_date)
                    && Objects.equals(metadata, other.metadata);
        }
        return ret;
    }

	@Override
	public int compareTo(final AppDataJobFile o) {
		return fileName.compareTo(o.fileName);
	}

}

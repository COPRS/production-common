/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    
    private Date t0PdgsDate;
    
    private Map<String,String> metadata;

    public AppDataJobFile(
    		final String fileName, 
    		final String keyObs, 
    		final String startDate, 
    		final String endDate,
    		final Date t0PdgsDate,
    		final Map<String,String> metadata
    ) {
        this.fileName = fileName;
        this.keyObs = keyObs;
        this.startDate = startDate;
        this.endDate = endDate;
        this.t0PdgsDate = t0PdgsDate;
        this.metadata = metadata;
    }
    
    public AppDataJobFile() {
    	this(null,null,null,null,null,new LinkedHashMap<>());
    }

    public AppDataJobFile(final String fileName) {
        this(fileName, null,null,null,null,new LinkedHashMap<>());
    }
    
    public AppDataJobFile(final String fileName, final String keyObs) {
    	this(fileName, keyObs, null, null, null, new LinkedHashMap<>());
    }
    
    public AppDataJobFile(final String fileName, final String keyObs, final Date t0PdgsDate) {
    	this(fileName, keyObs, null, null, t0PdgsDate, new LinkedHashMap<>());
    }
    
    public AppDataJobFile(
    		final String fileName, 
    		final String keyObs, 
    		final String startDate, 
    		final String endDate,
    		final Date t0PdgsDate
    ) {
    	this(fileName, keyObs, startDate, endDate, t0PdgsDate, new LinkedHashMap<>());
    }    

    public AppDataJobFile(final AppDataJobFile other) {
    	this(other.fileName, other.keyObs, other.startDate, other.endDate, other.t0PdgsDate, other.metadata);
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
    
    public Date getT0PdgsDate() {
		return t0PdgsDate;
	}

	public void setT0PdgsDate(Date t0PdgsDate) {
		this.t0PdgsDate = t0PdgsDate;
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
        return String.format("{fileName: %s, keyObs: %s, startDate: %s, endDate: %s, t0PdgsDate: %s, metadata:%s}", fileName, keyObs, startDate, endDate, t0PdgsDate, metadata);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(fileName, keyObs, startDate, endDate, t0PdgsDate, metadata);
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
                    && Objects.equals(t0PdgsDate, other.t0PdgsDate)
                    && Objects.equals(metadata, other.metadata);
        }
        return ret;
    }

	@Override
	public int compareTo(final AppDataJobFile o) {
		return fileName.compareTo(o.fileName);
	}

}

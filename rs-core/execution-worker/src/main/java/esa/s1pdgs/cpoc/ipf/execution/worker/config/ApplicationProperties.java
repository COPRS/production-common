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

package esa.s1pdgs.cpoc.ipf.execution.worker.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.ProductFamily;

/**
 * Application properties
 * 
 * @author Viveris Technologies
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "process")
public class ApplicationProperties {

	public static class TypeEstimationMapping {
		
		private String regexp;
		private int count;
		
		
		public String getRegexp() {
			return regexp;
		}
		public void setRegexp(String regexp) {
			this.regexp = regexp;
		}
		public int getCount() {
			return count;
		}
		public void setCount(int count) {
			this.count = count;
		}
	}
	
    /**
     * Application level
     */
    private ApplicationLevel level;

    /**
     * Timeout (in seconds) for waiting end of several tasks
     */
    private long tmProcAllTasksS;

    /**
     * Timeout (in seconds) for waiting end of one task
     */
    private long tmProcOneTaskS;

    /**
     * Timeout (in seconds) for waiting ending processing in case of forced stop
     */
    private long tmProcStopS;

    /**
     * Timeout (in seconds) for waiting stop ok
     */
    private long tmProcCheckStopS;

    /**
     * Batch size for upload
     */
    private int sizeBatchUpload;

    /**
     * Batch size for download
     */
    private int sizeBatchDownload;

    /**
     * Maximal number of loop when waiting for inputs downloading
     */
    private int wapNbMaxLoop;

    /**
     * Tempo between two loops when waiting for inputs downloading
     */
    private long wapTempoS;
   
    private String hostname;
    
    /**
     * Path to the working directory that should be used by the
     * wrapper. If this path is different than the one provided in the
     * job, it will be considered as error.
     */
    private String workingDir;
    
    private long thresholdEw;
    private long thresholdIw;
    private long thresholdSm;
    private long thresholdWv;
    
    private boolean oqcEnabled = false;
    private String oqcBinaryPath;
    private String oqcWorkingDir;
    private long oqcTimeoutInSeconds = 60;
    
	private List<String> plaintextTaskPatterns = new ArrayList<>();

	private String pathJobOrderXslt = "";
	
	/**
	 * Turn off workaround for S1 regarding ISIPs
	 */
	private boolean changeIsipToSafe = true;
	
	private boolean productTypeEstimationEnabled = false;
	
	private ProductFamily productTypeEstimationOutputFamily;
	
	private Map<String, TypeEstimationMapping> productTypeEstimatedCount = new HashMap<>();
	
	/**
	 * Workaround configuration for S1 L1 
	 */
	private boolean overwriteShell = false;

	/**
     * Default constructor
     */
    public ApplicationProperties() {
        super();
    }

    /**
     * @return the level
     */
    public ApplicationLevel getLevel() {
        return level;
    }

    /**
     * @param level
     *            the level to set
     */
    public void setLevel(final ApplicationLevel level) {
        this.level = level;
    }

    /**
     * @return the tmProcAllTasksS
     */
    public long getTmProcAllTasksS() {
        return tmProcAllTasksS;
    }

    /**
     * @param tmProcAllTasksS
     *            the tmProcAllTasksS to set
     */
    public void setTmProcAllTasksS(final long tmProcAllTasksS) {
        this.tmProcAllTasksS = tmProcAllTasksS;
    }

    /**
     * @return the tmProcOneTaskS
     */
    public long getTmProcOneTaskS() {
        return tmProcOneTaskS;
    }

    /**
     * @param tmProcOneTaskS
     *            the tmProcOneTaskS to set
     */
    public void setTmProcOneTaskS(final long tmProcOneTaskS) {
        this.tmProcOneTaskS = tmProcOneTaskS;
    }

    /**
     * @return the tmProcStopS
     */
    public long getTmProcStopS() {
        return tmProcStopS;
    }

    /**
     * @param tmProcStopS
     *            the tmProcStopS to set
     */
    public void setTmProcStopS(final long tmProcStopS) {
        this.tmProcStopS = tmProcStopS;
    }

    /**
     * @return the tmProcCheckStopS
     */
    public long getTmProcCheckStopS() {
        return tmProcCheckStopS;
    }

    /**
     * @param tmProcCheckStopS
     *            the tmProcCheckStopS to set
     */
    public void setTmProcCheckStopS(final long tmProcCheckStopS) {
        this.tmProcCheckStopS = tmProcCheckStopS;
    }

    /**
     * @return the sizeBatchS3Upload
     */
    public int getSizeBatchUpload() {
        return sizeBatchUpload;
    }

    /**
     * @param sizeBatchS3Upload
     *            the sizeBatchS3Upload to set
     */
    public void setSizeBatchUpload(final int sizeBatchS3Upload) {
        this.sizeBatchUpload = sizeBatchS3Upload;
    }

    /**
     * @return the sizeBatchS3Download
     */
    public int getSizeBatchDownload() {
        return sizeBatchDownload;
    }

    /**
     * @param sizeBatchS3Download
     *            the sizeBatchS3Download to set
     */
    public void setSizeBatchDownload(final int sizeBatchS3Download) {
        this.sizeBatchDownload = sizeBatchS3Download;
    }

    /**
     * @return the waitActiveProcessNbMaxLoop
     */
    public int getWapNbMaxLoop() {
        return wapNbMaxLoop;
    }

    /**
     * @param waitActiveProcessNbMaxLoop
     *            the waitActiveProcessNbMaxLoop to set
     */
    public void setWapNbMaxLoop(final int waitActiveProcessNbMaxLoop) {
        this.wapNbMaxLoop = waitActiveProcessNbMaxLoop;
    }

    /**
     * @return the waitActiveProcessTempoS
     */
    public long getWapTempoS() {
        return wapTempoS;
    }

    /**
     * @param waitActiveProcessTempoS
     *            the waitActiveProcessTempoS to set
     */
    public void setWapTempoS(final long waitActiveProcessTempoS) {
        this.wapTempoS = waitActiveProcessTempoS;
    }

	public String getHostname() {
		return hostname;
	}

	public void setHostname(final String hostname) {
		this.hostname = hostname;
	}

	public String getWorkingDir() {
		return workingDir;
	}

	public void setWorkingDir(final String workingDir) {
		this.workingDir = workingDir;
	}

	public long getThresholdEw() {
		return thresholdEw;
	}

	public void setThresholdEw(final long thresholdEw) {
		this.thresholdEw = thresholdEw;
	}

	public long getThresholdIw() {
		return thresholdIw;
	}

	public void setThresholdIw(final long thresholdIw) {
		this.thresholdIw = thresholdIw;
	}

	public long getThresholdSm() {
		return thresholdSm;
	}

	public void setThresholdSm(final long thresholdSm) {
		this.thresholdSm = thresholdSm;
	}

	public long getThresholdWv() {
		return thresholdWv;
	}

	public void setThresholdWv(final long thresholdWv) {
		this.thresholdWv = thresholdWv;
	}

	public long getOqcTimeoutInSeconds() {
		return oqcTimeoutInSeconds;
	}

	public void setOqcTimeoutInSeconds(final long oqcTimeoutInSeconds) {
		this.oqcTimeoutInSeconds = oqcTimeoutInSeconds;
	}

	public String getOqcBinaryPath() {
		return oqcBinaryPath;
	}

	public void setOqcBinaryPath(final String oqcBinaryPath) {
		this.oqcBinaryPath = oqcBinaryPath;
	}

	public String getOqcWorkingDir() {
		return oqcWorkingDir;
	}

	public void setOqcWorkingDir(final String oqcWorkingDir) {
		this.oqcWorkingDir = oqcWorkingDir;
	}

	public boolean isOqcEnabled() {
		return oqcEnabled;
	}

	public void setOqcEnabled(final boolean oqcEnabled) {
		this.oqcEnabled = oqcEnabled;
	}

	public List<String> getPlaintextTaskPatterns() {
		return plaintextTaskPatterns;
	}

	public void setPlaintextTaskPatterns(final List<String> plaintextTaskPatterns) {
		this.plaintextTaskPatterns = plaintextTaskPatterns;
	}
	
    public String getPathJobOrderXslt() {
		return pathJobOrderXslt;
	}

	public void setPathJobOrderXslt(String pathJobOrderXslt) {
		this.pathJobOrderXslt = pathJobOrderXslt;
	}
	
	public boolean isChangeIsipToSafe() {
		return changeIsipToSafe;
	}

	public void setChangeIsipToSafe(boolean changeIsipToSafe) {
		this.changeIsipToSafe = changeIsipToSafe;
	}

	public Map<String, TypeEstimationMapping> getProductTypeEstimatedCount() {
		return productTypeEstimatedCount;
	}

	public void setProductTypeEstimatedCount(Map<String, TypeEstimationMapping> productTypeEstimatedCount) {
		this.productTypeEstimatedCount = productTypeEstimatedCount;
	}

	public boolean isProductTypeEstimationEnabled() {
		return productTypeEstimationEnabled;
	}

	public void setProductTypeEstimationEnabled(boolean productTypeEstimationEnabled) {
		this.productTypeEstimationEnabled = productTypeEstimationEnabled;
	}

	public ProductFamily getProductTypeEstimationOutputFamily() {
		return productTypeEstimationOutputFamily;
	}

	public void setProductTypeEstimationOutputFamily(ProductFamily productTypeEstimationOutputFamily) {
		this.productTypeEstimationOutputFamily = productTypeEstimationOutputFamily;
	}

	public boolean getOverwriteShell() {
		return overwriteShell;
	}

	public void setOverwriteShell(boolean overwriteShell) {
		this.overwriteShell = overwriteShell;
	}
}

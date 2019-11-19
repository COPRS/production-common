package esa.s1pdgs.cpoc.ipf.preparation.worker.config;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.ApplicationMode;

/**
 * Extraction class of "process" configuration properties
 * 
 * @author Cyrielle Gailliard
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "process")
public class ProcessSettings {

    /**
     * Separator use to seperate the elements of a map in a string format
     */
    protected static final String MAP_ELM_SEP = "\\|\\|";

    /**
     * Separator use to separate the key and the value of a map element in a
     * string format
     */
    protected static final String MAP_KEY_VAL_SEP = ":";

    /**
     * Process level
     */
    private ApplicationLevel level;

    /**
     * Process level
     */
    private ApplicationMode mode;

    /**
     * Hostname
     */
    private String hostname;

    /**
     * Log level for the sdtout
     */
    private String loglevelstdout;

    /**
     * Log level for the stderr
     */
    private String loglevelstderr;

    /**
     * Processing station
     */
    private String processingstation;

    /**
     * Processing dynamic parameters: key = parameter name, value = parameter
     * value
     */
    private Map<String, String> params;

    /**
     * Regular expression: key = output file type, value = regular expression to
     * use for file name.<br/>
     * This is used to customize the way to match the outputs in the job
     */
    private Map<String, String> outputregexps;
    
    // 0 --> means that it least needs to be covered
    private int minSeaCoveragePercentage = 0;

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
     * @return the mode
     */
    public ApplicationMode getMode() {
        return mode;
    }

    /**
     * @param mode
     *            the mode to set
     */
    public void setMode(final ApplicationMode mode) {
        this.mode = mode;
    }

    /**
     * @return the hostname
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * @param hostname
     *            the hostname to set
     */
    public void setHostname(final String hostname) {
        this.hostname = hostname;
    }

    /**
     * @return the loglevelstdout
     */
    public String getLoglevelstdout() {
        return loglevelstdout;
    }

    /**
     * @param loglevelstdout
     *            the loglevelstdout to set
     */
    public void setLoglevelstdout(final String loglevelstdout) {
        this.loglevelstdout = loglevelstdout;
    }

    /**
     * @return the loglevelstderr
     */
    public String getLoglevelstderr() {
        return loglevelstderr;
    }

    /**
     * @param loglevelstderr
     *            the loglevelstderr to set
     */
    public void setLoglevelstderr(final String loglevelstderr) {
        this.loglevelstderr = loglevelstderr;
    }

    /**
     * @return the processingstation
     */
    public String getProcessingstation() {
        return processingstation;
    }

    /**
     * @param processingstation
     *            the processingstation to set
     */
    public void setProcessingstation(final String processingstation) {
        this.processingstation = processingstation;
    }

    /**
     * @return the params
     */
    public Map<String, String> getParams() {
        return params;
    }

    /**
     * @return the outputregexps
     */
    public Map<String, String> getOutputregexps() {
        return outputregexps;
    }

    /**
     * 
     * @param params
     */
    public void setParams(final Map<String, String> params) {
        this.params = params;
    }

    /**
     * 
     * @param outputregexps
     */
    public void setOutputregexps(final Map<String, String> outputregexps) {
        this.outputregexps = outputregexps;
    }

	public int getMinSeaCoveragePercentage() {
		return minSeaCoveragePercentage;
	}

	public void setMinSeaCoveragePercentage(int minSeaCoveragePercentage) {
		this.minSeaCoveragePercentage = minSeaCoveragePercentage;
	}
}

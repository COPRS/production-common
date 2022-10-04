package esa.s1pdgs.cpoc.preparation.worker.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.ApplicationMode;
import esa.s1pdgs.cpoc.common.ProductCategory;

/**
 * Extraction class of "process" configuration properties
 * 
 * @author Cyrielle Gailliard
 */
@Component
@Validated
@ConfigurationProperties(prefix = "process")
public class ProcessProperties {

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
    
    // only for logging/reporting purposes at the moment
    private String productType = "Product";
    
    // 0 --> means that it least needs to be covered
    private int minSeaCoveragePercentage = 0;
    
	private String seaCoverageCheckPattern = "$a"; // per default, don't match anything
	
	private String l0EwSlcCheckPattern = "$a";
	
	private String l0EwSlcTaskTableName = "EW_RAW__0_SLC";
	
    private String blacklistPattern;
    
    private long fixedDelayMs;
    
    private long initialDelayMs;
    
	private ProductCategory category;
	
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
    private Map<String, String> outputregexps = new HashMap<>();
    
	/**
	 * processing group to identify AppDataJobs in the JobGenerator. Is used
	 * additionally to the tasktableName to determine if a job is suitable for the
	 * generator. Needed if two separate Preparation Worker use the same TaskTable
	 * with different settings (ex. timeliness)
	 */
	private String processingGroup;
	
	private String l0EwSlcMaskFilePath;
	
	private String landMaskFilePath;

    public ApplicationLevel getLevel() {
        return level;
    }

    public void setLevel(final ApplicationLevel level) {
        this.level = level;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(final String hostname) {
        this.hostname = hostname;
    }

	public int getMinSeaCoveragePercentage() {
		return minSeaCoveragePercentage;
	}

	public void setMinSeaCoveragePercentage(final int minSeaCoveragePercentage) {
		this.minSeaCoveragePercentage = minSeaCoveragePercentage;
	}

	public String getSeaCoverageCheckPattern() {
		return seaCoverageCheckPattern;
	}

	public void setSeaCoverageCheckPattern(final String seaCoverageCheckPattern) {
		this.seaCoverageCheckPattern = seaCoverageCheckPattern;
	}
	
	public String getL0EwSlcCheckPattern() {
		return l0EwSlcCheckPattern;
	}

	public void setL0EwSlcCheckPattern(String l0EwSlcCheckPattern) {
		this.l0EwSlcCheckPattern = l0EwSlcCheckPattern;
	}
	
	public String getL0EwSlcTaskTableName() {
		return l0EwSlcTaskTableName;
	}

	public void setL0EwSlcTaskTableName(String l0EwSlcTaskTableName) {
		this.l0EwSlcTaskTableName = l0EwSlcTaskTableName;
	}

    public String getBlacklistPattern() {
		return blacklistPattern;
	}

	public void setBlacklistPattern(final String blacklistPattern) {
		this.blacklistPattern = blacklistPattern;
	}

	public long getFixedDelayMs() {
		return fixedDelayMs;
	}

	public void setFixedDelayMs(final long fixedDelayMs) {
		this.fixedDelayMs = fixedDelayMs;
	}

	public long getInitialDelayMs() {
		return initialDelayMs;
	}

	public void setInitialDelayMs(final long initialDelayMs) {
		this.initialDelayMs = initialDelayMs;
	}

	public ProductCategory getCategory() {
		return category;
	}

	public void setCategory(final ProductCategory category) {
		this.category = category;
	}

	public String getProductType() {
		return productType;
	}

	public void setProductType(final String productType) {
		this.productType = productType;
	}

	public ApplicationMode getMode() {
		return mode;
	}

	public void setMode(ApplicationMode mode) {
		this.mode = mode;
	}

	public String getLoglevelstdout() {
		return loglevelstdout;
	}

	public void setLoglevelstdout(String loglevelstdout) {
		this.loglevelstdout = loglevelstdout;
	}

	public String getLoglevelstderr() {
		return loglevelstderr;
	}

	public void setLoglevelstderr(String loglevelstderr) {
		this.loglevelstderr = loglevelstderr;
	}

	public String getProcessingstation() {
		return processingstation;
	}

	public void setProcessingstation(String processingstation) {
		this.processingstation = processingstation;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public void setParams(Map<String, String> params) {
		this.params = params;
	}

	public Map<String, String> getOutputregexps() {
		return outputregexps;
	}

	public void setOutputregexps(Map<String, String> outputregexps) {
		this.outputregexps = outputregexps;
	}

	public String getProcessingGroup() {
		return processingGroup;
	}

	public void setProcessingGroup(String processingGroup) {
		this.processingGroup = processingGroup;
	}

	public String getL0EwSlcMaskFilePath() {
		return l0EwSlcMaskFilePath;
	}

	public void setL0EwSlcMaskFilePath(String ewSlcMaskFilePath) {
		this.l0EwSlcMaskFilePath = ewSlcMaskFilePath;
	}

	public String getLandMaskFilePath() {
		return landMaskFilePath;
	}

	public void setLandMaskFilePath(String landMaskFilePath) {
		this.landMaskFilePath = landMaskFilePath;
	}
	
	
}

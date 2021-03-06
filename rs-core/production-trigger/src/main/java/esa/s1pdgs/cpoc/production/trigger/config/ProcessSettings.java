package esa.s1pdgs.cpoc.production.trigger.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.ProductCategory;

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
}

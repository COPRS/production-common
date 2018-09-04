package esa.s1pdgs.cpoc.appcatalog.server.job;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "jobs")
public class JobsProperties {

    private int cleaningJobsTerminatedFixedRateMs;
    
    private int cleaningJobsInvalidFixedRateMs;
    
    private JobPropertiesPerCategory levelProducts;
    
    private JobPropertiesPerCategory edrsSessions;
    
    /**
     * 
     */
    public JobsProperties() {
        super();
    }

    /**
     * @return the cleaningJobsTerminatedFixedRateMs
     */
    public int getCleaningJobsTerminatedFixedRateMs() {
        return cleaningJobsTerminatedFixedRateMs;
    }

    /**
     * @param cleaningJobsTerminatedFixedRateMs the cleaningJobsTerminatedFixedRateMs to set
     */
    public void setCleaningJobsTerminatedFixedRateMs(
            int cleaningJobsTerminatedFixedRateMs) {
        this.cleaningJobsTerminatedFixedRateMs = cleaningJobsTerminatedFixedRateMs;
    }

    /**
     * @return the cleaningJobsInvalidFixedRateMs
     */
    public int getCleaningJobsInvalidFixedRateMs() {
        return cleaningJobsInvalidFixedRateMs;
    }

    /**
     * @param cleaningJobsInvalidFixedRateMs the cleaningJobsInvalidFixedRateMs to set
     */
    public void setCleaningJobsInvalidFixedRateMs(
            int cleaningJobsInvalidFixedRateMs) {
        this.cleaningJobsInvalidFixedRateMs = cleaningJobsInvalidFixedRateMs;
    }

    /**
     * @return the levelProducts
     */
    public JobPropertiesPerCategory getLevelProducts() {
        return levelProducts;
    }

    /**
     * @param levelProducts the levelProducts to set
     */
    public void setLevelProducts(JobPropertiesPerCategory levelProducts) {
        this.levelProducts = levelProducts;
    }

    /**
     * @return the edrsSessions
     */
    public JobPropertiesPerCategory getEdrsSessions() {
        return edrsSessions;
    }

    /**
     * @param edrsSessions the edrsSessions to set
     */
    public void setEdrsSessions(JobPropertiesPerCategory edrsSessions) {
        this.edrsSessions = edrsSessions;
    }

   public static class JobPropertiesPerCategory {

        /**
         * Maximal job age per status
         */
        private Map<String, Long> maxAgeJobMs;

        /**
         * 
         */
        public JobPropertiesPerCategory() {
            super();
        }

        /**
         * @return the maxAgeJobMs
         */
        public Map<String, Long> getMaxAgeJobMs() {
            return maxAgeJobMs;
        }

        /**
         * @param maxAgeJobMs the maxAgeJobMs to set
         */
        public void setMaxAgeJobMs(Map<String, Long> maxAgeJobMs) {
            this.maxAgeJobMs = maxAgeJobMs;
        }
        
    }
}

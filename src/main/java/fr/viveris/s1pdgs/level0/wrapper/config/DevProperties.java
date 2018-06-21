package fr.viveris.s1pdgs.level0.wrapper.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * List the properties only used for developments
 * 
 * @author Viveris Technologies
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "dev")
public class DevProperties {

    /**
     * Activation of each step of job processing
     */
    private Map<String, Boolean> stepsActivation;

    /**
     * Constructors
     */
    public DevProperties() {
        stepsActivation = new HashMap<>();
    }

    /**
     * @return the devStepsActivation
     */
    public Map<String, Boolean> getStepsActivation() {
        return stepsActivation;
    }

    /**
     * @param devStepsActivation
     *            the devStepsActivation to set
     */
    public void setStepsActivation(final Map<String, Boolean> stepsActivation) {
        this.stepsActivation = stepsActivation;
    }

}

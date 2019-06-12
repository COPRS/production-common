package esa.s1pdgs.cpoc.errorrepo;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration of the application: product cagtegories
 * 
 * @author Viveris Technologies
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "application")
public class ApplicationProperties {

    private String hostname;
    
    /**
     * Constructor
     */
    public ApplicationProperties() {
        super();
    }

    /**
     * @return the hostname
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * @param hostname the hostname to set
     */
    public void setHostname(final String hostname) {
        this.hostname = hostname;
    }
}

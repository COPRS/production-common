package esa.s1pdgs.cpoc.ipf.execution.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;


/**
 * L0 Wrapper application
 * 
 * @author Olivier Bex-Chauvet
 *
 */
@SpringBootApplication
@EnableConfigurationProperties
@ComponentScan("esa.s1pdgs.cpoc")
public class Application {
	
    /**
     * Main application
     * @param args
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class);
        
        
    }

}

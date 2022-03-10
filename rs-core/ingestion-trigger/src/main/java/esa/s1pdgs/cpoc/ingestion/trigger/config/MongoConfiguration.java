package esa.s1pdgs.cpoc.ingestion.trigger.config;

import java.util.List;
import java.util.StringJoiner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

/**
 * Configuration for the MongoDB client
 * 
 * @author Viveris Technologies
 */
@Configuration
@ConfigurationProperties("mongo")
public class MongoConfiguration {

    /**
     * Logger
     */
    private static final Logger LOGGER =
            LogManager.getLogger(MongoConfiguration.class);

    private List<String> host;

    private int port;
    
    private String database;

    private String username;

    private String password;

    @Bean
    public MongoClient mongoClient() {
    	LOGGER.info("New constructor");
        StringJoiner stringJoinerHosts = new StringJoiner(",");       
        host.forEach(each -> {
        	stringJoinerHosts.add(each + ":" + port);
        });
        String credentials = "".equals(username) ? "" : username + ":" + password + "@";
        return MongoClients.create("mongodb://" + credentials + stringJoinerHosts.toString() + "/" + database + "?uuidRepresentation=STANDARD");
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoClient(), database);
    }  
}

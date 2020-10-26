package esa.s1pdgs.cpoc.mdc.timer.config;

import java.util.List;
import java.util.StringJoiner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
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
public class MongoConfiguration {

    /**
     * Logger
     */
    private static final Logger LOGGER =
            LogManager.getLogger(MongoConfiguration.class);

    @Value("${mongodb.host}")
    private List<String> mongoDBHost;

    @Value("${mongodb.port}")
    private int mongoDBPort;
    
    @Value("${mongodb.database}")
    private String mongoDBDatabase;

    @Value("${mongodb.username:}")
    private String mongoUsername;

    @Value("${mongodb.password:}")
    private String mongoPassword;

    @Bean
    public MongoClient mongoClient() {
    	LOGGER.info("New constructor");
        StringJoiner stringJoinerHosts = new StringJoiner(",");       
        mongoDBHost.forEach(host -> {
        	stringJoinerHosts.add(host + ":" + mongoDBPort);
        });
        String credentials = "".equals(mongoUsername) ? "" : mongoUsername + ":" + mongoPassword + "@";
        return MongoClients.create("mongodb://" + credentials + stringJoinerHosts.toString() + "/" + mongoDBDatabase + "?uuidRepresentation=STANDARD");
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoClient(), mongoDBDatabase);
    }  
}

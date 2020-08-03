package esa.s1pdgs.cpoc.appcatalog.server.config;

import java.util.List;
import java.util.StringJoiner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

/**
 * Configuration for the MongoDB client
 * 
 * @author Viveris Technologies
 */
@Configuration
@EnableMongoRepositories
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
    
    public @Bean MongoClient mongoClient() {
        LOGGER.info("New constructor");
        StringJoiner stringJoinerHosts = new StringJoiner(",");       
        mongoDBHost.forEach(host -> {
        	stringJoinerHosts.add(host + ":" + mongoDBPort);
        });
        return MongoClients.create("mongodb://" + stringJoinerHosts.toString() + "/?uuidRepresentation=STANDARD");
    }

    public @Bean MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoClient(), mongoDBDatabase);
    }  

}

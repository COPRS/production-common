/**
 * 
 */
package esa.s1pdgs.cpoc.appcatalog.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.mongodb.MongoClient;

/**
 * Configuration for the MongoDB client
 * 
 * @author Viveris Technologies
 */
@Configuration
@EnableMongoRepositories
public class MongoConfiguration {

    @Value("${mongodb.host}")
    private String mongoDBHost;

    @Value("${mongodb.port}")
    private int mongoDBPort;
    
    @Value("${mongodb.database}")
    private String mongoDBDatabase;
    
    public @Bean MongoClient mongoClient() {
        MongoClient client = MongoClient(mongoDBHost, mongoDBPort);
        return new MongoClient(mongoDBHost, mongoDBPort);
    }

    public @Bean MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoClient(), mongoDBDatabase);
    }  

}

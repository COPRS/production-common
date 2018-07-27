/**
 * 
 */
package esa.s1pdgs.cpoc.appcatalog.config;


import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

/**
 * Configuration for the MongoDB client
 * 
 * @author Viveris Technologies
 */
@Configuration
@EnableMongoRepositories
public class MongoConfiguration {

    @Value("${mongodb.host}")
    private List<String> mongoDBHost;

    @Value("${mongodb.port}")
    private int mongoDBPort;
    
    @Value("${mongodb.database}")
    private String mongoDBDatabase;
    
    public @Bean MongoClient mongoClient() {
        List<ServerAddress> servers = new ArrayList<>();
        mongoDBHost.forEach(host -> {
            servers.add(new ServerAddress(host, mongoDBPort));
        });
        return new MongoClient(servers);
    }

    public @Bean MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoClient(), mongoDBDatabase);
    }  

}

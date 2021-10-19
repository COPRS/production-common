package esa.s1pdgs.cpoc.appcatalog.server.config;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static org.springframework.util.StringUtils.isEmpty;

import java.util.List;

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
    
    @Value("${mongodb.username:}")
    private String mongoUsername;

    @Value("${mongodb.password:}")
    private String mongoPassword;

    @Value("${mongodb.connectTimeoutMs:60000}")
    private int connectTimeoutMs;

    @Value("${mongodb.connectTimeoutMs:60000}")
    private int socketTimeoutMS;

    public @Bean MongoClient mongoClient() {
        final String hosts = mongoDBHost.stream().map(host -> host + ":" + mongoDBPort).collect(joining(","));
        String credentials = isEmpty(mongoUsername) ? "" : format("%s:%s@", mongoUsername, mongoPassword);

        LOGGER.info("Creating mongo client for hosts {} to database {}", hosts, mongoDBDatabase);
        return MongoClients.create(format("mongodb://%s%s/%s?uuidRepresentation=STANDARD&connectTimeoutMS=%s&socketTimeoutMS=%s",
                credentials,
                hosts,
                mongoDBDatabase,
                connectTimeoutMs,
                socketTimeoutMS));
    }

    public @Bean MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoClient(), mongoDBDatabase);
    }  

}

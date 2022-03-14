package esa.s1pdgs.cpoc.ingestion.trigger.config;

import java.util.List;
import java.util.StringJoiner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

/**
 * Configuration for the MongoDB client
 * 
 * @author Viveris Technologies
 */
@Component
@Validated
@ConfigurationProperties(prefix = "mongo")
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
    
    public List<String> getHost() {
		return host;
	}

	public void setHost(List<String> host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}

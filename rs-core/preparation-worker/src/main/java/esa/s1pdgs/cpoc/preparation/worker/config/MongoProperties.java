package esa.s1pdgs.cpoc.preparation.worker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for the mongodb connection
 */
@Component
@Validated
@ConfigurationProperties(prefix = "mongodb")
public class MongoProperties {

    private String host;

    private int port;
    
    private String database;

    private String username;

    private String password;
    
    private int connectTimeoutMs = 60000;

    private int socketTimeoutMS = 60000;
    
    public String getHost() {
		return host;
	}

	public void setHost(String host) {
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

	public int getConnectTimeoutMs() {
		return connectTimeoutMs;
	}

	public void setConnectTimeoutMs(int connectTimeoutMs) {
		this.connectTimeoutMs = connectTimeoutMs;
	}

	public int getSocketTimeoutMS() {
		return socketTimeoutMS;
	}

	public void setSocketTimeoutMS(int socketTimeoutMS) {
		this.socketTimeoutMS = socketTimeoutMS;
	}
}

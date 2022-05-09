package esa.s1pdgs.cpoc.preparation.worker.config;

import static java.lang.String.format;

import java.util.StringJoiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.util.Assert;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

/**
 * Configuration class for the mongodb connection. Uses {@link MongoProperties}
 * to create MongoClient instance.
 */
@Configuration
public class MongoConfiguration {

	private static final Logger LOG = LoggerFactory.getLogger(MongoConfiguration.class);

	@Autowired
	private MongoProperties mongoProperties;

	@Bean
	public MongoClient mongoClient() {
		Assert.notNull(mongoProperties.getHost(), "Host is required for mongo connection configuration");
		Assert.notNull(mongoProperties.getPort(), "Port is required for mongo connection configuration");
		Assert.notNull(mongoProperties.getDatabase(), "Database is required for mongo connection configuration");

		LOG.info("Create new mongo client");
		StringJoiner stringJoinerHosts = new StringJoiner(",");
		String[] hosts = mongoProperties.getHost().split(",");

		for (String each : hosts) {
			stringJoinerHosts.add(each + ":" + mongoProperties.getPort());
		}

		String credentials = "".equals(mongoProperties.getUsername()) ? ""
				: mongoProperties.getUsername() + ":" + mongoProperties.getPassword() + "@";

		LOG.info("Creating mongo client for hosts {} to database {}", hosts, mongoProperties.getDatabase());
		return MongoClients
				.create(format("mongodb://%s%s/%s?uuidRepresentation=STANDARD&connectTimeoutMS=%s&socketTimeoutMS=%s",
						credentials, hosts, mongoProperties.getDatabase(), mongoProperties.getConnectTimeoutMs(),
						mongoProperties.getSocketTimeoutMS()));
	}

	@Bean
	public MongoTemplate mongoTemplate() {
		return new MongoTemplate(mongoClient(), mongoProperties.getDatabase());
	}
}

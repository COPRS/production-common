package esa.s1pdgs.cpoc.ingestion.trigger.config;

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

@Configuration
public class MongoConfiguration {

	private static final Logger LOG = LoggerFactory.getLogger(MongoConfiguration.class);

	@Autowired
	private IngestionTriggerConfigurationProperties properties;

	@Bean
	public MongoClient mongoClient() {		
		MongoProperties mongoProperties = properties.getMongo();
		
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
		return MongoClients.create("mongodb://" + credentials + stringJoinerHosts.toString() + "/"
				+ mongoProperties.getDatabase() + "?uuidRepresentation=STANDARD");
	}

	@Bean
	public MongoTemplate mongoTemplate() {
		MongoProperties mongoProperties = properties.getMongo();
		
		return new MongoTemplate(mongoClient(), mongoProperties.getDatabase());
	}
}

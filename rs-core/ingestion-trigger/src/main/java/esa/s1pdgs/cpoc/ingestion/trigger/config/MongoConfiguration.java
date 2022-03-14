package esa.s1pdgs.cpoc.ingestion.trigger.config;

import java.util.List;
import java.util.StringJoiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

@Configuration
public class MongoConfiguration {

	private static final Logger LOG = LoggerFactory.getLogger(MongoConfiguration.class);

	@Autowired
	private MongoProperties mongoProperties;

	@Bean
	public MongoClient mongoClient() {
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
		return new MongoTemplate(mongoClient(), mongoProperties.getDatabase());
	}
}

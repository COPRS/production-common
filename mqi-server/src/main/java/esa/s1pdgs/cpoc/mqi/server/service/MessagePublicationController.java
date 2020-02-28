package esa.s1pdgs.cpoc.mqi.server.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiCategoryNotAvailable;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiPublicationError;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiRouteNotAvailable;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.server.config.ApplicationProperties;
import esa.s1pdgs.cpoc.mqi.server.config.ApplicationProperties.ProductCategoryProperties;
import esa.s1pdgs.cpoc.mqi.server.config.ApplicationProperties.ProductCategoryPublicationProperties;
import esa.s1pdgs.cpoc.mqi.server.converter.XmlConverter;
import esa.s1pdgs.cpoc.mqi.server.publication.kafka.producer.GenericProducer;
import esa.s1pdgs.cpoc.mqi.server.publication.routing.DefaultRoute;
import esa.s1pdgs.cpoc.mqi.server.publication.routing.Route;
import esa.s1pdgs.cpoc.mqi.server.publication.routing.Routing;

/**
 * Manager of consumers
 * 
 * @author Viveris Technologies
 */
@Controller
public class MessagePublicationController {

	private static final Logger LOGGER = LogManager.getLogger(MessagePublicationController.class);

	private final Map<ProductCategory, Routing> routing;
	private final ApplicationProperties appProperties;
	private final XmlConverter xmlConverter;	
	private final GenericProducer kafkaProducer;

	@Autowired
	public MessagePublicationController(
			final ApplicationProperties appProperties,
			final XmlConverter xmlConverter,
			final GenericProducer kafkaProducer
	) {
		this.routing = new HashMap<>();
		this.appProperties = appProperties;
		this.xmlConverter = xmlConverter;
		this.kafkaProducer = kafkaProducer;
	}

	@PostConstruct
	public void initialize() throws IOException, JAXBException {
		for (final Map.Entry<ProductCategory, ProductCategoryProperties> entry : appProperties.getProductCategories().entrySet()) {					
			final ProductCategory cat = entry.getKey();		
			final ProductCategoryProperties catProp = entry.getValue();
			final ProductCategoryPublicationProperties prop = catProp.getPublication();

			if (prop.isEnable()) {
				// Create routing map
				LOGGER.info("Creating routing map for category {}", cat);
				final Routing routingEntry = (Routing) xmlConverter.convertFromXMLToObject(prop.getRoutingFile());
				LOGGER.debug("Routing for category {} is: {}", cat, routingEntry);
				routing.put(cat, routingEntry);
			}
		}
	}


	/**
	 * Publish a message of a given category
	 * 
	 * @param category
	 * @param dto
	 * @throws MqiPublicationError
	 * @throws MqiCategoryNotAvailable
	 * @throws MqiRouteNotAvailable
	 */
	public void publish(ProductCategory category, AbstractMessage dto, String inputKey, String outputKey)
			throws MqiPublicationError, MqiCategoryNotAvailable, MqiRouteNotAvailable {		
		final String topic = getTopic(category, dto.getProductFamily(), inputKey, outputKey);
		LOGGER.debug("== Publishing message {}, to topic {} ", dto, topic);
		kafkaProducer.send(topic, dto);
	}

	/**
	 * Get the topic to use for publication according the routing map
	 * 
	 * @param category
	 * @param family
	 * @return
	 * @throws MqiCategoryNotAvailable
	 * @throws MqiRouteNotAvailable
	 */
	final String getTopic(final ProductCategory category, final ProductFamily family, final String inputKey,
			final String outputKey) throws MqiCategoryNotAvailable, MqiRouteNotAvailable {		
		final Routing thisRouting = routing.get(category);
		
		LOGGER.debug("getTopic inpputKey={}, outputKey={}, family={}, category={}", inputKey, outputKey, family, category);
		
		if (thisRouting == null) {
			throw new MqiCategoryNotAvailable(category, "publisher");
		}
		final Route route = thisRouting.getRoute(inputKey, outputKey);
		if (route != null) {
			final String topic = route.getRouteTo().getTopic();
			LOGGER.debug("Got topic '{}' from Route for (category: {}, inputKey: {}, outputKey: {}): {}", 
					topic, category, inputKey, outputKey, route);
			return topic;
		}
		
		final DefaultRoute defaultRoute = thisRouting.getDefaultRoute(family);
		if (defaultRoute != null) {
			final String topic = defaultRoute.getRouteTo().getTopic();
			LOGGER.debug("Got topic '{}' from DefaultRoute for (category: {}, family: {}): {}", 
					topic, category, family, defaultRoute);
			return defaultRoute.getRouteTo().getTopic();
		}
		throw new MqiRouteNotAvailable(category, family);
	}
}

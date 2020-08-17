package esa.s1pdgs.cpoc.production.trigger.taskTableMapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Assert;

import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.xml.XmlConverter;
import esa.s1pdgs.cpoc.xml.model.tasktable.routing.LevelProductsRoute;
import esa.s1pdgs.cpoc.xml.model.tasktable.routing.LevelProductsRouting;

public class RoutingBasedTasktableMapper implements TasktableMapper {	
	public static final class Factory {
	    private final XmlConverter converter;
	    private final String file;
	    private final Function<AppDataJobProduct, String> keyFunction;
	    
	    private final Map<Pattern, String> routingMap = new LinkedHashMap<>();

		public Factory(
				final XmlConverter xmlConverter, 
				final String file, 
				final Function<AppDataJobProduct, String> keyFunction
		) {
			this.converter = xmlConverter;
			this.file = file;
			this.keyFunction = keyFunction;
		}
				
		public final RoutingBasedTasktableMapper newMapper() {			
	        // Init the routing map from XML file located in the task table folder
            final LevelProductsRouting routing = parse();
            
            Assert.isTrue(routing != null, String.format("Routing of '%s' is null", file));
            Assert.isTrue(routing.getRoutes() != null, String.format("Routing of '%s' has no routes", file));

            for (final LevelProductsRoute route : routing.getRoutes()) {
            	final Pattern key = routeKeyPatternOf(route);
            	final String ttName = targetTasktableOf(route);
            	LOGGER.debug("-> adding tasktable route for {} -> {}", key, ttName);
            	routingMap.put(key, ttName);
            }
            return new RoutingBasedTasktableMapper(keyFunction, routingMap);
		}
		
		private Pattern routeKeyPatternOf(final LevelProductsRoute route) {
			return Pattern.compile(routeKeyOf(route), Pattern.CASE_INSENSITIVE);
		}
		
		private final String routeKeyOf(final LevelProductsRoute route) {
			return route.getRouteFrom().getAcquisition() + "_" + route.getRouteFrom().getSatelliteId();
		}
		
		private final String targetTasktableOf(final LevelProductsRoute route) {
			final List<String> res = route.getRouteTo().getTaskTables();
			Assert.isTrue(res.size() == 1, "One tasktable expected");
			return res.get(0);
		}
		
		private final LevelProductsRouting parse() {
			try {
				return (LevelProductsRouting) converter.convertFromXMLToObject(file);
			} 
			catch (IOException | JAXBException e) {
	            throw new IllegalStateException(
	            		String.format("Cannot parse routing XML file located in %s", file),
	                    e
	            );
			}
		}
	}
	private static final Logger LOGGER = LogManager.getLogger(RoutingBasedTasktableMapper.class);
	
	private final Function<AppDataJobProduct, String> keyFunction;
    private final Map<Pattern, String> routingMap;

	public RoutingBasedTasktableMapper(
			final Function<AppDataJobProduct, String> keyFunction,
			final Map<Pattern, String> routingMap		
	) {
		this.keyFunction = keyFunction;
		this.routingMap = routingMap;
	}

	@Override
	public final List<String> tasktableFor(final AppDataJobProduct product) {
        final String key = keyFunction.apply(product);

        LOGGER.debug("Searching tasktable for {}", key);
        List<String> taskTableHolder = new ArrayList<>();
        
        for (final Map.Entry<Pattern, String> entry : routingMap.entrySet()) {
			if (entry.getKey().matcher(key).matches()) {	
				LOGGER.info("Got tasktable {} for {}", entry.getValue(), key);   
				taskTableHolder.add(entry.getValue());
			}
		}        
        
        if (taskTableHolder.isEmpty())
        {
            throw new IllegalArgumentException(
            		String.format(
            				"No tasktable found for AppDataJobProduct %s, key: %s in: %s", 
            				product.getMetadata().get("productName"),
            				key,
            				routingMap
            		)
            );
        }
        
        return taskTableHolder;
	}
}

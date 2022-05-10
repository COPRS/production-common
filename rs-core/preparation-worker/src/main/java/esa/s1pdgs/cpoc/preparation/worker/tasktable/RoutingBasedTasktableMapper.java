package esa.s1pdgs.cpoc.preparation.worker.tasktable;

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
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.util.CatalogEventAdapter;
import esa.s1pdgs.cpoc.xml.XmlConverter;
import esa.s1pdgs.cpoc.xml.model.tasktable.routing.LevelProductsRoute;
import esa.s1pdgs.cpoc.xml.model.tasktable.routing.LevelProductsRouting;

public class RoutingBasedTasktableMapper implements TasktableMapper {	
	public static final class Factory {
	    private final XmlConverter converter;
	    private final String file;
	    private final Function<AppDataJobProduct, String> keyFunction;
	    
	    private final Map<Pattern, List<String>> routingMap = new LinkedHashMap<>();

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
            	final List<String> ttName = route.getRouteTo().getTaskTables();
            	LOGGER.debug("-> adding tasktable route for {} -> {}", key, ttName);
            	final List<String> value = routingMap.getOrDefault(key, new ArrayList<>());
            	value.addAll(ttName);
            	routingMap.put(key, value);
            }
            return new RoutingBasedTasktableMapper(keyFunction, routingMap);
		}
		
		private Pattern routeKeyPatternOf(final LevelProductsRoute route) {
			return Pattern.compile(routeKeyOf(route), Pattern.CASE_INSENSITIVE);
		}
		
		private final String routeKeyOf(final LevelProductsRoute route) {
			return route.getRouteFrom().getAcquisition() + "_" + route.getRouteFrom().getSatelliteId();
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
    private final Map<Pattern, List<String>> routingMap;

	public RoutingBasedTasktableMapper(
			final Function<AppDataJobProduct, String> keyFunction,
			final Map<Pattern, List<String>> routingMap		
	) {
		this.keyFunction = keyFunction;
		this.routingMap = routingMap;
	}

	@Override
	public final List<String> tasktableFor(final CatalogEvent product) {
        final String key = keyFunction.apply(newProductFor(product));

        LOGGER.debug("Searching tasktable for {}", key);
        final List<String> taskTableHolder = new ArrayList<>();
        
        for (final Map.Entry<Pattern, List<String>> entry : routingMap.entrySet()) {
			if (entry.getKey().matcher(key).matches()) {	
				LOGGER.info("Got tasktable {} for {}", entry.getValue(), key);   
				taskTableHolder.addAll(entry.getValue());
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
	
	// FIXME check if filtering can be applied directly on metadata of catalog event to avoid this mapping
	private final AppDataJobProduct newProductFor(final CatalogEvent event) {
        final AppDataJobProduct productDto = new AppDataJobProduct();
        
		final CatalogEventAdapter eventAdapter = new CatalogEventAdapter(event);		
		productDto.getMetadata().put("productName", event.getProductName());
		productDto.getMetadata().put("productType", event.getProductType());
		productDto.getMetadata().put("satelliteId", eventAdapter.satelliteId());
		productDto.getMetadata().put(MissionId.FIELD_NAME, eventAdapter.missionId());
		productDto.getMetadata().put("processMode", eventAdapter.processMode());
		productDto.getMetadata().put("startTime", eventAdapter.startTime());
		productDto.getMetadata().put("stopTime", eventAdapter.stopTime());     
		productDto.getMetadata().put("timeliness", eventAdapter.timeliness());
		productDto.getMetadata().put("acquistion", eventAdapter.swathType());
        return productDto;
	}
}

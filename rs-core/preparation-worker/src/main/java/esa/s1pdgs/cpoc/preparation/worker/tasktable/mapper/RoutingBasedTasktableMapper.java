package esa.s1pdgs.cpoc.preparation.worker.tasktable.mapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Assert;

import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.util.CatalogEventAdapter;

public class RoutingBasedTasktableMapper implements TasktableMapper {
	public static final class Factory {
		private final Map<String, String> inputMap;
		private final Function<AppDataJobProduct, String> keyFunction;

		private final Map<Pattern, List<String>> routingMap = new LinkedHashMap<>();

		public Factory(final Map<String, String> inputMap, final Function<AppDataJobProduct, String> keyFunction) {
			this.inputMap = inputMap;
			this.keyFunction = keyFunction;
		}

		public final RoutingBasedTasktableMapper newMapper() {
			Assert.isTrue(!inputMap.isEmpty(),
					"No routing found. Please use the parameter 'tasktable.routing' to configure mappings between input events and tasktables.");

			for (Entry<String, String> entry : inputMap.entrySet()) {
				final Pattern key = routeSourceOf(entry);
				final List<String> ttNames = routeDestinationOf(entry);
				LOGGER.debug("-> adding tasktable route for {} -> {}", key, ttNames);
				routingMap.put(key, ttNames);
			}

			return new RoutingBasedTasktableMapper(keyFunction, routingMap);
		}

		private Pattern routeSourceOf(final Entry<String, String> route) {
			return Pattern.compile(route.getKey(), Pattern.CASE_INSENSITIVE);
		}

		private List<String> routeDestinationOf(final Entry<String, String> route) {
			return new ArrayList<String>(Arrays.asList(route.getValue().split(",")));
		}
	}

	private static final Logger LOGGER = LogManager.getLogger(RoutingBasedTasktableMapper.class);

	private final Function<AppDataJobProduct, String> keyFunction;
	private final Map<Pattern, List<String>> routingMap;

	public RoutingBasedTasktableMapper(final Function<AppDataJobProduct, String> keyFunction,
			final Map<Pattern, List<String>> routingMap) {
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

		if (taskTableHolder.isEmpty()) {
			throw new IllegalArgumentException(
					String.format("No tasktable found for AppDataJobProduct %s, key: %s in: %s",
							product.getMetadata().get("productName"), key, routingMap));
		}

		return taskTableHolder;
	}

	// FIXME check if filtering can be applied directly on metadata of catalog event
	// to avoid this mapping
	private final AppDataJobProduct newProductFor(final CatalogEvent event) {
		final AppDataJobProduct productDto = new AppDataJobProduct();

		final CatalogEventAdapter eventAdapter = new CatalogEventAdapter(event);
		productDto.getMetadata().put("productName", event.getProductName());
		productDto.getMetadata().put("productType", event.getProductType());
		productDto.getMetadata().put("satelliteId", eventAdapter.satelliteId());
		productDto.getMetadata().put(MissionId.FIELD_NAME, eventAdapter.missionId());
		productDto.getMetadata().put("processMode", eventAdapter.processMode());
		productDto.getMetadata().put("startTime", eventAdapter.productSensingStartDate());
		productDto.getMetadata().put("stopTime", eventAdapter.productSensingStopDate());
		productDto.getMetadata().put("timeliness", eventAdapter.timeliness());
		productDto.getMetadata().put("acquistion", eventAdapter.swathType());
		return productDto;
	}
}

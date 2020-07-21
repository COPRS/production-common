package esa.s1pdgs.cpoc.production.trigger.taskTableMapping;

import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;

public class ConfigurableKeyEvaluator implements Function<AppDataJobProduct, String> {
	private final String template;
	
	public ConfigurableKeyEvaluator(final String template) {
		this.template = template;
	}

	@Override
	public String apply(final AppDataJobProduct t) {
		String result = template;
		for (final Map.Entry<String,Object> metadata : t.getMetadata().entrySet()) {
			result = result.replaceAll(		
					Pattern.quote("$(product." + metadata.getKey() + ")"),
					String.valueOf(metadata.getValue())
			);
		}
		return result;
	}

	
	
	
}

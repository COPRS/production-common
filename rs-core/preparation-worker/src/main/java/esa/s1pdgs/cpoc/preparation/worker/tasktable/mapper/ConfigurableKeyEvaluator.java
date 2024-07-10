/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package esa.s1pdgs.cpoc.preparation.worker.tasktable.mapper;

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

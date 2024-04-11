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

package esa.s1pdgs.cpoc.preparation.worker.tasktable.adapter;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.preparation.worker.config.PreparationWorkerProperties;
import esa.s1pdgs.cpoc.preparation.worker.config.ProcessProperties;

@Component
public final class ElementMapper {
	private final ProcessProperties l0ProcessSettings;
	private final PreparationWorkerProperties ipfPreparationWorkerSettings;

	@Autowired
	public ElementMapper(final ProcessProperties l0ProcessSettings,
			final PreparationWorkerProperties ipfPreparationWorkerSettings) {
		this.l0ProcessSettings = l0ProcessSettings;
		this.ipfPreparationWorkerSettings = ipfPreparationWorkerSettings;
	}

	public final Optional<String> getParameterValue(final String key) {
		if (l0ProcessSettings.getParams().containsKey(key)) {
			return Optional.of(l0ProcessSettings.getParams().get(key));
		}
		return Optional.empty();
	}

	public final String getRegexFor(final String filetype) {
		return l0ProcessSettings.getOutputregexps().getOrDefault(filetype, "^.*" + filetype + ".*$");
	}

	public final String mappedFileType(final String filetype) {
		return ipfPreparationWorkerSettings.getMapTypeMeta().getOrDefault(filetype, filetype);
	}

	public final ProductFamily outputFamilyOf(final String fileType) {
		return familyOf(ipfPreparationWorkerSettings.getOutputfamilies(), fileType, defaultFamily());
	}

	public final ProductFamily inputFamilyOf(final String fileType) {
		return familyOf(ipfPreparationWorkerSettings.getInputfamilies(), fileType, defaultFamily());
	}

	/**
	 * Checks if there is a matching regex for the fileType inside of the given map.
	 * If not returns defaultFamily.
	 */
	private final ProductFamily familyOf(final Map<String, ProductFamily> map, final String fileType,
			final ProductFamily defaultFamily) {		
		for (final Map.Entry<String, ProductFamily> entry : map.entrySet()) {
			final String regex = entry.getKey();
			if (fileType.matches(regex) || fileType.equals(regex)) {
				return entry.getValue();
			}
		}
		return defaultFamily;
	}

	final ProductFamily defaultFamily() {
		return ProductFamily.fromValue(ipfPreparationWorkerSettings.getDefaultfamily());
	}
}
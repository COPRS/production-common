package esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.IpfPreparationWorkerSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.ProcessSettings;

@Component
public final class ElementMapper {
	private final ProcessSettings l0ProcessSettings;
	private final IpfPreparationWorkerSettings ipfPreparationWorkerSettings;

	@Autowired
	public ElementMapper(final ProcessSettings l0ProcessSettings,
			final IpfPreparationWorkerSettings ipfPreparationWorkerSettings) {
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
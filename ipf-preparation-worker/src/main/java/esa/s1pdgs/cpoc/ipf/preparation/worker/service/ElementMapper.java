package esa.s1pdgs.cpoc.ipf.preparation.worker.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.IpfPreparationWorkerSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.ProcessSettings;

@Component
public final class ElementMapper
{
	private final ProcessSettings l0ProcessSettings;
	private final IpfPreparationWorkerSettings ipfPreparationWorkerSettings;

	@Autowired
	public ElementMapper(
			final ProcessSettings l0ProcessSettings,
			final IpfPreparationWorkerSettings ipfPreparationWorkerSettings
	) {
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
		return l0ProcessSettings.getOutputregexps().getOrDefault(
				filetype, 
				"^.*" + filetype + ".*$"
		);
	}		
	
	public final String mappedFileType(final String filetype) {
		return ipfPreparationWorkerSettings.getMapTypeMeta().getOrDefault(
				filetype,
				filetype
		);
	}

	public final ProductFamily outputFamilyOf(final String fileType) {
		return ipfPreparationWorkerSettings.getOutputfamilies().getOrDefault(fileType, defaultFamily());
	}
	
	public final ProductFamily inputFamilyOf(final String fileType) {
		return ipfPreparationWorkerSettings.getInputfamilies().getOrDefault(fileType, defaultFamily());	
	}
	
	final ProductFamily defaultFamily() {
		return ProductFamily.fromValue(ipfPreparationWorkerSettings.getDefaultfamily());
	}		
}
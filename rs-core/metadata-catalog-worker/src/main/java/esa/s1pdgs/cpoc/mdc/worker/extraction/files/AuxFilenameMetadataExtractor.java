package esa.s1pdgs.cpoc.mdc.worker.extraction.files;

import java.util.regex.Matcher;

public class AuxFilenameMetadataExtractor {
	private final Matcher matcher;
	
	public AuxFilenameMetadataExtractor(final Matcher matcher) {
		this.matcher = matcher;
	}

	public boolean matches() {
		return matcher.matches();
	}
	
	public final String getMissionId() {
		return matcher.group(1);
	}
	
	public final String getSatelliteId() {
		return matcher.group(2);
	}
	
	public final String getProductClass() {
		return matcher.group(4);
	}
	
	public final String getFileType() {
		final String typeString = matcher.group(5);

		if (FileDescriptorBuilder.AUX_ECE_TYPES.contains(typeString)) {
			return "AUX_ECE";
		}
		return typeString;
	}
	
	public final String getExtension() {
		return matcher.group(6);
	}
}

/**
 * 
 */
package esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model;

public enum RfiMitigationPerformed {

	NOT_SUPPORTED("NotSupported"), NEVER("Never"), BASED_ON_NOISE_MEAS("BasedOnNoiseMeas"), ALWAYS("Always");
	
	private final String strRepresentation;
	
	RfiMitigationPerformed(String str) {
		this.strRepresentation = str;
	}
	
	public String stringRepresentation() {
		return strRepresentation;
	}
	
	public static RfiMitigationPerformed fromString(String str) {
		if (NOT_SUPPORTED.strRepresentation.equalsIgnoreCase(str)) {
			return NOT_SUPPORTED;
		} else if (NEVER.strRepresentation.equalsIgnoreCase(str)) {
			return NEVER;
		} else if (BASED_ON_NOISE_MEAS.strRepresentation.equalsIgnoreCase(str)) {
			return BASED_ON_NOISE_MEAS;
		} else if (ALWAYS.strRepresentation.equalsIgnoreCase(str)) {
			return ALWAYS;
		} else {
			throw new IllegalArgumentException("Not supported: " + str);
		}
	}
}

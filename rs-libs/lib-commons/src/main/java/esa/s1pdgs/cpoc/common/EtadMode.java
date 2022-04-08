package esa.s1pdgs.cpoc.common;

public enum EtadMode {
	SLICE,
	DATATAKE,
	DATATAKE_AND_SLICE,
	NOT_DEFINED;
	
	public static EtadMode of(final String value) {
		for (final EtadMode mode : EtadMode.values()) {
			if (mode.name().equals(value)) {
				return mode;
			}
		}
		return EtadMode.NOT_DEFINED;
	}
}

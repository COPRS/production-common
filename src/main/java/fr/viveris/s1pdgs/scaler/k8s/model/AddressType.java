package fr.viveris.s1pdgs.scaler.k8s.model;

public enum AddressType {
	
	INTERNAL_IP("InternalIP"), EXTERNAL_IP("ExternalIP"), HOSTNAME("Hostname"), UNKNOWN("");
	
	private final String label;
	
	AddressType(String label) {
		this.label = label;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}
	
	public static AddressType fromLabel(String label) {
		AddressType[] types = values();
		for (int i=0; i< types.length; i++) {
			if (types[i].getLabel().equals(label)) {
				return types[i];
			}
		}
		return UNKNOWN;
	}
	
}

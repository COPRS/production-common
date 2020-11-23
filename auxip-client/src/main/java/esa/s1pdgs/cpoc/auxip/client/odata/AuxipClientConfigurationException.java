package esa.s1pdgs.cpoc.auxip.client.odata;

@SuppressWarnings("serial")
public class AuxipClientConfigurationException extends RuntimeException {
	public AuxipClientConfigurationException(final String message) {
		super(message);
	}
	public AuxipClientConfigurationException(final String message, final Throwable cause) {
		super(message, cause);
	}
}

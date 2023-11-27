package de.werum.coprs.cadip.client.odata;

@SuppressWarnings("serial")
public class CadipClientConfigurationException extends RuntimeException {
	public CadipClientConfigurationException(final String message) {
		super(message);
	}
	public CadipClientConfigurationException(final String message, final Throwable cause) {
		super(message, cause);
	}
}

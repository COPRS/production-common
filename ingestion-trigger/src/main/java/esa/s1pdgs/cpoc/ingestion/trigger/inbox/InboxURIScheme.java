package esa.s1pdgs.cpoc.ingestion.trigger.inbox;

public enum InboxURIScheme {

	FILE("file"), HTTPS("https");

	private String scheme;

	private InboxURIScheme(String scheme) {
		this.scheme = scheme;
	}

	public String getScheme() {
		return this.scheme;
	}

	public String getSchemeWithSlashes() {
		return this.scheme + "://";
	}

	public InboxURIScheme toScheme(String scheme) {
		if (scheme == null) {
			throw new IllegalArgumentException("scheme is null");
		}

		if (scheme.equalsIgnoreCase(FILE.getScheme())) {
			return FILE;
		} else if (scheme.equalsIgnoreCase(HTTPS.getScheme())) {
			return HTTPS;
		} else {
			throw new IllegalArgumentException(String.format("scheme not supported: %s", scheme));
		}
	}

}

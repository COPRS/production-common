package esa.s1pdgs.cpoc.mqi.server.config;

public enum MessagePersistenceStrategy {
	
	APP_CATALOG_MESSAGE_PERSISTENCE("AppCatalogMessagePersistence"),
	IN_MEMORY_MESSAGE_PERSISTENCE("InMemoryMessagePersistence");
	
	private final String value;

	MessagePersistenceStrategy(String value) {
		this.value = value;
	}
	
	String getValue() {
		return value;
	}

}

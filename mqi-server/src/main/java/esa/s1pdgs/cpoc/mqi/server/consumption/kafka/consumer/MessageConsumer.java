package esa.s1pdgs.cpoc.mqi.server.consumption.kafka.consumer;

@FunctionalInterface
public interface MessageConsumer<T> {	
	static final MessageConsumer<Object> NULL = p -> {};
	
	@SuppressWarnings("unchecked")
	public static <T> MessageConsumer<T> nullConsumer() {
		return (MessageConsumer<T>) NULL;
	}
	
	public void consume(T message) throws Exception;
}

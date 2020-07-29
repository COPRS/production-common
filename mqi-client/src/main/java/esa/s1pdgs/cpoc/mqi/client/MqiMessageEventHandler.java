package esa.s1pdgs.cpoc.mqi.client;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;

public class MqiMessageEventHandler {		
	public static final class Builder<E extends AbstractMessage> {		
		private final ProductCategory cat;
		
		private Callable<List<GenericPublicationMessageDto<E>>> processor = () -> Collections.emptyList();
		private Consumer<List<GenericPublicationMessageDto<E>>> onSuccess = p -> {};
		private Consumer<Exception> onError = p -> {};
		
		public Builder(final ProductCategory cat) {
			this.cat = cat;
		}

		public final Builder<E> messageHandling(final Callable<List<GenericPublicationMessageDto<E>>> messageProcessor) {
			if (messageProcessor != null) {
				processor = messageProcessor;
			}
			return this;
		}
		
		public final Builder<E> onSuccess(final Consumer<List<GenericPublicationMessageDto<E>>> consumer) {
			if (consumer != null) {
				onSuccess = consumer;
			}
			return this;
		}
		
		public final Builder<E> onError(final Consumer<Exception> consumer) {
			if (consumer != null) {
				onError = consumer;
			}
			return this;
		}
		
		public final MqiMessageEventHandler newResult() {
			return new MqiMessageEventHandler(this);
		}
	}
	
	private final Callable<List<GenericPublicationMessageDto<? extends AbstractMessage>>> processor;
	private final Consumer<List<GenericPublicationMessageDto<? extends AbstractMessage>>> onSuccess;
	private final Consumer<Exception> onError;
	private final ProductCategory cat;
	
	// here, the generic type doesn't matter any more. To avoid struggeling against the compiler, just just the raw type
	MqiMessageEventHandler(final Builder builder) {
		this.processor	= builder.processor;
		this.onSuccess 	= builder.onSuccess;
		this.onError 	= builder.onError;
		this.cat		= builder.cat;
	}

	public final void processMessages(final MqiClient client) throws Exception {		
		try {
			final List<GenericPublicationMessageDto<? extends AbstractMessage>> result = processor.call();
			for (final GenericPublicationMessageDto<? extends AbstractMessage> message : result) {
				MqiConsumer.LOG.info("Publishing category {} message: {}", cat, message);
				client.publish(message, cat);
			}			
			onSuccess.accept(result);
		}
		catch (final Exception e) {
			onError.accept(e);
			throw e;
		}
	}
}

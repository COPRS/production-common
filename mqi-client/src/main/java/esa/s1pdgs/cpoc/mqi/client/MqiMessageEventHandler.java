package esa.s1pdgs.cpoc.mqi.client;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.queue.NullMessage;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;

public class MqiMessageEventHandler {		
	public static final class Builder<E extends AbstractMessage> {		
		private final ProductCategory cat;
		
		@SuppressWarnings("unchecked")
		private Callable<MqiPublishingJob<E>> processor = ()-> (MqiPublishingJob<E>)MqiPublishingJob.NULL;
		private Consumer<List<GenericPublicationMessageDto<E>>> onSuccess = p -> {};
		private Consumer<List<GenericPublicationMessageDto<E>>> onWarning = p -> {};
		private Consumer<Exception> onError = p -> {};
		
		public Builder(final ProductCategory cat) {
			this.cat = cat;
		}

		public final Builder<E> publishMessageProducer(final Callable<MqiPublishingJob<E>> messageProcessor) {
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
		
		public final Builder<E> onWarning(final Consumer<List<GenericPublicationMessageDto<E>>> consumer) {
			if (consumer != null) {
				onWarning = consumer;
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
	
	public static final MqiMessageEventHandler nullHandler() {
		return new MqiMessageEventHandler.Builder<NullMessage>(ProductCategory.UNDEFINED)
				.newResult();
	}
	
	private final Callable<MqiPublishingJob<? extends AbstractMessage>> processor;
	private final Consumer<List<GenericPublicationMessageDto<? extends AbstractMessage>>> onSuccess;
	private final Consumer<List<GenericPublicationMessageDto<? extends AbstractMessage>>> onWarning;
	private final Consumer<Exception> onError;
	private final ProductCategory cat;
	
	// here, the generic type doesn't matter any more. To avoid struggeling against the compiler, just just the raw type
	MqiMessageEventHandler(final Builder builder) {
		this.processor	= builder.processor;
		this.onSuccess 	= builder.onSuccess;
		this.onWarning 	= builder.onWarning;
		this.onError 	= builder.onError;
		this.cat		= builder.cat;
	}

	public final String processMessages(final MqiClient client) throws Exception {		
		try {
			final MqiPublishingJob<?> mqiPublishingJob = processor.call();			
			for (final GenericPublicationMessageDto<? extends AbstractMessage> message : mqiPublishingJob.getMessages()) {
				MqiConsumer.LOG.info("Publishing category {} message: {}", cat, message);
				client.publish(message, cat);
			}
			String warning = mqiPublishingJob.getWarning();
			if (!"".equals(warning)) {
				onWarning.accept(mqiPublishingJob.getMessages());
			} else {
				onSuccess.accept(mqiPublishingJob.getMessages());
			}
			return warning;
		}
		catch (final Exception e) {
			onError.accept(e);
			throw e;
		}
	}
}

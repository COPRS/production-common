package esa.s1pdgs.cpoc.mqi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractDto;
import esa.s1pdgs.cpoc.mqi.model.rest.Ack;
import esa.s1pdgs.cpoc.mqi.model.rest.AckMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

public final class MqiConsumer<E extends AbstractDto> implements Runnable {
	private static final Logger LOG = LogManager.getLogger(MqiConsumer.class);
	
	private final MqiClient client;
	private final ProductCategory category;
	private final MqiListener<E> mqiListener;
	private final long pollingIntervalMillis;
	
	public MqiConsumer(
			final MqiClient client, 
			final ProductCategory category, 
			final MqiListener<E> mqiListener,
			final long pollingIntervalMillis
	) {
		this.client = client;
		this.category = category;
		this.mqiListener = mqiListener;
		this.pollingIntervalMillis = pollingIntervalMillis;
	}

	@Override
	public final void run() {
		LOG.info("Starting {}", this);
		while (!Thread.currentThread().isInterrupted()) {
			try {
				LOG.debug("{} polls MQI", this);
				final GenericMessageDto<E> message = client.next(category);		
				if (message == null || message.getBody() == null) {
					LOG.trace("No message received: continue");
					return;
				}			
				AckMessageDto ackMess;			
				try {
					mqiListener.onMessage(message);
					ackMess = new AckMessageDto(message.getIdentifier(), Ack.OK, null, false);
				// any other error --> dump prominently into log file but continue	
				} catch (Exception e) {
					LOG.error(String.format("Unexpected Error handling message %s", message), e);
					ackMess = new AckMessageDto(message.getIdentifier(), Ack.ERROR, LogUtils.toString(e), false);
				}			
				client.ack(ackMess, category);
				try {
					Thread.sleep(pollingIntervalMillis);
				} catch (InterruptedException e) {
					LOG.debug("{} has been cancelled", this);
					break;
				}
			// on communication errors with Mqi --> just dump warning and retry on next polling attempt
			} catch (AbstractCodedException ace) {
				LOG.warn("Error Code: {}, Message: {}", ace.getCode().getCode(), ace.getLogMessage());			
			}
		}
		LOG.info("Exiting {}", this);
	}

	@Override
	public final String toString() {
		return "MqiConsumer [category=" + category + "]";
	}
}

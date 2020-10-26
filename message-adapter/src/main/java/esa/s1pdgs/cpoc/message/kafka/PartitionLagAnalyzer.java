package esa.s1pdgs.cpoc.message.kafka;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.admin.Admin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import esa.s1pdgs.cpoc.message.kafka.config.KafkaProperties;

public class PartitionLagAnalyzer implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(PartitionLagAnalyzer.class);

    private final Admin adminClient;
    private final KafkaProperties properties;

    private volatile ScheduledExecutorService executor;

    public PartitionLagAnalyzer(final Admin adminClient, final KafkaProperties properties) {
        this.adminClient = adminClient;
        this.properties = properties;
    }

    @Override
    public void run() {
        LOG.debug("running lag kafka lag analyzing for consumer-group {}", properties.getProducer().getLagBasedPartitioner().getConsumerGroup());
    }

    public void start() {
        executor = Executors.newSingleThreadScheduledExecutor();
        final long delay = properties.getProducer().getLagBasedPartitioner().getDelaySeconds();
        executor.scheduleWithFixedDelay(this, delay, delay, TimeUnit.SECONDS);
        LOG.info("started {} with delay {} seconds", this, delay);
    }

    public void stop() {
        LOG.info("stopping {}", this);
        if (executor != null) {
            executor.shutdownNow();
        }
    }
}

package esa.s1pdgs.cpoc.message.kafka;

import java.util.Map;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.clients.producer.internals.DefaultPartitioner;
import org.apache.kafka.common.Cluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LagBasedPartitioner implements Partitioner {

    private static final Logger LOG = LoggerFactory.getLogger(LagBasedPartitioner.class);

    private final Partitioner backupPartitioner = new DefaultPartitioner();
    private final PartitionLagAnalyzer lagAnalyzer = null;


//    public LagBasedPartitioner(PartitionLagAnalyzer lagAnalyzer) {
//        this.lagAnalyzer = lagAnalyzer;
//    }

    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        int partition = backupPartitioner.partition(topic, key, keyBytes, value, valueBytes, cluster);
        LOG.debug("use partition {} for new message on topic {}", partition, topic);
        return partition;
    }

    @Override
    public void close() {
    }

    @Override
    public void configure(Map<String, ?> configs) {
        LOG.debug("configure: {}", configs);
        backupPartitioner.configure(configs);
    }
}

package esa.s1pdgs.cpoc.message.kafka;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.clients.producer.internals.DefaultPartitioner;
import org.apache.kafka.common.Cluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import esa.s1pdgs.cpoc.message.kafka.config.KafkaProperties;

public class LagBasedPartitioner implements Partitioner {

    public static final String KAFKA_PROPERTIES = LagBasedPartitioner.class.getSimpleName() + ".kafka.properties";

    private static final Logger LOG = LoggerFactory.getLogger(LagBasedPartitioner.class);

    private final Partitioner backupPartitioner = new DefaultPartitioner();
    private volatile PartitionLagAnalyzer lagAnalyzer = null;


    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        int partition = backupPartitioner.partition(topic, key, keyBytes, value, valueBytes, cluster);
        LOG.debug("use partition {} for new message on topic {}", partition, topic);
        return partition;
    }

    @Override
    public void close() {
        backupPartitioner.close();
        if(lagAnalyzer != null) {
            lagAnalyzer.stop();
        }
    }

    @Override
    public void configure(Map<String, ?> configs) {
        LOG.debug("configure: {}", configs);
        backupPartitioner.configure(configs);

        if(lagAnalyzer == null) {
            Map<String, Object> adminConfig = new HashMap<>();
            KafkaProperties kafkaProperties = (KafkaProperties) configs.get(LagBasedPartitioner.KAFKA_PROPERTIES);
            adminConfig.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
            lagAnalyzer = new PartitionLagAnalyzer(Admin.create(adminConfig),kafkaProperties );
            lagAnalyzer.start();
        }
    }

    @Override
    public void onNewBatch(String topic, Cluster cluster, int prevPartition) {
        backupPartitioner.onNewBatch(topic, cluster, prevPartition);
    }
}

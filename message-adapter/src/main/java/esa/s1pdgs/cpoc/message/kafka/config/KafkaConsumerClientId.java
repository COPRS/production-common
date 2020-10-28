package esa.s1pdgs.cpoc.message.kafka.config;

public class KafkaConsumerClientId {

    public static String clientIdForRawIdAndTopic(final String rawId, final String topic) {
        return rawId + "-" + topic;
    }

    public static String rawIdForTopic(final String clientId, final String topic) {
        return clientId.replaceAll("-" + topic + "$", "");
    }

}

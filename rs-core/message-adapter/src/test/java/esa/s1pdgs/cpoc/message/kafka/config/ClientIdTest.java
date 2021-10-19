package esa.s1pdgs.cpoc.message.kafka.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

public class ClientIdTest {

    @Test
    public void clientIdForRawIdAndTopic() {
        assertThat(KafkaConsumerClientId.clientIdForRawIdAndTopic("worker0-host0", "topic33"), is(equalTo("worker0-host0-topic33")));
    }

    @Test
    public void rawIdForTopic() {
        assertThat(KafkaConsumerClientId.rawIdForTopic("worker0-host0-topic33-0", "topic33"), is(equalTo("worker0-host0")));
        assertThat(KafkaConsumerClientId.rawIdForTopic("worker0-host0-topic33-1", "topic33"), is(equalTo("worker0-host0")));
        assertThat(KafkaConsumerClientId.rawIdForTopic("worker0-host0-topic33-99", "topic33"), is(equalTo("worker0-host0")));
    }
}
package esa.s1pdgs.cpoc.message.kafka;

import java.util.Map;

public interface ProducerConfigurationFactory {

    Map<String, Object> producerConfiguration();

}
